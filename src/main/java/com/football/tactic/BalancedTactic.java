package com.football.tactic;

/**
 * Chiến thuật cân bằng — mặc định cho mọi đội bóng.
 *
 * Triết lý: Duy trì cân bằng giữa tấn công và phòng ngự,
 * không thiên vị cực đoan về bên nào.
 *
 * Hệ số: Attack x1.0, Defense x1.0 (không có bonus)
 */
public class BalancedTactic implements Tactic {

    @Override
    public String getName() {
        return "Balanced (4-4-2)";
    }

    @Override
    public double getAttackMultiplier() {
        return 1.0;
    }

    @Override
    public double getDefenseMultiplier() {
        return 1.0;
    }

    @Override
    public String getDescription() {
        return "Chiến thuật cân bằng: duy trì đội hình chuẩn, không có điều chỉnh đặc biệt.";
    }

    @Override
    public String toString() {
        return String.format("Tactic[%s | ATK×%.1f | DEF×%.1f]",
                getName(), getAttackMultiplier(), getDefenseMultiplier());
    }
}
