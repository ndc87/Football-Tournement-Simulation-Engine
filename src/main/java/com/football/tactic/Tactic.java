package com.football.tactic;

/**
 * Interface định nghĩa chiến thuật thi đấu (Strategy Pattern).
 *
 * Mục đích: Tách rời logic chiến thuật khỏi lớp Match.
 * Cho phép thay đổi chiến thuật tại thời điểm chạy (Runtime).
 *
 * Các triển khai:
 *  - BalancedTactic: Chiến thuật cân bằng (mặc định)
 *  - AllOutAttackTactic: Tấn công toàn lực (khi thua bàn)
 */
public interface Tactic {

    /**
     * Tên của chiến thuật.
     */
    String getName();

    /**
     * Hệ số nhân cho chỉ số tấn công.
     * Giá trị > 1.0 tăng cường tấn công; < 1.0 giảm tấn công.
     *
     * @return hệ số tấn công (attackMultiplier)
     */
    double getAttackMultiplier();

    /**
     * Hệ số nhân cho chỉ số phòng ngự.
     * Giá trị > 1.0 tăng cường phòng ngự; < 1.0 giảm phòng ngự.
     *
     * @return hệ số phòng ngự (defenseMultiplier)
     */
    double getDefenseMultiplier();

    /**
     * Mô tả ngắn về chiến thuật này.
     */
    String getDescription();
}
