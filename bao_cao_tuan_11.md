# WEEKLY REPORT
**Week 11, 12/04/2026 – 18/04/2026**

## I. GROUP INFORMATION:
*   **Group 06**
*   **Project name:** Food Application
*   **Prepared by:** Trần Mạnh Hùng
*   **Members:**
    *   23127033 – Bùi Dương Duy Cường
    *   23127391 – Nguyễn Anh Khoa
    *   23127195 – Trần Mạnh Hùng
    *   23127060 – Ninh Văn Khải
    *   23127259 – Nguyễn Tấn Thắng
*   **Meeting date:** 17/04/2026
*   **All members are present.**

## II. ACHIEVEMENTS SINCE LAST WEEK:
Dưới đây là bảng tổng hợp tiến độ hoàn thành các mục tiêu đã đề ra từ tuần trước, tập trung vào kiểm thử, tài liệu và build app:

| STT | Tính năng | Mô tả chi tiết các công việc đã hoàn thành | Người phụ trách | Tiến độ | Hạn hoàn thành |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Kiểm thử diện rộng (QC & Bug Fixing) | Tổ chức test chéo toàn bộ luồng app (Register/Login, Đặt hàng, Thanh toán SePay QR, Push Notification FCM, và Chat). Đã fix triệt để các bug liên quan đến giật lag UI ở màn hình Admin và mất session khi kill app. | Toàn bộ thành viên | 100% | 18/04/2026 |
| 2 | Hoàn thiện tài liệu dự án & Slide | Hoàn tất file báo cáo tổng kết word/pdf. Cập nhật và rà soát lại toàn bộ sơ đồ UML (Use Case, Sequence Diagram cho Auth/Payment). Chốt file Slide thuyết trình cuối cùng. | Trần Mạnh Hùng, Bùi Dương Duy Cường | 100% | 18/04/2026 |
| 3 | Tối ưu source code & Build Release APK | Loại bỏ các thư viện, file resource dư thừa. Cấu hình Proguard/R8 để tối ưu dung lượng và generate thành công file APK Release (có chữ ký Keystore) sẵn sàng để cài đặt demo lên máy thật. | Nguyễn Tấn Thắng, Ninh Văn Khải | 100% | 18/04/2026 |

## III. ISSUES AND IMPACTS:
*   Trong quá trình Build Release APK, code bị lỗi Crash App do tính năng làm rối mã (Obfuscation) của Proguard vô tình xóa mất một số class model dùng cho Retrofit và Gson. Cần phải tìm hiểu và cấu hình lại file `proguard-rules.pro` để giữ lại các entity này.
*   Việc tổng hợp các sơ đồ Sequence phức tạp (đặc biệt là tích hợp SePay và Custom Auth) tốn khá nhiều thời gian để canh chỉnh cho khớp với logic code thực tế. 

## IV. GOALS FOR NEXT WEEK
| Hạng mục (Item) | Mô tả chi tiết (Description) | Hạn hoàn thành (Due Date) | Người phụ trách (Responsibility) |
| :--- | :--- | :--- | :--- |
| **Seminar Báo cáo đồ án** | Trình bày Seminar báo cáo cuối kỳ trước hội đồng/giảng viên. Trình diễn (Demo) ứng dụng thực tế trực tiếp trên thiết bị di động. | 25/04/2026 | Toàn bộ thành viên |
| **Chuẩn bị phản biện (Q&A)** | Review lại toàn bộ kiến trúc hệ thống, API, Database Schema và các design pattern đã sử dụng (Observer, Singleton, Callbacks) để chuẩn bị trả lời các câu hỏi chất vấn từ hội đồng. | 25/04/2026 | Toàn bộ thành viên |

## V. SUMMARY:
Tuần 11 đánh dấu sự hoàn thiện 100% về mặt kỹ thuật của dự án "Food Order App". Trọng tâm tuần này không còn là viết code tính năng mới, mà chuyển hoàn toàn sang giai đoạn Đảm bảo chất lượng (QA/QC), viết báo cáo và xuất bản (Deployment). Nhờ sự phối hợp test chéo tốt, nhóm đã bắt được những bug tiềm ẩn và build thành công file APK bản chính thức với dung lượng được tối ưu. Các tài liệu kỹ thuật, sơ đồ hệ thống và Slide thuyết trình đã được đóng gói cẩn thận. Nhóm đã hoàn toàn sẵn sàng cho buổi bảo vệ đồ án cuối kỳ vào tuần tới.

## VI. AI REPORT:

**1. Trần Mạnh Hùng:**
*   **Bối cảnh:** Cần tổng hợp một khối lượng lớn thông tin kỹ thuật vào Slide thuyết trình 15 phút, đồng thời phải đảm bảo sơ đồ Sequence Diagram (Mermaid) cho phần Đăng nhập/Quên mật khẩu biểu diễn chính xác logic gọi API và Check Role hợp lý để báo cáo hội đồng.
*   **Prompt sử dụng:** 
    * "Dựa trên mô tả tính năng Custom Auth bằng SHA-256 và Supabase của tôi, hãy kiểm tra lại đoạn code Mermaid Sequence Diagram sau xem đã tối ưu và chuyên nghiệp để bỏ vào slide báo cáo chưa. Cần làm nổi bật các bước Hash Matching và Validate Role."
    * "Đóng vai một chuyên gia thuyết trình, hãy thiết kế giúp tui một cấu trúc dàn ý Slide Seminar dài 15 phút cho một App đặt đồ ăn (Mobile App). Bao gồm các slide: Đặt vấn đề, Giải pháp kiến trúc (Supabase, FCM, SePay), và Demo."
*   **Kết quả:** Tiết kiệm được 60% thời gian thiết kế cấu trúc Slide. Các sơ đồ UML được AI hỗ trợ tinh chỉnh (bổ sung tên hàm, API URL trực tiếp lên mũi tên mạng) giúp sơ đồ trở nên rất thực tế, rất tiện để giải thích cho giảng viên.

**2. Ninh Văn Khải:**
*   **Bối cảnh:** Chịu trách nhiệm build file APK bản Release nhưng lại gặp lỗi crash khi mở app do cơ chế làm rối mã bảo mật (ProGuard) làm mất đi các class Model của Retrofit.
*   **Prompt sử dụng:**
    *   "Hướng dẫn tôi từng bước cách tạo Keystore (Generate Signed Bundle / APK) trong Android Studio để build bản release cho dự án."
    *   "App Android của tôi dùng Retrofit và Gson để parse JSON. Khi build release với isMinifyEnabled = true thì bị crash NullPointerException. Hãy viết file `proguard-rules.pro` chuẩn để loại trừ (keep) các class Model và Retrofit khỏi bị Obfuscate."
*   **Kết quả:** Nhờ đoạn mã rules mà AI cung cấp (`-keep class com.example.food_order_app.model.** { *; }`), app giải quyết dứt điểm lỗi crash khi build. Bản APK sinh ra hoạt động mượt mà, dung lượng giảm đáng kể nhờ các dependency rác đã được AI hướng dẫn lọc bỏ.