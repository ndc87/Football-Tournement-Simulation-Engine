package com.football.observer;

import com.football.model.Match;
import com.football.model.Team;

import java.util.*;

/**
 * Bảng xếp hạng giải đấu — triển khai Observer Pattern.
 *
 * LeagueTable là Observer lắng nghe sự kiện từ Match (Subject):
 *  - onGoal: Không thực hiện thao tác (xử lý khi kết thúc trận)
 *  - onMatchFinished: Cập nhật ngay lập tức điểm, thắng/thua/hòa,
 *    hiệu số bàn thắng
 *
 * Quy tắc tính điểm:
 *  - Thắng: 3 điểm
 *  - Hòa:   1 điểm
 *  - Thua:  0 điểm
 *
 * Quy tắc xếp hạng (Tie-breaking rules):
 *  1. Điểm số (Points) — cao hơn xếp trên
 *  2. Hiệu số bàn thắng (Goal Difference) — cao hơn xếp trên
 *  3. Tổng bàn thắng (Goals Scored) — cao hơn xếp trên
 *  4. Tên đội (Alphabetical) — tiebreaker cuối cùng
 */
public class LeagueTable implements MatchObserver {

    /** Map từ đội bóng → hàng trong bảng xếp hạng */
    private final Map<Team, TeamStats> statsMap;

    public LeagueTable() {
        this.statsMap = new LinkedHashMap<>();
    }

    /**
     * Đăng ký đội bóng vào bảng xếp hạng khi bắt đầu giải đấu.
     */
    public void registerTeam(Team team) {
        statsMap.put(team, new TeamStats(team));
    }

    /**
     * Được gọi khi có bàn thắng — không thực hiện thao tác nào ở đây.
     * Tất cả cập nhật điểm được thực hiện trong onMatchFinished.
     */
    @Override
    public void onGoal(Match match, Team scoringTeam) {
        // Quan sát trực tiếp nhưng không cập nhật điểm trung gian
        // Điểm chỉ cộng chính xác sau khi trận kết thúc
    }

    /**
     * Được gọi ngay khi trận kết thúc.
     * Cập nhật điểm số (3/1/0) và thống kê cho cả hai đội.
     */
    @Override
    public void onMatchFinished(Match match) {
        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();
        int homeGoals = match.getHomeScore();
        int awayGoals = match.getAwayScore();

        TeamStats homeStats = statsMap.get(home);
        TeamStats awayStats = statsMap.get(away);

        if (homeStats == null || awayStats == null) {
            throw new IllegalStateException("Đội bóng chưa được đăng ký trong bảng xếp hạng: "
                    + (homeStats == null ? home.getName() : away.getName()));
        }

        // Cập nhật số trận đã đấu
        homeStats.played++;
        awayStats.played++;

        // Cập nhật bàn thắng/thua
        homeStats.goalsFor += homeGoals;
        homeStats.goalsAgainst += awayGoals;
        awayStats.goalsFor += awayGoals;
        awayStats.goalsAgainst += homeGoals;

        // Xác định kết quả và cập nhật điểm
        if (homeGoals > awayGoals) {
            // Đội nhà thắng
            homeStats.won++;
            homeStats.points += 3;
            awayStats.lost++;
        } else if (awayGoals > homeGoals) {
            // Đội khách thắng
            awayStats.won++;
            awayStats.points += 3;
            homeStats.lost++;
        } else {
            // Hòa
            homeStats.drawn++;
            homeStats.points += 1;
            awayStats.drawn++;
            awayStats.points += 1;
        }
    }

    /**
     * Lấy bảng xếp hạng đã được sắp xếp theo quy tắc tie-breaking.
     *
     * Thứ tự: Points → Goal Difference → Goals Scored → Team Name
     */
    public List<TeamStats> getSortedStandings() {
        List<TeamStats> standings = new ArrayList<>(statsMap.values());
        standings.sort(Comparator
                .comparingInt(TeamStats::getPoints).reversed()
                .thenComparingInt(TeamStats::getGoalDifference).reversed()
                .thenComparingInt(TeamStats::getGoalsFor).reversed()
                .thenComparing(s -> s.team.getName()));
        return standings;
    }

    /**
     * Lấy thống kê của một đội cụ thể.
     */
    public TeamStats getStatsForTeam(Team team) {
        return statsMap.get(team);
    }

    /**
     * In bảng xếp hạng ra console với định dạng đẹp.
     */
    public void printStandings() {
        List<TeamStats> standings = getSortedStandings();
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      🏆  BẢNG XẾP HẠNG PREMIER LEAGUE                      ║");
        System.out.println("╠═══╦══════════════════════════╦═══╦═══╦═══╦═══╦═════╦═════╦════╦════════════╣");
        System.out.println("║ # ║ Đội bóng                 ║ P ║ W ║ D ║ L ║  F  ║  A  ║ GD ║    PTS     ║");
        System.out.println("╠═══╬══════════════════════════╬═══╬═══╬═══╬═══╬═════╬═════╬════╬════════════╣");

        for (int i = 0; i < standings.size(); i++) {
            TeamStats s = standings.get(i);
            String rank = String.format("%2d", i + 1);
            String teamName = String.format("%-25s", s.team.getName());
            String marker = i < 4 ? "🔵" : i < 6 ? "🟠" : i > 16 ? "🔴" : "  ";
            System.out.printf("║ %s ║ %s║%3d║%3d║%3d║%3d║%5d║%5d║%4d║%10d  ║%n",
                    rank, teamName,
                    s.played, s.won, s.drawn, s.lost,
                    s.goalsFor, s.goalsAgainst, s.getGoalDifference(),
                    s.points);
        }
        System.out.println("╚═══╩══════════════════════════╩═══╩═══╩═══╩═══╩═════╩═════╩════╩════════════╝");
        System.out.println("  🔵 Champions League  🟠 Europa League  🔴 Relegation");
        System.out.println();
    }

    // ==============================
    // Inner Class: TeamStats
    // ==============================

    /**
     * Lưu trữ thống kê của một đội bóng trong bảng xếp hạng.
     */
    public static class TeamStats {
        public final Team team;
        public int played;
        public int won;
        public int drawn;
        public int lost;
        public int goalsFor;
        public int goalsAgainst;
        public int points;

        public TeamStats(Team team) {
            this.team = team;
        }

        public int getGoalDifference() {
            return goalsFor - goalsAgainst;
        }

        public int getGoalsFor() {
            return goalsFor;
        }

        public int getPoints() {
            return points;
        }

        @Override
        public String toString() {
            return String.format("%s | P=%d W=%d D=%d L=%d GF=%d GA=%d GD=%+d PTS=%d",
                    team.getName(), played, won, drawn, lost,
                    goalsFor, goalsAgainst, getGoalDifference(), points);
        }
    }
}
