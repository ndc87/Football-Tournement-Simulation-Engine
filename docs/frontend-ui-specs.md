# Frontend & UI Specifications — Football Tournament Simulation Engine

## Kiến trúc Framework-Agnostic

Hệ thống được thiết kế theo nguyên tắc **Framework-Agnostic**: core engine hoàn toàn tách biệt khỏi presentation layer. Điều này cho phép tích hợp bất kỳ frontend nào mà không cần sửa business logic.

---

## Ưu tiên 1: RESTful API (Spring Boot)

### Mô tả
Đóng gói `MatchSimulationEngine` và `Tournament` logic vào các REST endpoint. Đây là phương án **khuyến nghị** cho môi trường production, đảm bảo khả năng mở rộng và tích hợp với bất kỳ frontend hiện đại nào (React, Vue, Angular).

### Dependency cần thêm vào pom.xml

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>3.2.5</version>
</dependency>
```

### API Endpoints Specification

#### GET /api/teams
Lấy danh sách tất cả đội bóng với chỉ số sức mạnh.

**Response:**
```json
[
  {
    "name": "Manchester City",
    "shortName": "MCI",
    "attackRating": 92,
    "defenseRating": 85,
    "midfieldRating": 91,
    "overallRating": 88.6
  }
]
```

#### GET /api/fixtures
Lấy toàn bộ lịch thi đấu.

**Query params:** `?matchweek=1` (tùy chọn — lọc theo vòng)

**Response:**
```json
{
  "matchweek": 1,
  "matches": [
    {
      "matchId": "mw1-1",
      "homeTeam": "Arsenal",
      "awayTeam": "Burnley",
      "played": false,
      "homeScore": null,
      "awayScore": null
    }
  ]
}
```

#### POST /api/simulate
Chạy mô phỏng một hoặc nhiều vòng đấu.

**Request body:**
```json
{
  "matchweeks": [1],
  "verbose": false
}
```

**Response:**
```json
{
  "simulatedMatchweeks": [1],
  "results": [
    {
      "homeTeam": "Arsenal",
      "awayTeam": "Burnley",
      "homeScore": 3,
      "awayScore": 0,
      "events": [
        { "minute": 20, "type": "GOAL", "team": "Arsenal", "description": "..." }
      ]
    }
  ],
  "standings": [...]
}
```

#### GET /api/standings
Lấy bảng xếp hạng hiện tại (đã sort theo tie-breaking rules).

**Response:**
```json
[
  {
    "rank": 1,
    "team": "Manchester City",
    "played": 5,
    "won": 4,
    "drawn": 1,
    "lost": 0,
    "goalsFor": 14,
    "goalsAgainst": 4,
    "goalDifference": 10,
    "points": 13
  }
]
```

#### POST /api/simulate/full
Chạy toàn bộ 38 vòng đấu và trả về kết quả cuối cùng.

---

## Ưu tiên 2: Java Swing Desktop (Dark Mode)

### Mô tả
Giao diện Desktop đơn giản phục vụ mục đích **demo cục bộ**. Sử dụng Java Swing với theme Dark Mode.

### Layout Specification

```
┌──────────────────────────────────────────────────────────────────┐
│  ⚽ Football Tournament Simulation Engine        [Dark Mode]      │
├──────────────────────────────────────────────────────────────────┤
│  [Matchweek: 1 / 38]   [▶ Next Round]  [▶▶ Simulate All]        │
├─────────────────────────────┬────────────────────────────────────┤
│   FIXTURES (Vòng 1)         │        BẢNG XẾP HẠNG               │
│  ─────────────────────      │  ─────────────────────────────────  │
│  Man City 3 - 0 Luton       │  # Team    P  W  D  L  GD  PTS     │
│  Arsenal  2 - 1 Burnley     │  1 Man Cit 1  1  0  0  +3  3       │
│  Liverpool 1 - 1 Everton    │  2 Arsenal  1  1  0  0  +1  3       │
│  ...                        │  ...                               │
├─────────────────────────────┴────────────────────────────────────┤
│  MATCH LOG                                                       │
│  [20'] GOL Arsenal - Saka ghi ban! 1-0                          │
│  [45'] YEL Burnley - The vang                                    │
│  [78'] GOL Arsenal - Odegaard ghi ban! 2-0                      │
└──────────────────────────────────────────────────────────────────┘
```

### Dark Mode Color Palette

```java
// Background layers
Color BACKGROUND_DARK    = new Color(18, 18, 18);   // #121212
Color SURFACE            = new Color(30, 30, 30);   // #1E1E1E
Color SURFACE_VARIANT    = new Color(40, 40, 40);   // #282828

// Accent colors
Color ACCENT_GREEN       = new Color(0, 200, 83);   // #00C853
Color ACCENT_BLUE        = new Color(33, 150, 243); // #2196F3
Color TEXT_PRIMARY       = new Color(255, 255, 255);
Color TEXT_SECONDARY     = new Color(180, 180, 180);
```

---

## Hiện tại: CLI Mode

Phiên bản hiện tại sử dụng **CLI Interactive Menu** — đủ để validate toàn bộ engine logic trước khi tích hợp UI.

```
[1] Chay vong dau tiep theo
[2] Chay 5 vong dau tiep theo  
[3] Chay toan bo giai dau
[4] Xem bang xep hang hien tai
[5] Xem ket qua mot vong dau cu the
[0] Thoat
```
