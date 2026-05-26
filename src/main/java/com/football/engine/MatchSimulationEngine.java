package com.football.engine;

import com.football.model.*;
import com.football.tactic.AllOutAttackTactic;
import com.football.tactic.Tactic;

import java.util.List;
import java.util.Random;

/**
 * Engine mô phỏng trận đấu bóng đá — trái tim của hệ thống.
 *
 * Mô hình: "Time-step Simulation" với 9 nhịp × 10 phút/nhịp = 90 phút.
 *
 * Thuật toán mô phỏng mỗi nhịp:
 *  1. Kiểm tra & tự động chuyển chiến thuật (Strategy Pattern, phút 70)
 *  2. Tính xác suất ghi bàn cho mỗi đội: P(Goal) = f(Attack, Defense, Stamina, Tactic, Random)
 *  3. Tung xúc xắc xác suất để quyết định có ghi bàn không
 *  4. Tính xác suất xảy ra sự kiện phụ (thẻ phạt, chấn thương)
 *  5. Giảm stamina toàn đội
 *
 * Công thức xác suất ghi bàn:
 *  P(goal) = BASE_PROB × (attack × tacticMult / (defense × defMult)) × performanceFactor × randomFactor
 *
 *  Trong đó:
 *  - BASE_PROB = 0.25 (xác suất cơ sở mỗi nhịp 10 phút)
 *  - performanceFactor = trung bình hiệu suất toàn đội (dựa trên stamina & form)
 *  - randomFactor = nhiễu ngẫu nhiên [0.7, 1.3] (mô phỏng tình huống bất ngờ)
 *  - Xác suất tối đa mỗi nhịp bị giới hạn ở 70%
 */
public class MatchSimulationEngine {

    private static final int TOTAL_TICKS = 9;          // 9 nhịp × 10 phút = 90 phút
    private static final int MINUTES_PER_TICK = 10;    // Mỗi nhịp = 10 phút
    private static final double BASE_GOAL_PROB = 0.18; // Xác suất cơ sở ghi bàn mỗi nhịp
    private static final double MAX_GOAL_PROB = 0.55;  // Giới hạn trên xác suất ghi bàn
    private static final int TACTICAL_SWITCH_MINUTE = 70; // Phút chuyển chiến thuật

    // Xác suất sự kiện phụ mỗi nhịp
    private static final double YELLOW_CARD_PROB = 0.10;
    private static final double INJURY_PROB = 0.03;

    private final Random random;
    private boolean verbose; // Nếu true, in log chi tiết từng nhịp

    public MatchSimulationEngine() {
        this.random = new Random();
        this.verbose = true;
    }

    public MatchSimulationEngine(long seed) {
        this.random = new Random(seed);
        this.verbose = true;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Mô phỏng một trận đấu hoàn chỉnh (90 phút).
     *
     * @param match Trận đấu cần mô phỏng (phải chưa được thi đấu)
     */
    public void simulate(Match match) {
        if (match.isPlayed()) {
            throw new IllegalStateException("Trận đấu đã được thi đấu: " + match);
        }

        Team home = match.getHomeTeam();
        Team away = match.getAwayTeam();

        if (verbose) {
            System.out.println();
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.printf("  ⚽  KICKOFF: %-20s  vs  %-20s  (Vòng %d)%n",
                    home.getName(), away.getName(), match.getMatchweek());
            System.out.printf("       ATK: %d | DEF: %d  ——  ATK: %d | DEF: %d%n",
                    home.getAttackRating(), home.getDefenseRating(),
                    away.getAttackRating(), away.getDefenseRating());
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }

        // Vòng lặp mô phỏng chính: 9 nhịp × 10 phút
        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            int currentMinute = tick * MINUTES_PER_TICK;
            match.setCurrentMinute(currentMinute);

            // === Bước 1: Kiểm tra & chuyển chiến thuật (phút 70) ===
            if (currentMinute >= TACTICAL_SWITCH_MINUTE) {
                applyTacticalSwitch(match, home, away, currentMinute);
            }

            // === Bước 2: Mô phỏng cơ hội ghi bàn cho đội nhà ===
            simulateGoalChance(match, home, away, currentMinute, true);

            // === Bước 3: Mô phỏng cơ hội ghi bàn cho đội khách ===
            simulateGoalChance(match, away, home, currentMinute, false);

            // === Bước 4: Mô phỏng sự kiện phụ (thẻ phạt, chấn thương) ===
            simulateSideEvents(match, home, away, currentMinute);

            // === Bước 5: Giảm stamina toàn đội ===
            home.drainAllPlayersStamina();
            away.drainAllPlayersStamina();

            if (verbose) {
                System.out.printf("  [%2d'] Tỷ số: %-20s %d - %d %-20s%n",
                        currentMinute, home.getShortName(), match.getHomeScore(),
                        match.getAwayScore(), away.getShortName());
            }
        }

        // Kết thúc trận — thông báo cho tất cả observers (LeagueTable)
        match.finishMatch();

        if (verbose) {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.printf("  🏁 KẾT QUẢ: %s%n", match.getResultSummary());
            printMatchEvents(match);
            System.out.println();
        }
    }

