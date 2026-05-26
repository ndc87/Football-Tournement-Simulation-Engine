package com.football;

import com.football.data.DataSeeder;
import com.football.data.KaggleDataIntegrator;
import com.football.engine.FixtureGenerator;
import com.football.engine.MatchSimulationEngine;
import com.football.model.Match;
import com.football.model.Team;
import com.football.model.Tournament;
import com.football.observer.LeagueTable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point chính của Football Tournament Simulation Engine.
 *
 * Luồng thực thi:
 *  1. Hiển thị banner và menu lựa chọn
 *  2. Tải dữ liệu 20 đội bóng từ teams.json
 *  3. [MỚI] Enrich dữ liệu bằng Kaggle Premier League 2024/25 dataset (xG thực tế)
 *  4. Tạo lịch thi đấu Round-Robin (380 trận)
 *  5. Cho phép người dùng chạy từng vòng hoặc toàn bộ giải
 *  6. So sánh kết quả mô phỏng với kết quả thực tế
 */
public class Main {

    /** Đường dẫn đến thư mục chứa Kaggle CSV files. */
    private static final String KAGGLE_DATA_DIR =
            "d:\\codeNDC\\Football-Tournement-Simulation-Engine\\data\\kaggle";

    public static void main(String[] args) {
        printBanner();

        // === Bước 1: Tải dữ liệu cơ sở từ teams.json ===
        System.out.println("📂 Đang tải dữ liệu đội bóng từ teams.json...");
        DataSeeder seeder = new DataSeeder();
        List<Team> teams;

        try {
            teams = seeder.loadTeams();
        } catch (IOException e) {
            System.err.println("❌ Lỗi tải dữ liệu: " + e.getMessage());
            System.exit(1);
            return;
        }

        // === Bước 2: Enrich bằng dữ liệu Kaggle thực tế ===
        System.out.println("📊 Đang tích hợp dữ liệu Kaggle Premier League 2024/25...");
        KaggleDataIntegrator integrator = new KaggleDataIntegrator(KAGGLE_DATA_DIR);
        int enriched = integrator.enrichTeams(teams);

        if (enriched > 0) {
            System.out.printf("✅ Đã enrich %d/%d đội với dữ liệu xG thực tế từ Kaggle%n",
                    enriched, teams.size());
            System.out.println("   ► Chế độ simulation: xG-calibrated (dựa trên dữ liệu FBref 2024/25)");
        } else {
            System.out.println("ℹ️  Dùng dữ liệu thống kê ước tính (fallback mode)");
        }

        // In danh sách đội bóng với xG
        DataSeeder.printTeamsSummaryWithXG(teams);

        // === Bước 3: Khởi tạo Tournament ===
        Tournament tournament = new Tournament("Premier League 2024/25");
        teams.forEach(tournament::addTeam);

        // === Bước 4: Tạo lịch thi đấu ===
        System.out.println("📅 Đang tạo lịch thi đấu...");
        FixtureGenerator generator = new FixtureGenerator();
        List<List<Match>> fixtures = generator.generate(teams);
        tournament.setFixtures(fixtures);

        System.out.println();
        System.out.printf("✅ %s%n", tournament);

        // Xác nhận lịch thi đấu hợp lệ
        boolean valid = generator.validate(fixtures, teams);
        if (!valid) {
            System.err.println("❌ Lịch thi đấu không hợp lệ! Dừng chương trình.");
            System.exit(1);
        }

        // === Bước 5: Menu tương tác ===
        runInteractiveMenu(tournament, integrator);
    }

