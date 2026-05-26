# ⚽ Football Tournament Simulation Engine

> **Hệ thống mô phỏng giải đấu bóng đá chuyên nghiệp** được xây dựng bằng Java, áp dụng các Design Patterns chuẩn mực và thuật toán xếp lịch Round-Robin.

---

## 📖 Giới thiệu (About the Project)

**Football Tournament Simulation Engine** là một engine lõi (core engine) viết bằng **Java 23**, có khả năng:
- Tự động tạo lịch thi đấu Premier League (380 trận, 38 vòng) với thuật toán Round-Robin
- Mô phỏng diễn biến trận đấu theo mô hình Time-step (9 nhịp × 10 phút)
- Tự động cập nhật bảng xếp hạng real-time qua Observer Pattern
- Thay đổi chiến thuật linh hoạt ở phút 70 qua Strategy Pattern

Dự án là minh chứng thực tế cho việc áp dụng **OOP**, **Design Patterns**, và **Thuật toán** trong lập trình Java.

---

## ✨ Tính năng cốt lõi (Core Features)

| Tính năng | Mô tả | Design Pattern |
|-----------|-------|----------------|
| 📅 **Generate Fixtures** | Tạo 380 trận đấu không xung đột cho 20 đội | Round-Robin (Circle Method) O(N²) |
| ⚙️ **Match Simulation** | Mô phỏng 90 phút với xác suất ghi bàn theo công thức thống kê | Time-step Simulation |
| 🧠 **Tactical Shifts** | Tự động chuyển sang `AllOutAttackTactic` ở phút 70 khi đang thua | **Strategy Pattern** |
| 📊 **Live Standings** | Bảng xếp hạng cập nhật ngay khi kết thúc mỗi trận | **Observer Pattern** |
| 🏆 **Tie-breaking** | Phân hạng theo: Điểm → Hiệu số → Bàn thắng → Tên đội | Custom Comparator |

---

## 🧱 Kiến trúc Hệ thống

```
Main (CLI)
  │
  ├── DataSeeder ──── teams.json (20 Premier League teams)
  │
  ├── FixtureGenerator ──── Round-Robin Algorithm
  │         └── 380 Match objects
  │
  ├── MatchSimulationEngine
  │         ├── Strategy Pattern: Tactic (Balanced / AllOutAttack)
  │         └── Time-step: 9 ticks × 10 min
  │
  └── Tournament
            ├── Team[] ──── Player[]
            ├── Match[][] ──── MatchEvent[]
            └── LeagueTable (Observer) ──── TeamStats[]
```

---

## 🚀 Hướng dẫn Cài đặt & Chạy

