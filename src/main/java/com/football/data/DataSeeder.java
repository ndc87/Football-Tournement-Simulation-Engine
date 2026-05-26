package com.football.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.football.model.Player;
import com.football.model.Position;
import com.football.model.Team;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp tải và khởi tạo dữ liệu đội bóng từ file JSON.
 *
 * DataSeeder xử lý:
 *  - Đọc file teams.json từ classpath
 *  - Parse JSON → Tạo đối tượng Team và Player
 *  - Validate dữ liệu (ratings, stamina, form)
 *  - Trả về danh sách đội sẵn sàng cho Tournament
 *
 * Xử lý lỗi:
 *  - File không tìm thấy → IOException rõ ràng
 *  - Định dạng JSON sai → Thông báo field bị thiếu
 *  - Rating ngoài khoảng → IllegalArgumentException
 */
public class DataSeeder {

    private static final String DEFAULT_RESOURCE_PATH = "/teams.json";
    private final ObjectMapper objectMapper;

    public DataSeeder() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Tải danh sách đội bóng từ file JSON mặc định (teams.json trong classpath).
     *
     * @return Danh sách các đội bóng đã được khởi tạo đầy đủ
     * @throws IOException Nếu không đọc được file hoặc JSON bị lỗi định dạng
     */
    public List<Team> loadTeams() throws IOException {
        return loadTeams(DEFAULT_RESOURCE_PATH);
    }

    /**
     * Tải danh sách đội bóng từ một đường dẫn resource cụ thể.
     *
     * @param resourcePath Đường dẫn file JSON trong classpath
     * @return Danh sách các đội bóng
     * @throws IOException Nếu không đọc được file
     */
    public List<Team> loadTeams(String resourcePath) throws IOException {
        InputStream inputStream = DataSeeder.class.getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IOException(
                    "Không tìm thấy file dữ liệu: " + resourcePath +
                    "\nHãy đảm bảo file tồn tại trong src/main/resources/"
            );
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(inputStream);
        } catch (IOException e) {
            throw new IOException("Lỗi parse JSON từ " + resourcePath + ": " + e.getMessage(), e);
        }

        JsonNode teamsNode = root.get("teams");
        if (teamsNode == null || !teamsNode.isArray()) {
            throw new IOException("File JSON không có mảng 'teams'. Kiểm tra cấu trúc file.");
        }

        List<Team> teams = new ArrayList<>();
        int teamIndex = 0;

        for (JsonNode teamNode : teamsNode) {
            try {
                Team team = parseTeam(teamNode, teamIndex);
                teams.add(team);
                teamIndex++;
            } catch (Exception e) {
                throw new IOException(
                        "Lỗi parse đội bóng thứ " + (teamIndex + 1) + ": " + e.getMessage(), e
                );
            }
        }