    /**
     * Menu tương tác để người dùng điều khiển giải đấu.
     */
    private static void runInteractiveMenu(Tournament tournament, KaggleDataIntegrator integrator) {
        MatchSimulationEngine engine = new MatchSimulationEngine();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            int currentWeek = tournament.getCurrentMatchweek();
            int totalWeeks = tournament.getTotalMatchweeks();

            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.printf("  🏆 %s | Vòng hiện tại: %d/%d%n",
                    tournament.getName(), currentWeek, totalWeeks);
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.println("  [1] Chạy vòng đấu tiếp theo");
            System.out.println("  [2] Chạy 5 vòng đấu tiếp theo");
            System.out.println("  [3] Chạy toàn bộ giải đấu");
            System.out.println("  [4] Xem bảng xếp hạng hiện tại");
            System.out.println("  [5] Xem kết quả một vòng đấu cụ thể");
            System.out.println("  [0] Thoát");
            System.out.println("═══════════════════════════════════════════════════════════════");
            System.out.print("  Nhập lựa chọn: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    if (currentWeek >= totalWeeks) {
                        System.out.println("  ℹ️  Giải đấu đã kết thúc! Không còn vòng đấu nào.");
                    } else {
                        simulateNextMatchweek(tournament, engine);
                    }
                }
                case "2" -> {
                    int count = Math.min(5, totalWeeks - currentWeek);
                    System.out.printf("  ▶ Đang chạy %d vòng đấu...%n", count);
                    engine.setVerbose(false);
                    for (int i = 0; i < count; i++) {
                        if (tournament.getCurrentMatchweek() < totalWeeks) {
                            simulateNextMatchweekQuiet(tournament, engine);
                        }
                    }
                    engine.setVerbose(true);
                    System.out.printf("  ✅ Đã hoàn thành. Vòng hiện tại: %d/%d%n",
                            tournament.getCurrentMatchweek(), totalWeeks);
                    tournament.getLeagueTable().printStandings();
                }
                case "3" -> {
                    int remaining = totalWeeks - currentWeek;
                    if (remaining <= 0) {
                        System.out.println("  ℹ️  Giải đấu đã kết thúc!");
                    } else {
                        System.out.printf("  ▶ Đang chạy %d vòng đấu còn lại... (chế độ nhanh)%n", remaining);
                        engine.setVerbose(false);
                        while (tournament.getCurrentMatchweek() < totalWeeks) {
                            simulateNextMatchweekQuiet(tournament, engine);
                        }
                        engine.setVerbose(true);
                        System.out.println("  🏁 Giải đấu đã kết thúc!");
                        tournament.getLeagueTable().printStandings();
                        printChampion(tournament);
                        
                        // In báo cáo so sánh
                        if (integrator != null) {
                            Map<String, KaggleDataIntegrator.TeamKaggleData> realData = integrator.loadKaggleDataPublic();
                            if (!realData.isEmpty()) {
                                Map<String, Integer> simPoints = new java.util.HashMap<>();
                                for (Object obj : tournament.getLeagueTable().getSortedStandings()) {
                                    LeagueTable.TeamStats stats = (LeagueTable.TeamStats) obj;
                                    simPoints.put(stats.team.getName(), stats.points);
                                }
                                integrator.printValidationReport(realData, simPoints);
                            }
                        }
                    }
                }
                case "4" -> tournament.getLeagueTable().printStandings();
                case "5" -> {
                    System.out.printf("  Nhập số vòng đấu (1 - %d): ", totalWeeks);
                    try {
                        int week = Integer.parseInt(scanner.nextLine().trim());
                        printMatchweekResults(tournament, week);
                    } catch (NumberFormatException e) {
                        System.out.println("  ❌ Số vòng đấu không hợp lệ.");
                    }
                }
                case "0" -> {
                    System.out.println();
                    System.out.println("  👋 Cảm ơn đã sử dụng Football Tournament Simulation Engine!");
                    System.out.println("     ⚽ See you next season!");
                    System.out.println();
                    scanner.close();
                    return;
                }
                default -> System.out.println("  ❌ Lựa chọn không hợp lệ. Vui lòng thử lại.");
            }
        }
    }

    /**
     * Chạy vòng đấu tiếp theo với output chi tiết.
     */
    private static void simulateNextMatchweek(Tournament tournament, MatchSimulationEngine engine) {
        int nextWeek = tournament.getCurrentMatchweek() + 1;
        List<Match> matches = tournament.getMatchweekFixtures(nextWeek);
        engine.simulateMatchweek(matches);
        tournament.setCurrentMatchweek(nextWeek);
        tournament.getLeagueTable().printStandings();
    }

    /**
     * Chạy vòng đấu tiếp theo trong chế độ im lặng (không in log từng phút).
     */
    private static void simulateNextMatchweekQuiet(Tournament tournament, MatchSimulationEngine engine) {
        int nextWeek = tournament.getCurrentMatchweek() + 1;
        List<Match> matches = tournament.getMatchweekFixtures(nextWeek);
        for (Match match : matches) {
            engine.simulate(match);
        }
        tournament.setCurrentMatchweek(nextWeek);
    }

    /**
     * In kết quả các trận của một vòng đấu.
     */
    private static void printMatchweekResults(Tournament tournament, int matchweek) {
        try {
            List<Match> matches = tournament.getMatchweekFixtures(matchweek);
            System.out.println();
            System.out.printf("  📋 Kết quả Vòng %d:%n", matchweek);
            System.out.println("  " + "─".repeat(60));
            for (Match m : matches) {
                System.out.println("  " + m.getResultSummary());
            }
            System.out.println();
        } catch (IllegalArgumentException e) {
            System.out.println("  ❌ " + e.getMessage());
        }
    }

    /**
     * In thông tin nhà vô địch giải đấu.
     */
    private static void printChampion(Tournament tournament) {
        List<?> standings = tournament.getLeagueTable().getSortedStandings();
        if (!standings.isEmpty()) {
            var champion = (com.football.observer.LeagueTable.TeamStats) standings.get(0);
            System.out.println();
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.printf("║   🏆  NHÀ VÔ ĐỊCH: %-40s║%n", champion.team.getName());
            System.out.printf("║       Điểm: %-3d | Thắng: %-2d | Hòa: %-2d | Thua: %-2d | GD: %+d%n",
                    champion.points, champion.won, champion.drawn, champion.lost,
                    champion.getGoalDifference());
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        }
    }

    /**
     * In banner khởi động của ứng dụng.
     */
    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                              ║");
        System.out.println("║        ⚽  FOOTBALL TOURNAMENT SIMULATION ENGINE  ⚽                        ║");
        System.out.println("║                     Premier League Edition                                  ║");
        System.out.println("║                                                                              ║");
        System.out.println("║   Engine: Round-Robin Scheduler | Match Simulation | Observer Pattern       ║");
        System.out.println("║   Design: Strategy Pattern (Tactics) | Event-Driven Standings               ║");
        System.out.println("║                                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
