# System Design — Football Tournament Simulation Engine

## 1. Tổng quan Kiến trúc

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Main.java (Entry Point)                      │
│                    Interactive CLI Menu Controller                  │
└───────────────────────────┬─────────────────────────────────────────┘
                            │ orchestrates
           ┌────────────────┼────────────────┐
           │                │                │
    ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
    │ DataSeeder  │  │  Fixture    │  │   Match     │
    │             │  │  Generator  │  │ Simulation  │
    │ teams.json  │  │  Round-Robin│  │   Engine    │
    └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
           │                │                │
           └────────────────▼────────────────┘
                     ┌──────▼──────────────┐
                     │     Tournament      │
                     │  (Aggregates all)   │
                     └──────┬──────────────┘
                            │ contains
                 ┌──────────┼──────────┐
          ┌──────▼──┐  ┌────▼────┐  ┌──▼──────────┐
          │  Team[] │  │Match[][] │  │ LeagueTable │
          └──────┬──┘  └────┬────┘  └──────────────┘
                 │          │ Observer Pattern
          ┌──────▼──┐       │ (Match → LeagueTable)
          │ Player[]│       │
          └─────────┘  ┌────▼────┐
                       │ Tactic  │ Strategy Pattern
                       └─────────┘
```

---

## 2. Class Diagram

### Core Model Relationships

```
Tournament ─────────────── Aggregation ─────────────── Team (1..*)
    │                                                      │
    │                                                      │
    ├─── Aggregation ──── List<List<Match>>            Composition
    │                         │                            │
    │                         │                         Player (1..*)
    │                    Composition
    │                         │
    │                    MatchEvent (0..*)
    │
    └─── Has ──────────── LeagueTable (1)
```

**Giải thích quan hệ:**
- **Tournament → Team**: `Aggregation` — Tournament chứa danh sách Team, nhưng Team có thể tồn tại độc lập
- **Match → MatchEvent**: `Composition` — MatchEvent chỉ tồn tại trong context của Match. Khi Match bị xóa, MatchEvent cũng bị xóa
- **Team → Player**: `Composition` — Player thuộc về Team, không tồn tại độc lập

### Class Details

```
┌──────────────────────────────────┐
│           Tournament             │
├──────────────────────────────────┤
│ - name: String                   │
│ - teams: List<Team>              │
│ - fixtures: List<List<Match>>    │
│ - leagueTable: LeagueTable       │
│ - currentMatchweek: int          │
├──────────────────────────────────┤
│ + addTeam(team)                  │
│ + setFixtures(fixtures)          │
│ + getMatchweekFixtures(week)     │
│ + getTotalMatches()              │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│              Team                │
├──────────────────────────────────┤
│ - name: String                   │
│ - shortName: String              │
│ - players: List<Player>          │
│ - attackRating: int              │
│ - defenseRating: int             │
│ - midfieldRating: int            │
│ - currentTactic: Tactic          │  ← Strategy Pattern
├──────────────────────────────────┤
│ + getOverallRating(): double     │
│ + getAveragePerformanceFactor()  │
│ + drainAllPlayersStamina()       │
│ + setCurrentTactic(tactic)       │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│              Match               │
├──────────────────────────────────┤
│ - homeTeam: Team                 │
│ - awayTeam: Team                 │
│ - homeScore: int                 │
│ - awayScore: int                 │
│ - currentMinute: int             │
│ - events: List<MatchEvent>       │
│ - observers: List<MatchObserver> │  ← Subject (Observer Pattern)
├──────────────────────────────────┤
│ + addObserver(observer)          │
│ + scoreHomeGoal(minute)          │
│ + scoreAwayGoal(minute)          │
│ + finishMatch()                  │  ← notifyMatchFinished()
│ - notifyGoal(team)               │
│ - notifyMatchFinished()          │
└──────────────────────────────────┘
```

---

## 3. Strategy Pattern — Tactical Shifts

### Vấn đề
Nếu nhúng logic chiến thuật trực tiếp vào class `Match` hoặc `MatchSimulationEngine`, code sẽ trở nên:
- Khó mở rộng khi thêm chiến thuật mới (vi phạm Open/Closed Principle)
- Khó test từng chiến thuật riêng lẻ
- Phụ thuộc chặt chẽ (tight coupling)

### Giải pháp
```
        «interface»
          Tactic
    ┌─────────────────┐
    │ +getName()      │
    │ +getAttackMult()│
    │ +getDefenseMult()│
    └────────┬────────┘
             │ implements
    ┌────────┴─────────────────┐
    │                          │
