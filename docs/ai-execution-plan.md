# AI Execution Plan — Danh sách Prompts Code-Gen

Tài liệu này cung cấp danh sách các Prompts để AI tự động sinh code từng module của dự án, kèm theo các edge-case đặc biệt cần chú ý.

---

## Module 1: Core Entities

### Prompt 1.1 — Player Entity
```
Tạo class Java "Player" với các thuộc tính: name (String), position (enum Position),
stamina (double, 0-100), form (double, 0.0-1.0).
Implement method drainStamina() giảm stamina đi 5.0 mỗi lần gọi, không xuống dưới 0.
Implement getPerformanceFactor() = form * (stamina/100).
Validate tất cả giá trị trong constructor.
```

**Edge-cases:**
- Stamina không bao giờ âm: `stamina = Math.max(0, stamina - drain)`
- Form ngoài [0,1]: clamp thay vì throw exception khi set
- Player với stamina=0 vẫn thi đấu được (performanceFactor tối thiểu = 0)

---

### Prompt 1.2 — Team Entity
```
Tạo class Java "Team" với: name, shortName, attackRating/defenseRating/midfieldRating (int 1-100),
danh sách Player, và currentTactic (Tactic interface — Strategy Pattern).
Method getOverallRating() = ATK*0.40 + DEF*0.35 + MID*0.25.
Method getAveragePerformanceFactor() lấy trung bình performanceFactor của tất cả cầu thủ.
Nếu không có cầu thủ, trả về giá trị mặc định 0.75.
```

**Edge-cases:**
- Đội không có cầu thủ: `players.isEmpty()` → trả về default 0.75 (không throw exception)
- Tactic null: set mặc định là BalancedTactic trong constructor
- Rating = 0: validate và throw `IllegalArgumentException` rõ ràng

---

### Prompt 1.3 — Match Entity (Subject)
```
Tạo class Java "Match" implement Observer Pattern (Subject).
Thuộc tính: homeTeam, awayTeam (Team), homeScore/awayScore (int), currentMinute,
played (boolean), danh sách MatchEvent, danh sách MatchObserver.
Methods: addObserver(), scoreHomeGoal(minute), scoreAwayGoal(minute), finishMatch().
finishMatch() phải gọi notifyMatchFinished() cho tất cả observers.
```

**Edge-cases:**
- Simulate match đã played=true → throw `IllegalStateException`
- Observer list rỗng → không crash, không thông báo
- Concurrent modification của observer list → dùng copy-on-notify pattern

---

## Module 2: Round-Robin Scheduler

### Prompt 2.1 — FixtureGenerator
```
Implement thuật toán Round-Robin (Circle Method) trong Java.
Input: List<Team> (N teams). Output: List<List<Match>> (2*(N-1) rounds).
- Nếu N lẻ, thêm đội Bye (null) để N trở thành chẵn.
- Một đội được cố định (anchor), N-1 đội còn lại xoay vòng mỗi vòng.
- Lượt về: hoán đổi home/away của lượt đi.
- Loại bỏ trận có đội Bye.
Implement validate() kiểm tra: tổng trận = N*(N-1), không đội nào đá 2 lần trong cùng vòng.
```

**Edge-cases:**
- N < 2 teams: throw `IllegalArgumentException`
- N = 2: 1 trận lượt đi, 1 trận lượt về (2 vòng, 2 trận)
- N lẻ: thêm Bye, các trận có Bye bị bỏ qua silently
- Validate phát hiện xung đột: in thông báo lỗi rõ ràng với số vòng bị xung đột

---

## Module 3: Simulation Engine

### Prompt 3.1 — Strategy Pattern (Tactics)
```
Tạo interface Tactic với methods: getName(), getAttackMultiplier(), getDefenseMultiplier(), getDescription().
Implement BalancedTactic: ATK=1.0, DEF=1.0 (default).
Implement AllOutAttackTactic: ATK=1.35, DEF=0.80 (kích hoạt khi thua sau phút 70).
```

**Edge-cases:**
- Multiplier = 0: Không cho phép (validate trong implementation)
- Tự động chuyển chiến thuật: chỉ chuyển 1 lần, không toggle back-and-forth

---

