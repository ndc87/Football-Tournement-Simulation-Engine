package com.football.model;

import com.football.observer.LeagueTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thực thể đại diện cho toàn bộ giải đấu bóng đá.
 *
 * Tournament quản lý:
 *  - Danh sách các đội tham dự
 *  - Lịch thi đấu (Fixtures) được tổ chức theo từng vòng (Matchweek)
 *  - Bảng tổng sắp (LeagueTable)
 */
public class Tournament {

    private final String name;
    private final List<Team> teams;
    private final List<List<Match>> fixtures; // fixtures[week] = danh sách trận của vòng đó
    private final LeagueTable leagueTable;
    private int currentMatchweek;

    public Tournament(String name) {
        this.name = name;
        this.teams = new ArrayList<>();
        this.fixtures = new ArrayList<>();
        this.leagueTable = new LeagueTable();
        this.currentMatchweek = 0;
    }

    /**
     * Thêm đội bóng vào giải đấu.
     */
    public void addTeam(Team team) {
        teams.add(team);
        leagueTable.registerTeam(team);
    }

    /**
     * Thiết lập lịch thi đấu đã được tạo bởi FixtureGenerator.
     * Đồng thời đăng ký LeagueTable làm observer cho mỗi trận đấu.
     */
    public void setFixtures(List<List<Match>> fixtures) {
        this.fixtures.clear();
        this.fixtures.addAll(fixtures);

        // Đăng ký LeagueTable là observer cho tất cả các trận
        for (List<Match> matchweekMatches : fixtures) {
            for (Match match : matchweekMatches) {
                match.addObserver(leagueTable);
            }
        }
    }

    /**
     * Lấy danh sách trận đấu của một vòng cụ thể (1-indexed).
     */
    public List<Match> getMatchweekFixtures(int matchweek) {
        if (matchweek < 1 || matchweek > fixtures.size()) {
            throw new IllegalArgumentException("Vòng đấu không hợp lệ: " + matchweek);
        }
        return Collections.unmodifiableList(fixtures.get(matchweek - 1));
    }

    /**
     * Tổng số vòng đấu trong giải.
     */
    public int getTotalMatchweeks() {
        return fixtures.size();
    }

    /**
     * Tổng số trận đấu trong giải.
     */
    public long getTotalMatches() {
        return fixtures.stream().mapToLong(List::size).sum();
    }

    // --- Getters ---

    public String getName() { return name; }
    public List<Team> getTeams() { return Collections.unmodifiableList(teams); }
    public List<List<Match>> getAllFixtures() { return Collections.unmodifiableList(fixtures); }
    public LeagueTable getLeagueTable() { return leagueTable; }
    public int getCurrentMatchweek() { return currentMatchweek; }
    public void setCurrentMatchweek(int matchweek) { this.currentMatchweek = matchweek; }

    @Override
    public String toString() {
        return String.format("Tournament{name='%s', teams=%d, matchweeks=%d, totalMatches=%d}",
                name, teams.size(), getTotalMatchweeks(), getTotalMatches());
    }
}
