package com.football.model;

import com.football.observer.MatchObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thực thể đại diện cho một trận đấu bóng đá.
 *
 * Match đóng vai trò Subject trong Observer Pattern:
 *  - Lưu danh sách observers (LeagueTable, v.v.)
 *  - Phát thông báo khi có bàn thắng hoặc kết thúc trận
 *
 * Thuộc tính:
 *  - homeTeam / awayTeam: Đội nhà và đội khách
 *  - homeScore / awayScore: Tỷ số hiện tại
 *  - currentMinute: Đồng hồ trận đấu (0-90)
 *  - events: Nhật ký các sự kiện
 *  - matchweek: Vòng đấu thuộc về
 *  - played: Trạng thái đã thi đấu hay chưa
 */
public class Match {

    private final Team homeTeam;
    private final Team awayTeam;
    private int homeScore;
    private int awayScore;
    private int currentMinute;
    private boolean played;
    private int matchweek;

    /** Nhật ký sự kiện của trận đấu */
    private final List<MatchEvent> events;

    /** Danh sách observers (Observer Pattern) */
    private final List<MatchObserver> observers;

    public Match(Team homeTeam, Team awayTeam, int matchweek) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = 0;
        this.awayScore = 0;
        this.currentMinute = 0;
        this.played = false;
        this.matchweek = matchweek;
        this.events = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    // ==============================
    // Observer Pattern Methods
    // ==============================

    /**
     * Đăng ký một observer để nhận thông báo về sự kiện trận đấu.
     */
    public void addObserver(MatchObserver observer) {
        observers.add(observer);
    }

    /**
     * Hủy đăng ký observer.
     */
    public void removeObserver(MatchObserver observer) {
        observers.remove(observer);
    }

    /**
     * Phát thông báo "bàn thắng" đến tất cả observers.
     * Gọi sau mỗi lần ghi bàn.
     */
    private void notifyGoal(Team scoringTeam) {
        for (MatchObserver observer : observers) {
            observer.onGoal(this, scoringTeam);
        }
    }

    /**
     * Phát thông báo "kết thúc trận" đến tất cả observers.
     * Gọi khi trận đấu hoàn thành.
     */
    private void notifyMatchFinished() {
        for (MatchObserver observer : observers) {
            observer.onMatchFinished(this);
        }
    }

    // ==============================
    // Match Control Methods
    // ==============================

    /**
     * Ghi nhận bàn thắng cho đội nhà.
     */
    public void scoreHomeGoal(int minute) {
        homeScore++;
        MatchEvent event = new MatchEvent(minute, EventType.GOAL, homeTeam,
                "Bàn thắng! Tỷ số: " + homeScore + " - " + awayScore);
        events.add(event);
        notifyGoal(homeTeam);
    }

    /**
     * Ghi nhận bàn thắng cho đội khách.
     */
    public void scoreAwayGoal(int minute) {
        awayScore++;
        MatchEvent event = new MatchEvent(minute, EventType.GOAL, awayTeam,
                "Bàn thắng! Tỷ số: " + homeScore + " - " + awayScore);
        events.add(event);
        notifyGoal(awayTeam);
    }

    /**
     * Thêm sự kiện khác (thẻ phạt, chấn thương) vào nhật ký.
     */
    public void addEvent(MatchEvent event) {
        events.add(event);
    }

    /**
     * Đánh dấu trận đấu hoàn thành và thông báo cho observers.
     */
    public void finishMatch() {
        this.played = true;
        this.currentMinute = 90;
        notifyMatchFinished();
    }

    /**
     * Lấy kết quả trận đấu dưới dạng chuỗi mô tả.
     */
    public String getResultSummary() {
        if (!played) {
            return String.format("%-25s vs %-25s (Chưa thi đấu)", homeTeam.getName(), awayTeam.getName());
        }
        String result = homeScore > awayScore ? homeTeam.getShortName() + " thắng" :
                        awayScore > homeScore ? awayTeam.getShortName() + " thắng" : "Hòa";
        return String.format("%-25s %d - %d %-25s [%s]",
                homeTeam.getName(), homeScore, awayScore, awayTeam.getName(), result);
    }

    // ==============================
    // State Query Methods
    // ==============================

    /**
     * Kiểm tra đội nhà có đang dẫn bàn không.
     */
    public boolean isHomeTeamWinning() {
        return homeScore > awayScore;
    }

    /**
     * Kiểm tra đội khách có đang dẫn bàn không.
     */
    public boolean isAwayTeamWinning() {
        return awayScore > homeScore;
    }

    /**
     * Kiểm tra trận đang hòa.
     */
    public boolean isDrawing() {
        return homeScore == awayScore;
    }

    // --- Getters ---

    public Team getHomeTeam() { return homeTeam; }
    public Team getAwayTeam() { return awayTeam; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }
    public int getCurrentMinute() { return currentMinute; }
    public void setCurrentMinute(int minute) { this.currentMinute = minute; }
    public boolean isPlayed() { return played; }
    public int getMatchweek() { return matchweek; }
    public List<MatchEvent> getEvents() { return Collections.unmodifiableList(events); }

    @Override
    public String toString() {
        return getResultSummary();
    }
}
