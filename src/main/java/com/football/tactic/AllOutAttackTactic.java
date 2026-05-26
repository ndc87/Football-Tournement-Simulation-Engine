package com.football.tactic;

/**
 * Chiến thuật tấn công toàn lực — kích hoạt tự động ở phút 70
 * khi đội bóng đang trong trạng thái bị dẫn bàn.
 *
 * Triết lý: Dồn toàn bộ lực lượng lên phía trước để tìm bàn gỡ.
 * Đánh đổi: Tăng mạnh tấn công (+35%) nhưng suy yếu phòng ngự (-20%).
 *
 * Hệ số: Attack x1.35, Defense x0.80
 */
public class AllOutAttackTactic implements Tactic {

    @Override
    public String getName() {
        return "All-Out Attack (4-2-4)";
    }

    @Override
    public double getAttackMultiplier() {
        return 1.35; // +35% tăng cường tấn công
    }

    @Override
    public double getDefenseMultiplier() {
        return 0.80; // -20% phòng ngự bị suy yếu
    }

    @Override
    public String getDescription() {
        return "Chiến thuật tấn công toàn lực: tăng xác suất ghi bàn +35%, " +
               "nhưng phòng ngự yếu hơn -20%. Kích hoạt khi bị dẫn sau phút 70.";
    }

    @Override
    public String toString() {
        return String.format("Tactic[%s | ATK×%.2f | DEF×%.2f]",
                getName(), getAttackMultiplier(), getDefenseMultiplier());
    }
}