    /**
     * Mô phỏng toàn bộ một vòng đấu (Matchweek).
     *
     * @param matchweekMatches Danh sách trận đấu trong vòng
     */
    public void simulateMatchweek(List<Match> matchweekMatches) {
        if (matchweekMatches.isEmpty()) return;

        int matchweek = matchweekMatches.get(0).getMatchweek();
        System.out.println();
        System.out.printf("╔══════════════════════════════════════════════════════════╗%n");
        System.out.printf("║              🗓️  VÒNG ĐẤU %2d                            ║%n", matchweek);
        System.out.printf("╚══════════════════════════════════════════════════════════╝%n");

        for (Match match : matchweekMatches) {
            simulate(match);
        }
    }

    // ==============================
    // Private Helper Methods
    // ==============================

    /**
     * Kiểm tra và áp dụng chuyển chiến thuật tự động ở phút 70.
     *
     * Logic Strategy Pattern:
     *  - Nếu đội đang thua (losing) và chưa dùng AllOutAttackTactic
     *    → tự động chuyển sang AllOutAttackTactic
     */
    private void applyTacticalSwitch(Match match, Team home, Team away, int minute) {
        // Đội nhà đang thua
        if (match.isAwayTeamWinning()
                && !(home.getCurrentTactic() instanceof AllOutAttackTactic)) {
            home.setCurrentTactic(new AllOutAttackTactic());
            if (verbose) {
                System.out.printf("  [%2d'] 🔄 %s chuyển sang chiến thuật: %s%n",
                        minute, home.getName(), home.getCurrentTactic().getName());
            }
        }

        // Đội khách đang thua
        if (match.isHomeTeamWinning()
                && !(away.getCurrentTactic() instanceof AllOutAttackTactic)) {
            away.setCurrentTactic(new AllOutAttackTactic());
            if (verbose) {
                System.out.printf("  [%2d'] 🔄 %s chuyển sang chiến thuật: %s%n",
                        minute, away.getName(), away.getCurrentTactic().getName());
            }
        }
    }

