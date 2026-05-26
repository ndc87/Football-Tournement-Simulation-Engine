TÀI LIỆU TỔNG HỢP ĐẶC TẢ DỰ ÁN FOOTBALL TOURNAMENT SIMULATION ENGINE

1. TỔNG QUAN DỰ ÁN (PROJECT OVERVIEW)

Dự án Football Tournament Simulation Engine là một hệ thống lõi (Engine) được phát triển bằng ngôn ngữ Java, tập trung vào việc mô phỏng chuyên sâu các giải đấu bóng đá chuyên nghiệp. Hệ thống cung cấp giải pháp tự động hóa toàn diện từ khâu lập lịch thi đấu, thực thi mô phỏng diễn biến dựa trên dữ liệu thống kê, đến quản lý trạng thái giải đấu theo thời gian thực.

Mục tiêu cốt lõi của hệ thống:

* Kiến trúc Thực thể Hóa: Tự động khởi tạo và quản lý các thực thể bóng đá với độ chi tiết cao về chỉ số kỹ thuật.
* Tối ưu hóa Lịch trình: Thực thi thuật toán xếp lịch thi đấu đảm bảo tính công bằng và ràng buộc về thời gian/địa điểm.
* Mô phỏng Động lực học: Thực hiện mô phỏng trận đấu dựa trên xác suất thống kê, phản ánh đúng tương quan sức mạnh và chiến thuật giữa các đội.
* Đồng bộ Dữ liệu Real-time: Cập nhật bảng xếp hạng và các thông số chuyên sâu ngay khi sự kiện trận đấu xảy ra thông qua kiến trúc hướng sự kiện.

2. ĐẶC TẢ THỰC THỂ CỐT LÕI (CORE ENTITIES)

Thực thể	Thuộc tính chi tiết
Team	Danh sách định danh cầu thủ, tập hợp chỉ số sức mạnh tổng hợp (Attack, Defense, Midfield), và trạng thái chiến thuật hiện hành (Tactic).
Player	Tên, vị trí thi đấu (Position), chỉ số thể lực (Stamina) và biến số phong độ (Form).
Match	Định danh Đội nhà/khách, tỷ số hiện tại, đồng hồ trận đấu và nhật ký sự kiện (Bàn thắng, thẻ phạt, chấn thương).
Tournament	Danh sách các đội tham dự, cấu trúc lịch thi đấu (Fixtures) và bảng tổng sắp (Standings/League Table).

3. THIẾT KẾ LOGIC HỆ THỐNG VÀ DESIGN PATTERNS

3.1. Thuật toán Xếp lịch (Generate Fixtures)

Module điều phối lịch thi đấu phải triển khai thuật toán Round-Robin tiêu chuẩn với các ràng buộc kiến trúc sau:

* Tính đối xứng: Mỗi cặp đấu phải diễn ra chính xác 2 lượt (Home/Away). Nếu Đội A tiếp Đội B tại lượt đi (vòng n), thì Đội B phải tiếp Đội A tại lượt về (vòng n + (N-1)).
* Ràng buộc xung đột: Trong một vòng đấu (Matchweek), một đội bóng chỉ được phép xuất hiện trong duy nhất một trận đấu.
* Tính toán mảng: Sử dụng kỹ thuật xoay vòng phần tử trong danh sách (List Rotation) để đảm bảo độ phức tạp thuật toán tối ưu O(N^2).

3.2. Công cụ Mô phỏng Trận đấu (Match Simulation Engine)

Cơ chế mô phỏng thực thi theo mô hình "Time-step Simulation" với chu kỳ 10 phút/nhịp:

* Simulation Loop: Trận đấu bao gồm 9 nhịp xử lý chính. Tại mỗi nhịp, hệ thống tính toán khả năng xảy ra sự kiện dựa trên trọng số sức mạnh.
* Công thức xác suất ghi bàn: Khả năng ghi bàn P(Goal) được xác định bởi hàm: P(Goal) = f(Attack\_A, Defense\_B, Random\_Factor) Trong đó, Random\_Factor đại diện cho biến số phong độ và các tình huống bất ngờ trên sân.
* Cập nhật trạng thái: Sau mỗi nhịp, các chỉ số thể lực của Player phải được trừ dần, ảnh hưởng trực tiếp đến hiệu suất trong các nhịp kế tiếp.

3.3. Ứng dụng Design Patterns

1. Strategy Pattern (Tactical Shifts):
  * Mục đích: Tách rời logic chiến thuật khỏi lớp Match.
  * Cơ chế: Cho phép thay đổi đối tượng Tactic tại thời điểm chạy (Runtime). Hệ thống phải tự động chuyển đổi từ BalancedTactic sang AllOutAttackTactic ở phút 70 nếu đội bóng đang trong trạng thái bị dẫn bàn để tối ưu xác suất tấn công.
