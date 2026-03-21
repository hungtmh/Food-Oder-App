# WEEKLY REPORT
**Week 3, 14/03/2026 – 21/03/2026**

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
*   **Meeting date:** 20/03/2026
*   **All member are present.**

## II. ACHIEVEMENTS SINCE LAST WEEK:
Dưới đây là bảng tổng hợp liền mạch toàn bộ các chức năng đã được lập trình và tích hợp thành công vào dự án tính đến thời điểm hiện tại:

| STT | Tính năng | Mô tả chi tiết các công việc đã hoàn thành | Người phụ trách | Tiến độ | Hạn hoàn thành |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | Thống kê & Báo cáo (Admin) | Thêm module report-statistics, nâng cấp UI/UX cho Revenue Dashboard. Cập nhật các màn hình báo cáo và tùy chỉnh Custom Bottom Navigation. | Nguyễn Tấn Thắng | 100% | 19/03/2026 |
| 2 | Khắc phục lỗi (Admin) | Fix lỗi XML parsing error, tách biệt phần Statistics ra khỏi Revenue theo đúng yêu cầu dự án. | Nguyễn Tấn Thắng | 100% | 15/03/2026 |
| 3 | Cập nhật UI & Gợi ý (User) | Thêm giao diện Hot Offers, điều chỉnh slider cho các banner. Cập nhật hệ thống gợi ý món ăn cá nhân hóa thiết kế lại UI ứng dụng cho hợp lý hơn. | Bùi Dương Duy Cường | 100% | 20/03/2026 |
| 4 | Đặt tại quán & Hồ sơ (User) | Bổ sung thêm tính năng cho phép người dùng đặt ăn tại quán (Dine-in). Nâng cấp, cập nhật giao diện hiển thị Avatar cho hệ thống người dùng. | Trần Mạnh Hùng | 100% | 21/03/2026 |
| 5 | Thông báo (Chung) | Cập nhật luồng thông báo cơ bản, tích hợp Notification updates cùng các giao diện liên quan. | Nguyễn Tấn Thắng | 80% | 18/03/2026 |

## III. ISSUES AND IMPACTS:
*   Hệ thống thông báo đẩy (Firebase Cloud Messaging) và thanh toán ví điện tử của tuần trước vẫn đang trong quá trình thử nghiệm và ráp nối các luồng với nhau.
*   Cần thêm dữ liệu món ăn và đơn hàng đồ sộ hơn để kiểm thử module Report & Statistics (Báo cáo & Thống kê) của Admin hoạt động chính xác.
*   Tính năng đặt tại quán mới được thêm vào, cần testing kĩ các luồng xử lý giỏ hàng và đơn hàng liên quan để tránh xung đột với đơn Delivery.

## IV. GOALS FOR NEXT WEEK
| Hạng mục (Item) | Mô tả chi tiết (Description) | Hạn hoàn thành (Due Date) | Người phụ trách (Responsibility) |
| :--- | :--- | :--- | :--- |
| Kiểm thử Tích hợp (Integration) | Testing tính năng đặt tại quán, kiểm tra kĩ luồng xử lý với các đơn hàng thông thường. | 26/03/2026 | Trần Mạnh Hùng |
| Hoàn thiện Thông báo & Khuyến mãi | Tích hợp sâu thông báo đẩy và UI Hot Offers vào hệ thống thực tế cho UX tốt hơn. | 27/03/2026 | Bùi Dương Duy Cường |
| Hoàn thành Cổng thanh toán | Gắn chặt mã API thanh toán online / Mã giảm giá vào luồng Checkout của Cart. | 28/03/2026 | Nguyễn Anh Khoa |
| Tối ưu Dashboard Admin | Cải thiện hiệu suất load dữ liệu của module Báo Cáo Doanh Thu, xuất báo cáo PDF. | 27/03/2026 | Nguyễn Tấn Thắng |
| Train AI (Tiếp tục) | Gắn dữ liệu review thật để AI tiến hành đưa ra các phân tích Insight chính xác cho món ăn. | 28/03/2026 | Ninh Văn Khải |

## V. SUMMARY:
Trong đợt làm việc tuần qua, nhóm tập trung nhiều vào việc hoàn thiện UI/UX (đề xuất món, avatar, slider) và đẩy mạnh các tính năng thống kê phức tạp cho phía Admin (Báo cáo doanh thu, Custom Navigation). Đồng thời, nhóm bổ sung luồng tính năng mới là Đặt ăn ngay tại quán, đang trong quá trình tích hợp mượt mà vào hệ thống cũ.

## VI. AI REPORT:
**1. Trần Mạnh Hùng:**
*   **Mục đích:** Bổ sung tính năng cho phép người dùng quét/chọn bàn để đặt ăn ngay tại quán, cập nhật ảnh Avatar từ Storage Supabase và load mượt mà bằng Glide.
*   **Prompt sử dụng:** "Làm sao để thiết kế tùy chọn 'Ăn tại quán' hoặc 'Giao hàng' trước khi checkout trong Android, và cách upload image files Avatar từ Android lên Supabase Storage bucket an toàn?"
*   **Kết quả:** Cập nhật thành công giao diện Avatar mới và định hình lại được luồng Database cho tuỳ chọn đặt trực tiếp.

**2. Bùi Dương Duy Cường:**
*   **Mục đích:** Nâng cấp UI ở màn hình Home, thêm các banner ưu đãi Hot Offers và thuật toán hiển thị gợi ý cá nhân hóa dựa trên lịch sử xem/mua.
*   **Prompt sử dụng:** "Tôi muốn thiết kế một Section 'Hot Offers' trong RecyclerView và thuật toán gợi ý món ăn cơ bản dựa trên category mà người dụng dọn nhiều nhất trong Android."
*   **Kết quả:** Tạo ra được giao diện bắt mắt, cải thiện đáng kể UX cho User.

**3. Nguyễn Anh Khoa:**
*   **Mục đích:** Tập trung vào các thuật toán tối ưu giỏ hàng và setup hạ tầng cổng thanh toán. (Nối tiếp tuần trước)
*   **Prompt sử dụng:** "Hướng dẫn tôi debug lỗi crash khi chuyển đổi qua lại giữa Cart có mã giảm giá và tính lại Total Price trên nền Android Java."
*   **Kết quả:** Sửa được một số bugs tiềm ẩn ở Cart.

**4. Nguyễn Tấn Thắng:**
*   **Mục đích:** Xây dựng phần Biểu đồ thống kê, Báo cáo và làm Custom Bottom Navigation cho Admin app. Khắc phục lỗi parse file XML.
*   **Prompt sử dụng:** "Làm thế nào để sử dụng thư viện MPAndroidChart vẽ biểu đồ đường cho thống kê doanh thu theo ngày? Và cách để tạo một Custom Bottom Navigation Bar lượn sóng (curved) đẹp mắt trong Android XML."
*   **Kết quả:** Biểu đồ hoạt động, chia tách rành mạch Module Báo cáo thống kê, UI Bottom Nav rất đẹp. Khắc phục trơn tru lỗi XML parsing.

**5. Ninh Văn Khải:**
*   **Mục đích:** Test thử Data trả về từ AI Sentiment Analysis lên Dashboard.
*   **Prompt sử dụng:** "Làm sao để trực quan hóa nhiều loại data từ JSON responses của model AI lên các View khác nhau trên cùng một Fragment?"
*   **Kết quả:** Sơ bộ ghép được data demo lên Dashboard.
