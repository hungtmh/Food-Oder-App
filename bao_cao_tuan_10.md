# WEEKLY REPORT
**Week 10, 05/04/2026 – 11/04/2026**

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
*   **Meeting date:** 10/04/2026
*   **All members are present.**

## II. ACHIEVEMENTS SINCE LAST WEEK:
Dưới đây là bảng tổng hợp tiến độ và các chức năng đã được lập trình, thiết kế trong tuần qua (dựa trên lịch sử commit và phân công của dự án):

| STT | Tính năng | Mô tả chi tiết các công việc đã hoàn thành | Người phụ trách | Tiến độ | Hạn hoàn thành |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Cập nhật Danh mục & Doanh thu | Hoàn thiện chức năng chỉnh sửa danh mục món ăn. Bổ sung thêm thống kê doanh thu theo từng danh mục riêng biệt trong phần màn hình doanh thu (Revenue Statistics). | Nguyễn Tấn Thắng | 100% | 11/04/2026 |
| 2 | Pipeline Push Notifications | Tạo tài khoản Firebase Cloud Messaging (FCM) và xây dựng tính năng thông báo out-app dựa trên kiến trúc Supabase đã đề xuất. Thiết lập các kịch bản test case đảm bảo push notification chạy đúng trên background. | Bùi Dương Duy Cường | 100% | 11/04/2026 |
| 3 | Quản lý voucher | Thêm chức năng quản lý voucher cho admin | Ninh Văn Khải | 100% | 11/04/2026 |

## III. ISSUES AND IMPACTS:
*   Việc setup FCM (Firebase Cloud Messaging) ban đầu gặp một số khó khăn ở phần file cấu hình `google-services.json` và yêu cầu phải test nhiều trường hợp thực tế (ứng dụng đang mở, chạy ngầm hoặc đã đóng hoàn toàn) để đảm bảo thông báo đẩy không bị miss.
*   Cập nhật tính năng doanh thu theo danh mục yêu cầu query lại dữ liệu của nhiều bảng khác nhau nên cần chú ý tối ưu hóa hiệu suất load ở màn hình dashboard admin.

## IV. GOALS FOR NEXT WEEK
| Hạng mục (Item) | Mô tả chi tiết (Description) | Hạn hoàn thành (Due Date) | Người phụ trách (Responsibility) |
| :--- | :--- | :--- | :--- |
| **Kiểm thử diện rộng (QC/Testing)** | Tổ chức test chéo các tính năng quan trọng: Thanh toán qua code, hiển thị thông báo, hoạt động của AI. Lên danh sách fix bug cuối cùng trước báo cáo. | 18/04/2026 | Toàn bộ thành viên |
| **Hoàn thiện tài liệu dự án** | Viết báo cáo tổng kết, check lại diagram và hoàn tất slide thuyết trình. | 18/04/2026 | Trần Mạnh Hùng, Bùi Dương Duy Cường |
| **Bảo trì & Build bản cuối** | Tối ưu source code, lược bỏ các dependencies không cần thiết và tiến hành build (APK) bản Release cho việc demo. | 18/04/2026 | Nguyễn Tấn Thắng, Ninh Văn Khải |

## V. SUMMARY:
Trọng tâm hoạt động của nhóm trong tuần 10 đã được chuyển hẳn sang việc hoàn thiện các tính năng nâng cao và chắp nối toàn bộ hệ thống (Notifications, Thống kê Doanh Thu chi tiết, tinh chỉnh UI/UX). Đáng kể nhất là hệ thống thông báo đẩy out-app (Push Notification bằng FCM) đã được Cường đưa vào ứng dụng thành công, đi kèm với khả năng thống kê doanh thu theo danh mục hoàn chỉnh của Thắng. Các công việc còn lại chủ yếu là fix bug phần UI, bảo mật API config và merge logic từ các branch con, tạo tiền đề tốt nhất để nhóm bước vào giai đoạn kiểm thử diện rộng trong tuần cuối.

## VI. AI REPORT:

**1. Nguyễn Tấn Thắng:**
*   **Bối cảnh:** Cần hoàn thiện form chỉnh sửa danh mục quản lý bên Admin và phải hiển thị thêm biểu đồ/số liệu thống kê cụ thể của "Doanh thu theo danh mục" bên trong màn hình Doanh thu.
*   **Prompt sử dụng:** 
    * "Viết mã logic và cả UI cập nhật mới (có thể dùng React Native/Flutter) bổ sung thêm tính năng cập nhật (Edit) bên trong trang quản lý danh mục (Category). Yêu cầu check validation đầy đủ trước khi lưu dữ liệu."
    * "Dựa trên màn hình biểu đồ doanh thu hiện tại của tui, hãy hướng dẫn cách query dữ liệu và code biểu đồ thống kê hiển thị 'Doanh thu danh mục'. Mỗi danh mục sẽ đóng góp bao nhiêu phần trăm doanh thu, chia ra từng màu sắc phù hợp ở Dashboard Admin."
*   **Kết quả:** Nhờ làm rõ yêu cầu, AI đã xây dựng cách query data tính toán doanh thu hiệu quả mà không bị chậm lag màn hình, đồng thời sinh thành công đoạn code xử lý hiển thị ở trang edit Category.

**2. Bùi Dương Duy Cường:**
*   **Bối cảnh:** Bắt tay vào cài đặt hệ thống FCM thực tế đã được phân tích kiến trúc từ tuần 9 và cần liệt kê các test case để chắc chắn push notification không gặp lỗi.
*   **Prompt sử dụng:**
    *   "Dựa trên pipeline kiến trúc Push Notification đã thiết kế cho project App Mobile, hướng dẫn tôi chi tiết từng bước cách tạo tài khoản Firebase Cloud Messaging (FCM), lấy API key, config vào mã nguồn Supabase để kích hoạt tính năng thông báo."
    *   "Chỉ tôi cách viết các luồng test case cho các trường hợp: 1. App đang mở (Foreground); 2. App đang chạy ngầm (Background); 3. App đã bị tắt hoàn toàn (Terminated/Killed); để đảm bảo Push Notification từ hệ thống vẫn hoạt động đúng 100%."
*   **Kết quả:** Quá trình cài đặt hoàn thành nhanh chóng và không thiếu bước config quan trọng nào nhờ checklist từ AI. Các trường hợp test rõ ràng giúp phát hiện và sửa kịp thời lỗi bị miss thông báo trên các thiết bị khi tắt app hoàn toàn.
