# Backend Specifications — Football Tournament Simulation Engine

## 1. DataSeeder Class

### Mục đích
`DataSeeder` là lớp chịu trách nhiệm tải và khởi tạo dữ liệu đội bóng từ file JSON vào bộ nhớ.

### Luồng xử lý

```
teams.json (Classpath)
       │
       ▼
DataSeeder.loadTeams()
       │
       ├─► ObjectMapper.readTree()  ──► JsonNode (root)
       │
       ├─► parseTeam(teamNode)      ──► Team object
       │       └─► parsePlayer(playerNode) ──► Player object
       │
       └─► List<Team>
```

### Xử lý lỗi (Edge Cases)
- **File không tìm thấy**: Ném `IOException` với thông báo rõ ràng về đường dẫn
- **JSON sai định dạng**: Jackson ném `IOException` với vị trí lỗi trong file
- **Thiếu field bắt buộc** (`name`, `attackRating`, v.v.): Ném `IllegalArgumentException`
- **Rating ngoài khoảng [1, 100]**: Team constructor validate và ném `IllegalArgumentException`
- **Position không hợp lệ**: Ném `IllegalArgumentException` với danh sách các giá trị hợp lệ
- **Player bị lỗi**: Log cảnh báo và bỏ qua, không dừng toàn bộ quá trình tải

### Cấu trúc teams.json

```json
{
  "tournament": "Premier League 2024/25",
  "teams": [
    {
      "name": "Manchester City",
      "shortName": "MCI",
      "attackRating": 92,
      "defenseRating": 85,
      "midfieldRating": 91,
      "players": [
        {
          "name": "Erling Haaland",
          "position": "FORWARD",
          "stamina": 100,
          "form": 0.97
        }
      ]
    }
  ]
}
```

---

## 2. Round-Robin Algorithm (FixtureGenerator)

### Mô tả
Triển khai thuật toán **Circle Method** để tạo lịch thi đấu Round-Robin cho N đội.

### Pseudo-code

```
FUNCTION GenerateFixtures(teams[N]):
    IF N is odd:
        teams.append(null)  // Thêm đội Bye ảo
        N = N + 1
    
    fixtures = []
    
    // LƯỢT ĐI (N-1 vòng)
    rotation = teams[1..N-1]  // Tất cả trừ anchor
    anchor = teams[0]
    
    FOR round = 1 TO N-1:
        matchweek = []
        
        // Trận anchor vs rotation[0]
        IF anchor != null AND rotation[0] != null:
            matchweek.add(Match(anchor, rotation[0], round))
        
        // Các cặp đối xứng
        FOR i = 1 TO N/2 - 1:
            teamA = rotation[i]
            teamB = rotation[N-2-i]
            IF teamA != null AND teamB != null:
                matchweek.add(Match(teamA, teamB, round))
        
        fixtures.add(matchweek)
        rotation = [rotation.last()] + rotation[0..N-3]  // Xoay vòng
    
    // LƯỢT VỀ (hoán đổi sân nhà/khách)
    FOR each matchweek in first_leg:
        return_week = []
        FOR each match in matchweek:
            return_week.add(Match(match.away, match.home, matchweek + N-1))
        fixtures.add(return_week)
    
    RETURN fixtures

// Độ phức tạp: O(N²)
// Kết quả: 2*(N-1) vòng, N*(N-1) trận
// Với N=20: 38 vòng, 380 trận
```

### Kiểm chứng (Validation)
Sau khi tạo lịch, hệ thống kiểm tra:
1. **Tổng số trận**: Phải bằng N*(N-1) = 380
2. **Không xung đột**: Mỗi vòng, mỗi đội xuất hiện đúng 1 lần
3. **Tính đối xứng**: Mỗi cặp đội đấu đúng 2 lần (Home & Away)

---

## 3. Match Simulation Engine

### Mô hình Time-step

Mỗi trận đấu được chia thành **9 nhịp × 10 phút = 90 phút**.

```
Minute:  10  20  30  40  50  60  70  80  90
Tick:     1   2   3   4   5   6   7   8   9
```

### Công thức xác suất ghi bàn

```
P(goal) = BASE_PROB × StrengthRatio × PerformanceFactor × RandomFactor

Trong đó:
  BASE_PROB       = 0.25  (xác suất cơ sở mỗi nhịp)
  StrengthRatio   = (Attack_A × TacticMult_A) / (Defense_B × DefMult_B)
  PerformanceFactor = avg(player.stamina/100 × player.form)  cho toàn đội
  RandomFactor    = Uniform[0.7, 1.3]  (tình huống bất ngờ)

  P(goal) bị giới hạn tối đa tại MAX_GOAL_PROB = 0.70
```

### Ví dụ tính toán

Đội A (Man City): ATK=92, Tactic=Balanced (×1.0)
Đội B (Luton):    DEF=59, Tactic=Balanced (×1.0)
PerformanceFactor = 0.85
RandomFactor = 1.1

```
StrengthRatio   = (92 × 1.0) / (59 × 1.0) = 1.559
P(goal)         = 0.25 × 1.559 × 0.85 × 1.1 = 0.364 = 36.4%
```

### Auto Tactical Switch (Strategy Pattern)

```
IF currentMinute >= 70:
    IF homeTeam is LOSING:
        IF homeTeam.tactic != AllOutAttackTactic:
            homeTeam.setTactic(new AllOutAttackTactic())
            // ATK ×1.35, DEF ×0.80
    IF awayTeam is LOSING:
        IF awayTeam.tactic != AllOutAttackTactic:
            awayTeam.setTactic(new AllOutAttackTactic())
```

### Stamina Degradation

Mỗi nhịp 10 phút, toàn bộ cầu thủ mất **5 đơn vị stamina** (từ 100 xuống 50 sau 90 phút).

```
stamina_new = max(0, stamina_old - 5.0)
PerformanceFactor = form × (stamina / 100)
```

---

## 4. League Table — Observer Pattern

### Cơ chế

```
Match (Subject)         LeagueTable (Observer)
─────────────────       ─────────────────────────
scoreHomeGoal()    ──► onGoal()           [không thao tác điểm]
scoreAwayGoal()    ──► onGoal()           [không thao tác điểm]
finishMatch()      ──► onMatchFinished()  [cập nhật điểm + stats]
```

### Tie-breaking Rules (Quy tắc phân hạng khi bằng điểm)

```
1. Points (Điểm)           ──► cao hơn = xếp trên
2. Goal Difference (Hiệu số) ──► cao hơn = xếp trên
3. Goals Scored (Bàn thắng)  ──► nhiều hơn = xếp trên
4. Team Name (Tên đội)       ──► A-Z (tiebreaker cuối)
```

### Bảng điểm

| Kết quả | Điểm |
|---------|------|
| Thắng   | 3    |
| Hòa     | 1    |
| Thua    | 0    |