    /**
     * Tính xác suất và mô phỏng cơ hội ghi bàn cho đội tấn công.
     *
     * Công thức:
     *  P(goal) = BASE_PROB × (ATK_attacker × ATK_mult / DEF_defender × DEF_mult)
     *            × performanceFactor × randomFactor
     *
     * @param match       Trận đấu
     * @param attacker    Đội đang tấn công
     * @param defender    Đội đang phòng ngự
     * @param minute      Phút hiện tại
     * @param isHomeTeam  Đội tấn công có phải đội nhà không (để gọi đúng method)
     */
    private void simulateGoalChance(Match match, Team attacker, Team defender,
                                     int minute, boolean isHomeTeam) {
        Tactic atkTactic = attacker.getCurrentTactic();
        Tactic defTactic = defender.getCurrentTactic();

        double goalProbability;

        if (attacker.hasRealXG() && defender.hasRealXGA()) {
            // ================================================================
            // CHẾ ĐỘ XG THỰC TẾ (Kaggle Data) — Ưu tiên khi có dữ liệu
            // ================================================================
            // xGPerGame = xG mỗi 90 phút → chia 9 ticks = xG mỗi 10 phút
            double attackXGPerTick  = attacker.getXGPerGame()  / TOTAL_TICKS;
            double defenseXGAPerTick = defender.getXGAPerGame() / TOTAL_TICKS;

            // Tính xác suất trung bình giữa khả năng tạo cơ hội của đội tấn công
            // và khả năng nhận bàn của đội phòng thủ
            double baseXGProb = (attackXGPerTick + defenseXGAPerTick) / 2.0;

            // Áp dụng hệ số chiến thuật
            double tacticMult = atkTactic.getAttackMultiplier() * defTactic.getDefenseMultiplier();

            // Hệ số ngẫu nhiên [0.6, 1.4]
            double randomFactor = 0.6 + random.nextDouble() * 0.8;

            goalProbability = baseXGProb * tacticMult * randomFactor;

        } else {
            // ================================================================
            // CHẾ ĐỘ THỐNG KÊ (fallback khi không có dữ liệu Kaggle)
            // ================================================================
            double attackStrength = attacker.getAttackRating() * atkTactic.getAttackMultiplier();
            double defenseStrength = defender.getDefenseRating() * defTactic.getDefenseMultiplier();

            double strengthRatio = attackStrength / Math.max(defenseStrength, 1.0);
            strengthRatio = Math.sqrt(strengthRatio);

            double performanceFactor = Math.max(0.4, attacker.getAveragePerformanceFactor());
            double randomFactor = 0.5 + random.nextDouble() * 1.3;

            goalProbability = BASE_GOAL_PROB * strengthRatio * performanceFactor * randomFactor;
        }

        // Giới hạn xác suất tối đa
        goalProbability = Math.min(goalProbability, MAX_GOAL_PROB);

        // Tung xúc xắc
        if (random.nextDouble() < goalProbability) {
            if (isHomeTeam) {
                match.scoreHomeGoal(minute);
            } else {
                match.scoreAwayGoal(minute);
            }
            if (verbose) {
                String mode = attacker.hasRealXG() ? "xG" : "stat";
                System.out.printf("  [%2d'] ⚽ GOAL! %s ghi bàn! (P=%.1f%% [%s])%n",
                        minute, attacker.getShortName(), goalProbability * 100, mode);
            }
        }
    }

    /**
     * Mô phỏng các sự kiện phụ: thẻ vàng, chấn thương.
     */
    private void simulateSideEvents(Match match, Team home, Team away, int minute) {
        // Thẻ vàng đội nhà
        if (random.nextDouble() < YELLOW_CARD_PROB) {
            MatchEvent event = new MatchEvent(minute, EventType.YELLOW_CARD, home,
                    "Thẻ vàng cho " + home.getShortName());
            match.addEvent(event);
        }

        // Thẻ vàng đội khách
        if (random.nextDouble() < YELLOW_CARD_PROB) {
            MatchEvent event = new MatchEvent(minute, EventType.YELLOW_CARD, away,
                    "Thẻ vàng cho " + away.getShortName());
            match.addEvent(event);
        }

        // Chấn thương (ít phổ biến hơn)
        if (random.nextDouble() < INJURY_PROB) {
            Team injuredTeam = random.nextBoolean() ? home : away;
            MatchEvent event = new MatchEvent(minute, EventType.INJURY, injuredTeam,
                    "Chấn thương cầu thủ " + injuredTeam.getShortName());
            match.addEvent(event);
        }
    }

    /**
     * In nhật ký sự kiện của trận đấu.
     */
    private void printMatchEvents(Match match) {
        List<MatchEvent> goals = match.getEvents().stream()
                .filter(e -> e.getType() == EventType.GOAL)
                .toList();

        if (!goals.isEmpty()) {
            System.out.println("  Bàn thắng:");
            goals.forEach(e -> System.out.println("    " + e));
        }
    }
}
