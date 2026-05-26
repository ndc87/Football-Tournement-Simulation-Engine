package com.football.observer;

import com.football.model.Match;
import com.football.model.Team;

/**
 * Interface Observer trong Observer Pattern.
 *
 * Mục đích: Đảm bảo tính nhất quán dữ liệu giữa Match Engine
 * và League Table mà không gây ra phụ thuộc vòng (Circular Dependency).
 *
 * Subject: Match
 * Observer: LeagueTable (và bất kỳ observer nào khác muốn lắng nghe)
 */
public interface MatchObserver {

    /**
     * Được gọi ngay khi có bàn thắng trong trận đấu.
     *
     * @param match       Trận đấu vừa có bàn thắng
     * @param scoringTeam Đội bóng vừa ghi bàn
     */
    void onGoal(Match match, Team scoringTeam);

    /**
     * Được gọi ngay khi trận đấu kết thúc.
     * Observer cập nhật điểm số (3/1/0) và hiệu số bàn thắng.
     *
     * @param match Trận đấu vừa kết thúc
     */
    void onMatchFinished(Match match);
}
