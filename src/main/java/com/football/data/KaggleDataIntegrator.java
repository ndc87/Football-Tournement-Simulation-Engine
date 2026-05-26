package com.football.data;

import com.football.model.Team;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Tích hợp dữ liệu thực tế từ Kaggle Premier League 2024-2025 Dataset.
 *
 * Dataset: https://www.kaggle.com/datasets/furkanark/premier-league-2024-2025-data
 * (Scraped từ FBref - CC BY-SA 4.0)
 *
 * Chức năng:
 *  1. Đọc `overwiev__results2024-202591_overall.csv`:
 *     → xG, xGA, W/D/L/GF/GA → tính lại attackRating, defenseRating
 *  2. Đọc `overwiev__stats_squads_standard_for.csv`:
 *     → Gls/90, Poss, CrdY → enrich Team stats
 *  3. So sánh kết quả mô phỏng vs kết quả thực tế (validation)
 *
 * Công thức normalize xG → Rating (scale 50-99):
 *   attackRating  = normalize(xG/game,  min=0.60, max=2.30, scale=50..99)
 *   defenseRating = normalize(xGA/game, min=0.60, max=2.30, scale=99..50) [inverted]
 *   midfieldRating = (Poss - 35) / (65 - 35) * 49 + 50  [possession proxy]
 */
public class KaggleDataIntegrator {

    private static final String OVERALL_CSV  = "overwiev__results2024-202591_overall.csv";
    private static final String STANDARD_FOR = "overwiev__stats_squads_standard_for.csv";

    private final String dataDir;

    public KaggleDataIntegrator(String dataDir) {
        this.dataDir = dataDir;
    }

    // ==============================
    // Main Entry: Enrich all teams
    // ==============================

    /**
     * Enrichment đầy đủ: đọc CSV và cập nhật xG + ratings cho danh sách đội.
     * Các đội không tìm thấy trong CSV được giữ nguyên dữ liệu từ teams.json.
     *
     * @param teams Danh sách đội cần enrich
     * @return Số đội đã được enrich thành công
     */
    public int enrichTeams(List<Team> teams) {
        // Map: tên đội thực tế → dữ liệu xG từ Kaggle
        Map<String, TeamKaggleData> kaggleMap = loadKaggleData();

        if (kaggleMap.isEmpty()) {
            System.err.println("  ⚠️  Không đọc được dữ liệu Kaggle. Dùng dữ liệu ước tính.");
            return 0;
        }

        int enriched = 0;
        for (Team team : teams) {
            // Tìm tên match (Kaggle dùng tên khác ở một số đội)
            String kaggleKey = resolveKaggleName(team.getName());
            TeamKaggleData data = kaggleMap.get(kaggleKey);

            if (data != null) {
                applyRealData(team, data);
                enriched++;
            } else {
                System.out.printf("  ⚠️  Không tìm thấy '%s' (key='%s') trong Kaggle data%n",
                        team.getName(), kaggleKey);
            }
        }

        return enriched;
    }

    /**
     * Áp dụng dữ liệu thực tế vào Team:
     *  - Tính lại ratings từ xG/xGA
     *  - Lưu xGPerGame để calibrate simulation
     */
    private void applyRealData(Team team, TeamKaggleData data) {
        // Lưu xG gốc để dùng trong simulation engine
        team.setXGPerGame(data.xGPerGame);
        team.setXGAPerGame(data.xGAPerGame);
        team.setPossession(data.possession);
        team.setYellowCardsPerGame(data.yellowCardsPerGame);
    }

    // ==============================
    // CSV Loaders
    // ==============================

    /**
     * Đọc cả hai CSV và merge thành Map<teamName, TeamKaggleData>.
     */
    private Map<String, TeamKaggleData> loadKaggleData() {
        Map<String, TeamKaggleData> result = new LinkedHashMap<>();

        // 1. Đọc overall table (xG, xGA, W/D/L/GF/GA)
        loadOverallTable(result);

        // 2. Đọc squad standard stats (Gls/90, Poss, CrdY)
        loadSquadStandardFor(result);

        return result;
    }