### Prompt 3.2 — MatchSimulationEngine
```
Implement Time-step simulation: 9 ticks × 10 minutes = 90 phút.
Mỗi tick:
  1. Kiểm tra phút 70: nếu team đang thua và chưa dùng AllOutAttackTactic → tự động chuyển
  2. Tính P(goal) = BASE_PROB * sqrt(ATK/DEF) * max(0.4, performanceFactor) * random[0.5, 1.8]
  3. Tung random để quyết định ghi bàn
  4. Giảm stamina toàn đội
Sau 9 ticks: gọi match.finishMatch() → kích hoạt Observer
```

**Edge-cases:**
- performanceFactor = 0 (tất cả stamina cạn): `Math.max(0.4, factor)` để đảm bảo luôn có xác suất tối thiểu
- P(goal) > MAX_GOAL_PROB: cap tại 0.55 để tránh quá nhiều bàn thắng
- Team thiếu cầu thủ: dùng default performanceFactor = 0.75
- Match đã played: throw `IllegalStateException` ngay đầu method

---

### Prompt 3.3 — Observer Pattern (LeagueTable)
```
Implement LeagueTable implements MatchObserver.
onGoal(): không làm gì (điểm chỉ cập nhật sau khi kết thúc trận).
onMatchFinished(): cập nhật stats cho cả homeTeam và awayTeam:
  - Tăng played, goalsFor, goalsAgainst
  - Thắng: +3 points, Hòa: +1 điểm, Thua: +0 điểm
getSortedStandings(): sort theo Points DESC, GoalDifference DESC, GoalsFor DESC, Name ASC.
```

**Edge-cases cực kỳ quan trọng:**
- **Bằng điểm AND bằng hiệu số AND bằng bàn thắng**: sort theo tên đội A-Z (tiebreaker cuối cùng đảm bảo deterministic)
- **Team chưa đăng ký trong LeagueTable**: throw `IllegalStateException` với tên đội — không để null pointer exception ngầm
- **onMatchFinished gọi nhiều lần cho cùng 1 match**: KHÔNG xảy ra nếu Match.finishMatch() chỉ gọi notifyMatchFinished() 1 lần. Cần đảm bảo điều này.
- **Thống kê âm**: không thể xảy ra với logic đúng, nhưng nên validate trong printStandings()

---

## Module 4: Data Layer

### Prompt 4.1 — DataSeeder với JSON Error Handling
```
Implement DataSeeder đọc teams.json từ classpath bằng Jackson ObjectMapper.
Xử lý các lỗi sau rõ ràng:
  - File không tồn tại: IOException với đường dẫn đầy đủ
  - Mảng "teams" bị thiếu: IOException mô tả cấu trúc kỳ vọng
  - Field bắt buộc thiếu (name, attackRating...): IllegalArgumentException với tên field + context
  - Position string không hợp lệ: IllegalArgumentException với danh sách các giá trị hợp lệ
  - Rating ngoài [1,100]: IllegalArgumentException từ Team constructor
  - Player lỗi: Log warning và SKIP (không dừng toàn bộ)
```

**Edge-cases:**
- File JSON hợp lệ nhưng mảng teams rỗng `[]`: trả về empty list, không lỗi
- Team có 0 cầu thủ: hợp lệ, dùng default performanceFactor
- Encoding: đọc với UTF-8 để hỗ trợ ký tự đặc biệt trong tên cầu thủ
- Đường dẫn resource: luôn dùng classpath (`/teams.json`), không dùng file path tuyệt đối

---

## Kiểm thử Tie-breaking Logic

### Test Case: Bằng điểm, bằng hiệu số, bằng bàn thắng
```
Team A: 10 points, GD=+5, GF=15 → xếp trên Team B (alphabet: A < B)
Team B: 10 points, GD=+5, GF=15 → xếp dưới Team A
```

### Test Case: Bằng điểm, khác hiệu số
```
Team X: 10 points, GD=+3
Team Y: 10 points, GD=+5 → Team Y xếp trên
```

### Test Case: N=2 teams
```
Kỳ vọng: 2 vòng, 2 trận (1 lượt đi + 1 lượt về)
Team A tiếp Team B → Team B tiếp Team A
```

### Test Case: N lẻ (3 teams)
```
Kỳ vọng: thêm Bye, tạo 4 vòng (với 2 trận/vòng thực sự = 3+3=6 trận)
Nhưng sau bỏ Bye: mỗi vòng có 1 trận, tổng 6 trận = 3*(3-1)=6 ✓
```
