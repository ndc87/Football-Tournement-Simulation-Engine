# ⚽ Football Tournament Simulation Engine

## 📖 Giới thiệu (About the Project)
**Football Tournament Simulation Engine** là một dự án xây dựng lõi hệ thống (engine) bằng ngôn ngữ **Java**. Dự án có khả năng tạo giải đấu, tự động xếp lịch thi đấu, mô phỏng diễn biến trận đấu dựa trên chiến thuật, và tự động cập nhật bảng xếp hạng theo thời gian thực [1]. 

Dự án này là minh chứng rõ nét cho việc áp dụng Lập trình hướng đối tượng (OOP), các Cấu trúc dữ liệu & Thuật toán, và đặc biệt là các **Design Patterns** chuẩn mực [1, 2].

## ✨ Tính năng cốt lõi (Core Features)
*   **📅 Generate Fixtures (Tạo lịch thi đấu):** Triển khai thuật toán **Round-Robin** (Đá vòng tròn tính điểm). Mỗi đội sẽ gặp nhau 2 lượt (sân nhà/sân khách) mà không bị trùng lịch thi đấu [3].
*   **⚙️ Match Simulation Engine (Mô phỏng trận đấu):** Trận đấu được chia thành các "nhịp" thời gian (ví dụ: mỗi 10 phút/lần). Tỷ lệ ghi bàn được tính toán chi tiết dựa trên chỉ số Tấn công (Attack) của đội A so với Phòng ngự (Defense) của đội B, kết hợp cùng hệ số phong độ ngẫu nhiên [3].
*   **🧠 Tactical Shifts (Thay đổi chiến thuật):** Ứng dụng **Strategy Pattern**. Hệ thống sẽ tự động thay đổi chiến thuật linh hoạt (ví dụ: chuyển từ `BalancedTactic` sang `AllOutAttackTactic` khi bị dẫn bàn ở cuối trận) [2].
*   **📊 Live Standings (Bảng xếp hạng Real-time):** Ứng dụng **Observer Pattern**. Bảng xếp hạng (Observer) sẽ "lắng nghe" sự kiện từ các trận đấu (Subject) để ngay lập tức cập nhật Điểm số và Hiệu số bàn thắng bại khi trận đấu diễn ra hoặc kết thúc [2].

## 🧱 Cấu trúc Thực thể (Core Entities)
*   **`Team`**: Quản lý danh sách cầu thủ, chỉ số sức mạnh (Attack, Defense, Midfield) và chiến thuật hiện tại [1].
*   **`Player`**: Chứa thông tin tên, vị trí thi đấu, thể lực và phong độ [1].
*   **`Match`**: Quản lý trận đấu giữa Đội nhà và Đội khách, cập nhật tỷ số, thời gian và các sự kiện (bàn thắng, thẻ phạt) [3].
*   **`Tournament`**: Quản lý danh sách các đội tham gia, lịch thi đấu toàn giải và Bảng xếp hạng [3].

## 🗺️ Lộ trình Phát triển (Roadmap)
Dự án được lên kế hoạch phát triển trong 4 tuần:
*   **Tuần 1: Khởi tạo Model & Data:** Xây dựng các class cơ bản và sử dụng `DataSeeder` để nạp dữ liệu cho 20 đội bóng thực tế [4].
*   **Tuần 2: Thuật toán Xếp lịch:** Triển khai thuật toán *Round-Robin* để tự động sinh ra 38 vòng đấu cho 20 đội [4, 5].
*   **Tuần 3: Simulation Engine & Design Patterns:** Viết vòng lặp trận đấu (1-90 phút) và áp dụng *Strategy Pattern* cùng *Observer Pattern* để mô phỏng trận đấu và cập nhật Bảng xếp hạng vòng 1 [5].
*   **Tuần 4: Tích hợp Hệ thống:** Đưa logic cốt lõi vào framework (Spring Boot API hoặc giao diện Desktop) và tối ưu hóa các trường hợp kiểm thử (bằng điểm, bằng hiệu số) [6, 7].

## 🚀 Hướng dẫn Cài đặt (Getting Started)
*(Sẽ cập nhật chi tiết cách clone repo, chạy ứng dụng bằng dòng lệnh hoặc framework ở các giai đoạn sau)*