        System.out.printf("✅ Đã tải thành công %d đội bóng từ %s%n", teams.size(), resourcePath);
        return teams;
    }

    /**
     * Parse một node JSON thành đối tượng Team.
     */
    private Team parseTeam(JsonNode node, int index) {
        String name = requireString(node, "name", "team[" + index + "]");
        String shortName = requireString(node, "shortName", name);
        int attackRating = requireInt(node, "attackRating", name);
        int defenseRating = requireInt(node, "defenseRating", name);
        int midfieldRating = requireInt(node, "midfieldRating", name);

        Team team = new Team(name, shortName, attackRating, defenseRating, midfieldRating);

        // Parse danh sách cầu thủ (không bắt buộc)
        JsonNode playersNode = node.get("players");
        if (playersNode != null && playersNode.isArray()) {
            for (JsonNode playerNode : playersNode) {
                try {
                    Player player = parsePlayer(playerNode, name);
                    team.addPlayer(player);
                } catch (Exception e) {
                    System.err.println("  ⚠️  Bỏ qua cầu thủ lỗi trong " + name + ": " + e.getMessage());
                }
            }
        }

        return team;
    }

    /**
     * Parse một node JSON thành đối tượng Player.
     */
    private Player parsePlayer(JsonNode node, String teamName) {
        String name = requireString(node, "name", "player in " + teamName);
        String positionStr = requireString(node, "position", name);
        double stamina = node.has("stamina") ? node.get("stamina").asDouble() : Player.MAX_STAMINA;
        double form = node.has("form") ? node.get("form").asDouble() : 0.80;

        Position position;
        try {
            position = Position.valueOf(positionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Vị trí không hợp lệ '" + positionStr + "' cho cầu thủ " + name +
                    ". Các vị trí hợp lệ: GOALKEEPER, DEFENDER, MIDFIELDER, FORWARD"
            );
        }

        return new Player(name, position, stamina, form);
    }

    // ==============================
    // Helper Methods
    // ==============================

    private String requireString(JsonNode node, String field, String context) {
        if (!node.has(field) || node.get(field).isNull()) {
            throw new IllegalArgumentException("Thiếu trường bắt buộc '" + field + "' trong: " + context);
        }
        return node.get(field).asText();
    }

    private int requireInt(JsonNode node, String field, String context) {
        if (!node.has(field) || node.get(field).isNull()) {
            throw new IllegalArgumentException("Thiếu trường bắt buộc '" + field + "' trong: " + context);
        }
        return node.get(field).asInt();
    }

    /**
     * In thông tin chi tiết các đội đã tải để kiểm tra.
     */
    public static void printTeamsSummary(List<Team> teams) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   📋  DANH SÁCH ĐỘI BÓNG                           ║");
        System.out.println("╠═══╦══════════════════════════════╦═════╦═════╦═════╦════════════════╣");
        System.out.println("║ # ║ Đội bóng                     ║ ATK ║ DEF ║ MID ║    Overall     ║");
        System.out.println("╠═══╬══════════════════════════════╬═════╬═════╬═════╬════════════════╣");

        for (int i = 0; i < teams.size(); i++) {
            Team t = teams.get(i);
            System.out.printf("║%3d║ %-29s║%5d║%5d║%5d║%14.1f  ║%n",
                    i + 1, t.getName(), t.getAttackRating(),
                    t.getDefenseRating(), t.getMidfieldRating(), t.getOverallRating());
        }

        System.out.println("╚═══╩══════════════════════════════╩═════╩═════╩═════╩════════════════╝");
        System.out.printf("  Tổng cộng: %d đội bóng%n%n", teams.size());
    }

    /**
     * In danh sách các đội đã tải với thông tin xG thực tế.
     */
    public static void printTeamsSummaryWithXG(List<Team> teams) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   📋  DANH SÁCH ĐỘI BÓNG (VỚI KAGGLE XG DATA)              ║");
        System.out.println("╠═══╦══════════════════════════════╦═════╦═════╦═════╦══════════╦══════════╣");
        System.out.println("║ # ║ Đội bóng                     ║ ATK ║ DEF ║ MID ║ xG/game  ║ xGA/game ║");
        System.out.println("╠═══╬══════════════════════════════╬═════╬═════╬═════╬══════════╬══════════╣");

        for (int i = 0; i < teams.size(); i++) {
            Team t = teams.get(i);
            String xgStr  = t.hasRealXG() ? String.format("%.2f", t.getXGPerGame()) : "N/A ";
            String xgaStr = t.hasRealXGA() ? String.format("%.2f", t.getXGAPerGame()) : "N/A ";
            System.out.printf("║%3d║ %-28s ║ %3d ║ %3d ║ %3d ║   %4s   ║   %4s   ║%n",
                    i + 1, t.getName(),
                    t.getAttackRating(), t.getDefenseRating(), t.getMidfieldRating(),
                    xgStr, xgaStr);
        }

        System.out.println("╚═══╩══════════════════════════════╩═════╩═════╩═════╩══════════╩══════════╝");
        System.out.println("  Tổng cộng: " + teams.size() + " đội bóng");
        System.out.println();
    }
}