### Yêu cầu
- **JDK 23** (đã cài tại `C:\Program Files\Java\jdk-23`)
- **Apache Maven 3.9+** (hoặc dùng Maven tại `d:\codeNDC\apache-maven-3.9.6\`)

### Bước 1: Clone / mở project
```bash
cd d:\codeNDC\Football-Tournement-Simulation-Engine
```

### Bước 2: Compile
```bash
# Dùng Maven đã download sẵn
set JAVA_HOME=C:\Program Files\Java\jdk-23
d:\codeNDC\apache-maven-3.9.6\bin\mvn.cmd clean compile
```

### Bước 3: Chạy chương trình
```bash
d:\codeNDC\apache-maven-3.9.6\bin\mvn.cmd exec:java
```

### Bước 4: Build JAR (tùy chọn)
```bash
d:\codeNDC\apache-maven-3.9.6\bin\mvn.cmd package
java -jar target\football-tournament-engine-1.0.0.jar
```

---

## 🎮 Cách sử dụng (Usage)

Khi chạy, hệ thống hiển thị menu tương tác:

```
╔══════════════════════════════════════════════════════════════════════════════╗
║        ⚽  FOOTBALL TOURNAMENT SIMULATION ENGINE  ⚽                        ║
║                     Premier League Edition                                  ║
╚══════════════════════════════════════════════════════════════════════════════╝

  [1] Chạy vòng đấu tiếp theo         ← Xem chi tiết từng phút
  [2] Chạy 5 vòng đấu tiếp theo       ← Chế độ tăng tốc
  [3] Chạy toàn bộ giải đấu           ← Kết quả cuối mùa
  [4] Xem bảng xếp hạng hiện tại
  [5] Xem kết quả một vòng đấu cụ thể
  [0] Thoát
```

---

## 📊 Ví dụ Output

### Danh sách đội bóng
```
╔═══╦══════════════════════════════╦═════╦═════╦═════╦════════════════╗
║ # ║ Đội bóng                     ║ ATK ║ DEF ║ MID ║    Overall     ║
╠═══╬══════════════════════════════╬═════╬═════╬═════╬════════════════╣
║  1║ Manchester City              ║   92║   85║   91║          89.3  ║
║  2║ Arsenal                      ║   86║   83║   87║          85.2  ║
╚═══╩══════════════════════════════╩═════╩═════╩═════╩════════════════╝
```

### Bảng xếp hạng sau cả mùa
```
╔══════════════════════════════════════════════════════════════════════════════╗
║                      🏆  BẢNG XẾP HẠNG PREMIER LEAGUE                      ║
╠═══╦══════════════════════════╦═══╦═══╦═══╦═══╦═════╦═════╦════╦════════════╣
║ # ║ Đội bóng                 ║ P ║ W ║ D ║ L ║  F  ║  A  ║ GD ║    PTS     ║
╠═══╬══════════════════════════╬═══╬═══╬═══╬═══╬═════╬═════╬════╬════════════╣
║  1 ║ Manchester City          ║ 38║ 22║  8║  8║   67║   31║  36║        74  ║
║  2 ║ Arsenal                  ║ 38║ 21║  9║  8║   63║   29║  34║        72  ║
╚═══╩══════════════════════════╩═══╩═══╩═══╩═══╩═════╩═════╩════╩════════════╝
```

---

## 🗺️ Lộ trình Phát triển

| Tuần | Giai đoạn | Trạng thái |
|------|-----------|------------|
| **1** | Foundation & Data — OOP entities + DataSeeder JSON | ✅ Hoàn thành |
| **2** | Scheduling Core — Round-Robin 380 trận | ✅ Hoàn thành |
| **3** | Simulation Engine — Strategy + Observer Pattern | ✅ Hoàn thành |
| **4** | Optimization — Tie-breaking + CLI integration | ✅ Hoàn thành |

---

## 📁 Cấu trúc Project

```
Football-Tournement-Simulation-Engine/
├── src/main/java/com/football/
│   ├── Main.java                    ← CLI Entry Point
│   ├── model/                       ← Domain Entities
│   │   ├── Player.java, Team.java, Match.java
│   │   ├── Tournament.java, MatchEvent.java
│   │   └── Position.java, EventType.java (enums)
│   ├── tactic/                      ← Strategy Pattern
│   │   ├── Tactic.java (interface)
│   │   ├── BalancedTactic.java
│   │   └── AllOutAttackTactic.java
│   ├── observer/                    ← Observer Pattern
│   │   ├── MatchObserver.java (interface)
│   │   └── LeagueTable.java
│   ├── engine/                      ← Core Algorithms
│   │   ├── FixtureGenerator.java    (Round-Robin)
│   │   └── MatchSimulationEngine.java
│   └── data/
│       └── DataSeeder.java          ← JSON → Objects
├── src/main/resources/
│   └── teams.json                   ← 20 Premier League teams
├── docs/
│   ├── backend-specs.md
│   ├── system-design.md
│   ├── frontend-ui-specs.md
│   └── ai-execution-plan.md
├── pom.xml                          ← Maven build (Java 23)
├── execusionplan.md                 ← Project specification
└── README.md
```

---

## 📚 Tài liệu Tham khảo

- [Backend Specs](docs/backend-specs.md) — DataSeeder, Round-Robin pseudo-code, công thức xác suất
- [System Design](docs/system-design.md) — Class diagram, Strategy & Observer Pattern analysis
- [Frontend UI Specs](docs/frontend-ui-specs.md) — REST API endpoints, Java Swing layout
- [AI Execution Plan](docs/ai-execution-plan.md) — Code-gen prompts + edge cases

---

## 🔬 Design Patterns Được Áp Dụng

### Strategy Pattern — Chiến thuật linh hoạt
```java
// Tự động ở phút 70 khi thua bàn:
homeTeam.setCurrentTactic(new AllOutAttackTactic()); // ATK×1.35, DEF×0.80
```

### Observer Pattern — Bảng xếp hạng real-time
```java
// Match (Subject) → LeagueTable (Observer)
match.addObserver(leagueTable);
match.finishMatch(); // → leagueTable.onMatchFinished() tự động cập nhật điểm
```
