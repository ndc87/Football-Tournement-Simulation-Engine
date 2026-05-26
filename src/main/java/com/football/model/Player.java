package com.football.model;

/**
 * Thực thể đại diện cho một cầu thủ bóng đá.
 *
 * Thuộc tính:
 *  - name: Tên cầu thủ
 *  - position: Vị trí thi đấu (GK, DEF, MID, FWD)
 *  - stamina: Chỉ số thể lực (0-100), giảm dần qua mỗi nhịp mô phỏng
 *  - form: Hệ số phong độ (0.0 - 1.0), ảnh hưởng đến hiệu suất
 */
public class Player {

    private final String name;
    private final Position position;
    private double stamina;
    private double form;

    /** Stamina tối đa ban đầu */
    public static final double MAX_STAMINA = 100.0;

    /** Lượng stamina giảm mỗi nhịp 10 phút */
    public static final double STAMINA_DRAIN_PER_TICK = 5.0;

    public Player(String name, Position position, double stamina, double form) {
        if (stamina < 0 || stamina > MAX_STAMINA) {
            throw new IllegalArgumentException("Stamina phải trong khoảng [0, 100]. Giá trị: " + stamina);
        }
        if (form < 0.0 || form > 1.0) {
            throw new IllegalArgumentException("Form phải trong khoảng [0.0, 1.0]. Giá trị: " + form);
        }
        this.name = name;
        this.position = position;
        this.stamina = stamina;
        this.form = form;
    }

    /**
     * Giảm stamina sau mỗi nhịp mô phỏng (10 phút).
     * Stamina không bao giờ giảm xuống dưới 0.
     */
    public void drainStamina() {
        this.stamina = Math.max(0, this.stamina - STAMINA_DRAIN_PER_TICK);
    }

    /**
     * Tính hệ số hiệu suất của cầu thủ dựa trên stamina và form hiện tại.
     * Khi stamina giảm, hiệu suất giảm theo.
     *
     * @return hệ số hiệu suất trong khoảng [0.0, 1.0]
     */
    public double getPerformanceFactor() {
        double staminaFactor = stamina / MAX_STAMINA;
        return form * staminaFactor;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public double getStamina() {
        return stamina;
    }

    public double getForm() {
        return form;
    }

    public void setForm(double form) {
        this.form = Math.max(0.0, Math.min(1.0, form));
    }

    @Override
    public String toString() {
        return String.format("Player{name='%s', position=%s, stamina=%.1f, form=%.2f}",
                name, position, stamina, form);
    }
}