2. Observer Pattern (Live Standings):
  * Mục đích: Đảm bảo tính nhất quán dữ liệu giữa Match Engine và League Table mà không gây ra phụ thuộc vòng (Circular Dependency).
  * Cơ chế: Match đóng vai trò là Subject phát thông báo mỗi khi có bàn thắng hoặc kết thúc trận. LeagueTable là Observer thực hiện cập nhật điểm số (3, 1, 0) và hiệu số (Goal Difference) ngay lập tức.

4. LỘ TRÌNH PHÁT TRIỂN 4 TUẦN (IMPLEMENTATION PLAN)

Tuần	Tên giai đoạn	Nhiệm vụ cụ thể	Mục tiêu cần đạt (Milestones)
1	Foundation & Data	Thiết kế cấu trúc OOP; Xây dựng DataSeeder nạp dữ liệu Premier League từ CSV/JSON.	Validation: Xuất bản danh sách 20 đội bóng với chỉ số sức mạnh chuẩn hóa.
2	Scheduling Core	Triển khai Round-Robin; Xử lý logic sân nhà/khách và phân bổ vòng đấu.	Validation: Tạo lập 380 trận đấu không có xung đột lịch trình hoặc trùng lặp địa điểm.
3	Simulation Engine	Áp dụng Strategy & Observer; Thiết lập vòng lặp mô phỏng 90 phút (10-min ticks).	Validation: Thực thi "Matchweek 1", tự động cập nhật bảng xếp hạng với độ trễ < 100ms.
4	Optimization & API	Đóng gói logic core; Xử lý các trường hợp bằng điểm/hiệu số (Tie-breaking rules).	Validation: API trả về kết quả xếp hạng chính xác theo thứ tự: Điểm -> Hiệu số -> Số bàn thắng.

4. ĐẶC TẢ CHI TIẾT CÁC TẬP TIN MARKDOWN (MD FILES)

5.1. README.md (Writer Prompt)

"Viết tài liệu giới thiệu dự án Football Tournament Simulation Engine. Bao gồm: Phần giới thiệu chuyên nghiệp; Hướng dẫn cài đặt JDK và môi trường Java; Hướng dẫn chạy Quick Start qua CLI; Sơ đồ tính năng tóm tắt (Quản lý đội, Xếp lịch, Mô phỏng, Bảng xếp hạng)."

5.2. backend-specs.md (Writer Prompt)

"Đặc tả chi tiết Business Logic cho Backend. Yêu cầu mô tả Class DataSeeder xử lý dữ liệu từ JSON/CSV để mô phỏng Premier League. Chi tiết hóa thuật toán Round-Robin bằng mã giả (Pseudo-code). Mô tả phương thức tính toán xác suất ghi bàn trong lớp MatchSimulation."

5.3. system-design.md (Writer Prompt)

"Phân tích cấu trúc hệ thống dưới góc độ kiến trúc phần mềm. Yêu cầu mô tả Class Diagram: Mối quan hệ Aggregation giữa Tournament và Team; Composition giữa Match và MatchEvents. Giải trình lý do sử dụng Strategy Pattern để decoupling chiến thuật và Observer Pattern để đồng bộ hóa bảng xếp hạng."

5.4. frontend-ui-specs.md (Architectural Recommendation)

Kiến trúc hệ thống phải được thiết kế theo hướng Framework-Agnostic (không phụ thuộc khung phần mềm cụ thể).

* Ưu tiên 1 (RESTful APIs): Sử dụng Spring Boot để đóng gói các Endpoint (e.g., GET /api/fixtures, POST /api/simulate). Đây là phương án khuyến nghị để đảm bảo khả năng mở rộng.
* Ưu tiên 2 (Java Swing): Cung cấp giao diện Desktop Dark Mode cho mục đích trình diễn cục bộ, tập trung vào bảng điều khiển Matchweek.

5.5. ai-execution-plan.md (Writer Prompt)

"Tạo danh sách các Prompts cho AI để thực hiện code-gen từng module. Lưu ý đặc biệt về các trường hợp biên (Edge-cases): Logic phân hạng khi hai đội bằng điểm và hiệu số (Tie-breaker logic); Xử lý ngoại lệ khi file dữ liệu đầu vào bị lỗi định dạng."

6. NGUỒN THAM KHẢO VÀ PHÁT TRIỂN MỞ RỘNG

* Kỹ thuật Xếp lịch: Nghiên cứu từ khóa "Round Robin tournament algorithm in Java" và "Circle method for fixtures".
* Logic Match Engine: Tham khảo các dự án "Football Manager CLI Java" trên GitHub để học cách quản lý trạng thái trận đấu.
* Xác suất thống kê: Tìm kiếm "Poisson distribution in football modeling" hoặc "Building a sports simulation engine" trên Medium để nâng cấp độ thực tế của công thức ghi bàn.
* Tài liệu tham khảo: Các bài báo về "Stochastic modeling in sports" để tối ưu hóa biến số Random\_Factor.
