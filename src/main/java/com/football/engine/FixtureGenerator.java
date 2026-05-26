package com.football.engine;

import com.football.model.Match;
import com.football.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Bộ tạo lịch thi đấu sử dụng thuật toán Round-Robin (Circle Method).
 *
 * Thuật toán:
 *  - Với N đội, tạo ra (N-1) vòng đấu lượt đi, mỗi vòng có N/2 trận.
 *  - Một đội bị cố định (anchor), các đội còn lại xoay vòng.
 *  - Lượt về: hoán đổi sân nhà/sân khách (thêm (N-1) vòng nữa).
 *  - Tổng: 2*(N-1) vòng, N*(N-1) trận.
 *
 * Với Premier League (20 đội):
 *  - 38 vòng đấu, 380 trận
 *  - Độ phức tạp: O(N²)
 *
 * Ràng buộc đảm bảo:
 *  - Mỗi cặp đội đấu đúng 2 lần (Home/Away)
 *  - Trong một vòng, mỗi đội xuất hiện đúng 1 lần
 */
public class FixtureGenerator {

    /**
     * Tạo toàn bộ lịch thi đấu cho danh sách các đội bóng.
     *
     * @param teams Danh sách các đội tham dự giải
     * @return fixtures[i] = danh sách trận đấu của vòng (i+1)
     * @throws IllegalArgumentException nếu số đội là lẻ hoặc < 2
     */
    public List<List<Match>> generate(List<Team> teams) {
        int n = teams.size();

        if (n < 2) {
            throw new IllegalArgumentException("Giải đấu cần ít nhất 2 đội.");
        }

        // Nếu số đội lẻ, thêm đội "Bye" ảo để thuật toán hoạt động
        List<Team> teamList = new ArrayList<>(teams);
        boolean hasBye = false;
        if (n % 2 != 0) {
            teamList.add(null); // null = đội "Bye"
            hasBye = true;
            n++;
        }

        List<List<Match>> allFixtures = new ArrayList<>();

        // === LƯỢT ĐI ===
        List<List<Match>> firstLeg = generateLeg(teamList, n, false);
        allFixtures.addAll(firstLeg);

        // === LƯỢT VỀ (hoán đổi sân nhà/khách) ===
        List<List<Match>> secondLeg = generateLeg(teamList, n, true);
        allFixtures.addAll(secondLeg);

        return allFixtures;
    }

    /**
     * Tạo một lượt (lượt đi hoặc lượt về) bằng Circle Method.
     *
     * @param teams     Danh sách đội (có thể có Bye)
     * @param n         Số đội (luôn chẵn)
     * @param swapHomes Nếu true, hoán đổi sân nhà/khách (lượt về)
     */
    private List<List<Match>> generateLeg(List<Team> teams, int n, boolean swapHomes) {
        List<List<Match>> leg = new ArrayList<>();

        // Tạo mảng xoay vòng (trừ phần tử đầu là anchor)
        List<Team> rotation = new ArrayList<>(teams.subList(1, n));
        Team anchor = teams.get(0); // Đội cố định (anchor)

        int matchweekOffset = swapHomes ? (n - 1) : 0;

        for (int round = 0; round < n - 1; round++) {
            List<Match> matchweekMatches = new ArrayList<>();
            int matchweek = round + 1 + matchweekOffset;

            // Trận đầu: anchor vs rotation[0]
            Team teamA = anchor;
            Team teamB = rotation.get(0);
            addMatchIfNotBye(matchweekMatches, teamA, teamB, swapHomes, matchweek);

            // Các trận còn lại: cặp đối xứng
            for (int i = 1; i < n / 2; i++) {
                teamA = rotation.get(i);
                teamB = rotation.get(n - 1 - i);
                addMatchIfNotBye(matchweekMatches, teamA, teamB, swapHomes, matchweek);
            }

            leg.add(matchweekMatches);

            // Xoay vòng danh sách (Circle Method): đưa phần tử cuối lên đầu
            rotation.add(0, rotation.remove(rotation.size() - 1));
        }

        return leg;
    }

    /**
     * Thêm một trận vào danh sách, bỏ qua trận có đội Bye (null).
     */
    private void addMatchIfNotBye(List<Match> matches, Team home, Team away,
                                   boolean swap, int matchweek) {
        if (home == null || away == null) {
            return; // Bỏ qua trận có đội Bye
        }

        if (swap) {
            matches.add(new Match(away, home, matchweek)); // Lượt về: hoán đổi
        } else {
            matches.add(new Match(home, away, matchweek)); // Lượt đi: giữ nguyên
        }
    }

    /**
     * Xác nhận tính hợp lệ của lịch thi đấu:
     *  1. Tổng số trận = N * (N-1)
     *  2. Trong mỗi vòng, không đội nào xuất hiện > 1 lần
     *  3. Mỗi cặp đội đấu đúng 2 lần
     *
     * @return true nếu lịch hợp lệ
     */
    public boolean validate(List<List<Match>> fixtures, List<Team> teams) {
        int n = teams.size();
        int expectedTotalMatches = n * (n - 1);
        long actualTotal = fixtures.stream().mapToLong(List::size).sum();

        if (actualTotal != expectedTotalMatches) {
            System.err.printf("❌ Kiểm tra thất bại: Kỳ vọng %d trận, thực tế %d trận%n",
                    expectedTotalMatches, actualTotal);
            return false;
        }

        // Kiểm tra mỗi vòng không có đội đá 2 lần
        for (int i = 0; i < fixtures.size(); i++) {
            List<Match> round = fixtures.get(i);
            List<Team> teamsInRound = new ArrayList<>();
            for (Match m : round) {
                if (teamsInRound.contains(m.getHomeTeam()) || teamsInRound.contains(m.getAwayTeam())) {
                    System.err.printf("❌ Xung đột lịch thi đấu ở vòng %d: Đội đá 2 lần trong cùng vòng%n", i + 1);
                    return false;
                }
                teamsInRound.add(m.getHomeTeam());
                teamsInRound.add(m.getAwayTeam());
            }
        }

        System.out.printf("✅ Lịch thi đấu hợp lệ: %d vòng, %d trận, không xung đột%n",
                fixtures.size(), actualTotal);
        return true;
    }
}
