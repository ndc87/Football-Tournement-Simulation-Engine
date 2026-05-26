package com.football.model;

/**
 * Đại diện cho một sự kiện xảy ra trong trận đấu.
 *
 * Mỗi MatchEvent ghi lại:
 *  - minute: Phút xảy ra sự kiện
 *  - type: Loại sự kiện (GOAL, YELLOW_CARD, RED_CARD, v.v.)
 *  - team: Đội bóng liên quan
 *  - description: Mô tả chi tiết sự kiện
 */
public class MatchEvent {

    private final int minute;
    private final EventType type;
    private final Team team;
    private final String description;

    public MatchEvent(int minute, EventType type, Team team, String description) {
        this.minute = minute;
        this.type = type;
        this.team = team;
        this.description = description;
    }

    public int getMinute() {
        return minute;
    }

    public EventType getType() {
        return type;
    }

    public Team getTeam() {
        return team;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        String icon = switch (type) {
            case GOAL -> "⚽";
            case YELLOW_CARD -> "🟨";
            case RED_CARD -> "🟥";
            case INJURY -> "🏥";
            case SUBSTITUTION -> "🔄";
        };
        return String.format("[%2d'] %s %s - %s", minute, icon, team.getShortName(), description);
    }
}