    /**
     * Đọc `overwiev__results2024-202591_overall.csv`.
     * Columns: Rk,Squad,MP,W,D,L,GF,GA,GD,Pts,...,xG,xGA,...
     */
    private void loadOverallTable(Map<String, TeamKaggleData> result) {
        File file = new File(dataDir, OVERALL_CSV);
        if (!file.exists()) {
            System.err.println("  ⚠️  Không tìm thấy: " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null) return;

            // Parse header để tìm vị trí các cột
            String[] cols = header.split(",");
            int idxSquad = findCol(cols, "Squad");
            int idxW     = findCol(cols, "W");
            int idxD     = findCol(cols, "D");
            int idxL     = findCol(cols, "L");
            int idxGF    = findCol(cols, "GF");
            int idxGA    = findCol(cols, "GA");
            int idxPts   = findCol(cols, "Pts");
            int idxXG    = findColExact(cols, "xG");
            int idxXGA   = findColExact(cols, "xGA");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length <= idxSquad) continue;

                String squad = parts[idxSquad].trim();
                if (squad.isEmpty()) continue;

                TeamKaggleData data = result.computeIfAbsent(squad, TeamKaggleData::new);
                data.wins   = safeInt(parts, idxW,  0);
                data.draws  = safeInt(parts, idxD,  0);
                data.losses = safeInt(parts, idxL,  0);
                data.goalsFor     = safeInt(parts, idxGF,  0);
                data.goalsAgainst = safeInt(parts, idxGA,  0);
                data.points       = safeInt(parts, idxPts, 0);

                double totalXG  = safeDouble(parts, idxXG,  -1.0);
                double totalXGA = safeDouble(parts, idxXGA, -1.0);
                // Convert total xG → per game (38 matches)
                data.xGPerGame  = totalXG  > 0 ? totalXG  / 38.0 : -1.0;
                data.xGAPerGame = totalXGA > 0 ? totalXGA / 38.0 : -1.0;
            }

        } catch (IOException e) {
            System.err.println("  ⚠️  Lỗi đọc " + OVERALL_CSV + ": " + e.getMessage());
        }
    }

    /**
     * Đọc `overwiev__stats_squads_standard_for.csv`.
     * Columns: Squad,...,Poss,...,Performance_CrdY,...,Per 90 Minutes_Gls,...
     */
    private void loadSquadStandardFor(Map<String, TeamKaggleData> result) {
        File file = new File(dataDir, STANDARD_FOR);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null) return;

            String[] cols = header.split(",");
            int idxSquad  = findCol(cols, "Squad");
            int idxPoss   = findCol(cols, "Poss");
            int idxCrdY   = findCol(cols, "Performance_CrdY");
            int idxGls90  = findCol(cols, "Per 90 Minutes_Gls");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length <= idxSquad) continue;

                String squad = parts[idxSquad].trim();
                TeamKaggleData data = result.computeIfAbsent(squad, TeamKaggleData::new);
                data.possession        = safeDouble(parts, idxPoss, -1.0);
                data.yellowCards       = safeInt(parts, idxCrdY, 0);
                data.yellowCardsPerGame = data.yellowCards > 0 ? (int)(data.yellowCards / 38.0) : 0;
                data.goalsPerNinety    = safeDouble(parts, idxGls90, -1.0);
            }

        } catch (IOException e) {
            System.err.println("  ⚠️  Lỗi đọc " + STANDARD_FOR + ": " + e.getMessage());
        }
    }

    // ==============================
    // Rating Normalization
    // ==============================

    /**
     * Normalize một giá trị từ [minVal, maxVal] sang [minRating, maxRating].
     * Clamp nếu giá trị ngoài khoảng.
     */
    public static int normalize(double value, double minVal, double maxVal,
                                 int minRating, int maxRating) {
        if (value <= minVal) return minRating;
        if (value >= maxVal) return maxRating;
        double ratio = (value - minVal) / (maxVal - minVal);
        return (int) Math.round(minRating + ratio * (maxRating - minRating));
    }

    // ==============================
    // Validation: Sim vs Reality
    // ==============================

    /**
     * In bảng so sánh kết quả mô phỏng vs kết quả thực tế.
     *
     * @param realData   Map từ tên đội → dữ liệu thực tế
     * @param simPoints  Map từ tên đội → điểm mô phỏng
     */
    public void printValidationReport(Map<String, TeamKaggleData> realData,
                                       Map<String, Integer> simPoints) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║              📊  SO SÁNH MÔ PHỎNG VS THỰC TẾ 2024/25                       ║");
        System.out.println("╠══════════════════════════╦════════════════╦════════════════╦════════════════╣");
        System.out.println("║ Đội bóng                 ║  Điểm Thực tế  ║  Điểm Mô phỏng ║    Sai lệch    ║");
        System.out.println("╠══════════════════════════╬════════════════╬════════════════╬════════════════╣");

        // Sort theo điểm thực tế
        List<Map.Entry<String, TeamKaggleData>> sorted = new ArrayList<>(realData.entrySet());
        sorted.sort((a, b) -> b.getValue().points - a.getValue().points);

        int totalError = 0;
        int count = 0;
        for (Map.Entry<String, TeamKaggleData> entry : sorted) {
            String team = entry.getKey();
            int realPts = entry.getValue().points;
            Integer simPts = simPoints.get(team);
            if (simPts == null) {
                // Thử resolve tên
                for (Map.Entry<String, Integer> se : simPoints.entrySet()) {
                    if (se.getKey().contains(team.split(" ")[0]) ||
                        team.contains(se.getKey().split(" ")[0])) {
                        simPts = se.getValue();
                        break;
                    }
                }
            }

            if (simPts != null) {
                int error = Math.abs(realPts - simPts);
                String accuracy = error <= 5 ? "✅" : error <= 15 ? "⚠️" : "❌";
                System.out.printf("║ %-25s║%15d ║%15d ║  %+4d  %s       ║%n",
                        team, realPts, simPts, (simPts - realPts), accuracy);
                totalError += error;
                count++;
            }
        }

        System.out.println("╚══════════════════════════╩════════════════╩════════════════╩════════════════╝");
        if (count > 0) {
            System.out.printf("  📈 Sai lệch trung bình: %.1f điểm/đội%n", (double) totalError / count);
        }
        System.out.println();
    }

    /**
     * Tải dữ liệu Kaggle dưới dạng map để dùng trong validation.
     */
    public Map<String, TeamKaggleData> loadKaggleDataPublic() {
        return loadKaggleData();
    }

    // ==============================
    // Team Name Mapping
    // ==============================

    /**
     * Map tên đội từ teams.json → tên trong Kaggle CSV.
     * Kaggle dùng tên khác ở một số đội (Man Utd, Wolves, Brighton).
     */
    private String resolveKaggleName(String teamName) {
        return switch (teamName) {
            case "Manchester United"          -> "Manchester Utd";
            case "Wolverhampton Wanderers"    -> "Wolves";
            case "Brighton & Hove Albion"     -> "Brighton";
            case "Nottingham Forest"          -> "Nottingham Forest";
            case "Tottenham Hotspur"          -> "Tottenham Hotspur";
            default                           -> teamName;
        };
    }

    // ==============================
    // CSV Parsing Helpers
    // ==============================

    private int findCol(String[] cols, String name) {
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].trim().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    private int findColExact(String[] cols, String name) {
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].trim().equals(name)) return i;
        }
        return -1;
    }

    /** Parse CSV line với hỗ trợ quoted fields. */
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        for (char c : line.toCharArray()) {
            if (c == '"') { inQuote = !inQuote; }
            else if (c == ',' && !inQuote) { tokens.add(sb.toString()); sb.setLength(0); }
            else { sb.append(c); }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    private int safeInt(String[] parts, int idx, int def) {
        if (idx < 0 || idx >= parts.length) return def;
        try { return Integer.parseInt(parts[idx].trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private double safeDouble(String[] parts, int idx, double def) {
        if (idx < 0 || idx >= parts.length) return def;
        try { return Double.parseDouble(parts[idx].trim()); }
        catch (NumberFormatException e) { return def; }
    }

    // ==============================
    // Inner class: TeamKaggleData
    // ==============================

    /** Lưu trữ toàn bộ dữ liệu thực tế của một đội từ Kaggle CSV. */
    public static class TeamKaggleData {
        public final String name;
        public double xGPerGame   = -1.0;
        public double xGAPerGame  = -1.0;
        public double possession  = -1.0;
        public double goalsPerNinety = -1.0;
        public int wins           = 0;
        public int draws          = 0;
        public int losses         = 0;
        public int goalsFor       = 0;
        public int goalsAgainst   = 0;
        public int points         = 0;
        public int yellowCards    = 0;
        public int yellowCardsPerGame = 0;

        public TeamKaggleData(String name) { this.name = name; }

        /** Tính attackRating từ xG (normalize 50-99). */
        public int computeAttackRating() {
            if (xGPerGame <= 0) return 70; // fallback
            return KaggleDataIntegrator.normalize(xGPerGame, 0.60, 2.30, 50, 99);
        }

        /** Tính defenseRating từ xGA (normalize inverted: xGA thấp = phòng thủ tốt). */
        public int computeDefenseRating() {
            if (xGAPerGame <= 0) return 70; // fallback
            return KaggleDataIntegrator.normalize(xGAPerGame, 0.60, 2.30, 99, 50); // inverted
        }

        /** Tính midfieldRating từ possession (proxy). */
        public int computeMidfieldRating() {
            if (possession <= 0) return 70; // fallback
            return KaggleDataIntegrator.normalize(possession, 35.0, 65.0, 55, 95);
        }

        @Override
        public String toString() {
            return String.format("%s | xG/g=%.2f xGA/g=%.2f Poss=%.1f%% Pts=%d",
                    name, xGPerGame, xGAPerGame, possession, points);
        }
    }
}