┌───▼───────────┐  ┌───────────▼────────────┐
│BalancedTactic │  │  AllOutAttackTactic    │
│ ATK × 1.00   │  │  ATK × 1.35           │
│ DEF × 1.00   │  │  DEF × 0.80           │
└───────────────┘  └────────────────────────┘
```

**Lợi ích:**
- Thêm `DefensiveTactic`, `CounterAttackTactic` mà không sửa code cũ
- Team chỉ phụ thuộc vào interface `Tactic`, không phụ thuộc implementation cụ thể
- Switch chiến thuật tại runtime không ảnh hưởng đến các class khác

---

## 4. Observer Pattern — Live Standings

### Vấn đề
Nếu `Match` gọi trực tiếp `LeagueTable.update()`, sẽ tạo ra **Circular Dependency**:
- `Match` cần biết về `LeagueTable`
- `LeagueTable` cần biết về `Match`

### Giải pháp

```
        «interface»
        MatchObserver
    ┌──────────────────┐
    │ +onGoal()        │
    │ +onMatchFinished()│
    └──────┬───────────┘
           │ implements
    ┌──────▼───────────┐
    │   LeagueTable    │ ◄── Observer
    │                  │
    │ +onGoal()        │  → không làm gì (điểm tính khi kết thúc)
    │ +onMatchFinished()│ → cập nhật điểm W/D/L, GF/GA/GD
    └──────────────────┘

    ┌──────────────────┐
    │      Match       │  ← Subject
    │                  │
    │ observers: List  │
    │ +addObserver()   │
    │ -notifyGoal()    │
    │ -notifyFinished()│
    └──────────────────┘
```

**Lợi ích:**
- `Match` không phụ thuộc vào `LeagueTable`; chỉ biết về interface `MatchObserver`
- Dễ thêm Observer mới: `StatisticsTracker`, `NotificationService`, v.v.
- Tách biệt hoàn toàn presentation logic khỏi domain logic

---

## 5. Data Flow

```
[1] DataSeeder.loadTeams()
         │
         ▼
[2] Tournament.addTeam() × 20
         │
         ▼
[3] FixtureGenerator.generate(teams) → 380 Match objects
         │
         ▼
[4] Tournament.setFixtures() → đăng ký LeagueTable là Observer cho mỗi Match
         │
         ▼
[5] User chọn "Simulate Matchweek"
         │
         ▼
[6] MatchSimulationEngine.simulate(match)
    ├── For each tick (1-9):
    │   ├── applyTacticalSwitch() [Strategy Pattern]
    │   ├── simulateGoalChance(home) → match.scoreHomeGoal() → notifyGoal()
    │   ├── simulateGoalChance(away) → match.scoreAwayGoal() → notifyGoal()
    │   └── drainAllPlayersStamina()
    └── match.finishMatch() → notifyMatchFinished() → LeagueTable.onMatchFinished()
         │
         ▼
[7] LeagueTable.getSortedStandings() → Sorted by Points, GD, GF, Name
```

---

## 6. Package Structure

```
com.football/
├── Main.java                    ← CLI Entry Point
├── model/                       ← Domain Entities
│   ├── Player.java
│   ├── Position.java            (enum)
│   ├── Team.java
│   ├── Match.java               (Observer Subject)
│   ├── MatchEvent.java
│   ├── EventType.java           (enum)
│   └── Tournament.java
├── tactic/                      ← Strategy Pattern
│   ├── Tactic.java              (interface)
│   ├── BalancedTactic.java
│   └── AllOutAttackTactic.java
├── observer/                    ← Observer Pattern
│   ├── MatchObserver.java       (interface)
│   └── LeagueTable.java
├── engine/                      ← Business Logic
│   ├── FixtureGenerator.java    (Round-Robin algorithm)
│   └── MatchSimulationEngine.java
└── data/                        ← Data Layer
    └── DataSeeder.java
```
