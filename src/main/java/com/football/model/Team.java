package com.football.model;

import com.football.tactic.Tactic;
import com.football.tactic.BalancedTactic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thực thể đại diện cho một đội bóng đá.
 *
 * Thuộc tính:
 *  - name: Tên đội bóng
 *  - players: Danh sách cầu thủ
 *  - attackRating: Chỉ số tấn công tổng hợp (1-100)
 *  - defenseRating: Chỉ số phòng ngự tổng hợp (1-100)
 *  - midfieldRating: Chỉ số tiền vệ tổng hợp (1-100)
 *  - currentTactic: Chiến thuật hiện hành (Strategy Pattern)
 */
public class Team {

    private final String name;
    private final String shortName;
    private final List<Player> players;
    private int attackRating;
    private int defenseRating;
    private int midfieldRating;
    private Tactic currentTactic;

    /** Chỉ số xG thực tế từ Kaggle dataset (Expected Goals per game) */
    private double xGPerGame = -1.0;   // -1 = chưa có dữ liệu thực
    private double xGAPerGame = -1.0;  // -1 = chưa có dữ liệu thực
    private double possession = -1.0;  // % kiểm soát bóng thực tế
    private int yellowCardsPerGame = -1; // thẻ vàng trung bình

    public Team(String name, String shortName, int attackRating, int defenseRating, int midfieldRating) {
        validateRating(attackRating, "attackRating");
        validateRating(defenseRating, "defenseRating");
        validateRating(midfieldRating, "midfieldRating");

        this.name = name;
        this.shortName = shortName;
        this.attackRating = attackRating;
        this.defenseRating = defenseRating;
        this.midfieldRating = midfieldRating;
        this.players = new ArrayList<>();
        this.currentTactic = new BalancedTactic(); // Mặc định dùng chiến thuật cân bằng
    }

    private void validateRating(int rating, String fieldName) {
        if (rating < 1 || rating > 100) {
            throw new IllegalArgumentException(fieldName + " phải trong khoảng [1, 100]. Giá trị: " + rating);
        }
    }

    /**
     * Tính sức mạnh tổng hợp của đội (Overall Rating).
     * Trọng số: Attack 40%, Defense 35%, Midfield 25%
     */
    public double getOverallRating() {
        return attackRating * 0.40 + defenseRating * 0.35 + midfieldRating * 0.25;
    }

    /**
     * Tính hệ số phong độ trung bình của toàn đội dựa trên hiệu suất cầu thủ.
     * Nếu không có cầu thủ, trả về form mặc định 0.75.
     */
    public double getAveragePerformanceFactor() {
        if (players.isEmpty()) {
            return 0.75;
        }
        return players.stream()
                .mapToDouble(Player::getPerformanceFactor)
                .average()
                .orElse(0.75);
    }

    /**
     * Giảm stamina của toàn bộ cầu thủ sau mỗi nhịp mô phỏng.
     */
    public void drainAllPlayersStamina() {
        players.forEach(Player::drainStamina);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    // --- Getters & Setters ---

    public String getName() { return name; }
    public String getShortName() { return shortName; }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public int getAttackRating() { return attackRating; }
    public int getDefenseRating() { return defenseRating; }
    public int getMidfieldRating() { return midfieldRating; }
    public Tactic getCurrentTactic() { return currentTactic; }
    public void setCurrentTactic(Tactic tactic) { this.currentTactic = tactic; }

    public double getXGPerGame() { return xGPerGame; }
    public void setXGPerGame(double xGPerGame) { this.xGPerGame = xGPerGame; }
    public boolean hasRealXG() { return xGPerGame > 0; }

    public double getXGAPerGame() { return xGAPerGame; }
    public void setXGAPerGame(double xGAPerGame) { this.xGAPerGame = xGAPerGame; }
    public boolean hasRealXGA() { return xGAPerGame > 0; }

    public double getPossession() { return possession; }
    public void setPossession(double possession) { this.possession = possession; }

    public int getYellowCardsPerGame() { return yellowCardsPerGame; }
    public void setYellowCardsPerGame(int yellowCardsPerGame) { this.yellowCardsPerGame = yellowCardsPerGame; }

    @Override
    public String toString() {
        return String.format("Team{name='%s', ATK=%d, DEF=%d, MID=%d, overall=%.1f}",
                name, attackRating, defenseRating, midfieldRating, getOverallRating());
    }
}
