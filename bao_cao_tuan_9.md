# WEEKLY REPORT
**Week 9, 29/03/2026 – 04/04/2026**

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
*   **Meeting date:** 03/04/2026
*   **All member are present.**

## II. ACHIEVEMENTS SINCE LAST WEEK:
Dưới đây là bảng tổng hợp tiến độ và các chức năng đã được lập trình, thiết kế trong tuần qua, dựa trên dữ liệu commit và phân công của dự án:

| STT | Tính năng | Mô tả chi tiết các công việc đã hoàn thành | Người phụ trách | Tiến độ | Hạn hoàn thành |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | AI Prediction & Sentiment | Thay đổi logic rule-based dự đoán xu hướng dựa trên truy xuất order thực tế (thay vì gán cứng). Chuyển hoàn toàn mô hình AI từ Gemini sang Qwen & Hugging Face. Cập nhật UI cho AI Insight và tính năng bộ lọc (filter). | Ninh Văn Khải | 100% | 04/04/2026 |
| 2 | Phân tích Push Notifications | Dùng AI phân tích và hệ thống hóa luồng xử lý thông báo ngoài ứng dụng (out-app). Đề xuất kiến trúc tích hợp hệ thống Supabase hiện tại với các nền tảng gửi push messages (FCM, OneSignal). | Bùi Dương Duy Cường | 100% | 04/04/2026 |
| 3 | Quản lý Admin Categories | Hoàn thiện giao diện Admin Category List dạng Grid 2 cột có tính năng Search bar và đếm số lượng món. Xây dựng Form thêm/sửa danh mục có validation, hỗ trợ chọn ảnh và pre-fill dữ liệu mode Edit. | Nguyễn Tấn Thắng | 100% | 04/04/2026 |
| 4 | Flow Order, Navbar & Seminar | Bổ sung trạng thái "Đang giao" cho các đơn hàng mang về (bỏ qua bước này nếu ăn tại quán). Fix dứt điểm lỗi navbar admin (ấn nhiều lần nhảy quyền sang user). Cấu hình bảo mật khóa API, thiết kế Slide Reveal.js từ Script chuẩn bị Thuyết trình. | Trần Mạnh Hùng | 100% | 04/04/2026 |
| 5 | Merge Logic Sepay/Voucher | Hoàn thiện công tác merge code luồng hệ thống branch main, giữ nguyên các tính năng mới đã thử nghiệm đối với webhook thanh toán (Sepay) và Voucher. | Nguyễn Anh Khoa | 100% | 04/04/2026 |

## III. ISSUES AND IMPACTS:
*   Việc chuyển đổi mô hình AI (qua Qwen và thư viện Hugging Face) để giải quyết các hạn chế API cũ tốn một chút thời gian tinh chỉnh thông số và thay đổi giao diện (UI Insight). 
*   Xung đột code (conflict) khi tiến hành Merge các branch lớn (Khoa, Thắng, Khải) đòi hỏi sự cẩn trọng để không ghi đè các tính năng hoặc file config quan trọng. 
*   Rule-based cần độ khó cao hơn khi tiến hành truy xuất thật từng order trên hệ thống, tốn công lập pseudo-code logic để AI xử lý code chính xác nhất, tránh bugs sai phân luồng dữ liệu.

## IV. GOALS FOR NEXT WEEK
| Hạng mục (Item) | Mô tả chi tiết (Description) | Hạn hoàn thành (Due Date) | Người phụ trách (Responsibility) |
| :--- | :--- | :--- | :--- |
| **Hoàn thiện AI** | Huấn luyện mô hình AI và áp dụng hợp lý vào các tính năng trọng tâm bên trong dự án. | 11/04/2026 | Ninh Văn Khải, Trần Mạnh Hùng |
| **Thông báo out-app** | Cài đặt hệ thống thông báo ngoài ứng dụng (Push Notification) để người dùng nhận được cập nhật tức thì. | 11/04/2026 | Bùi Dương Duy Cường, Nguyễn Anh Khoa |
| **Tăng độ UI/UX** | Cải thiện trải nghiệm người dùng thông qua thiết kế giao diện hiện đại, dễ sử dụng ở các màn hình con. | 11/04/2026 | Nguyễn Tấn Thắng |

## V. SUMMARY:
Trọng tâm hoạt động của cả nhóm trong tuần thứ 9 là nâng cấp quản trị ứng dụng và tinh chỉnh hệ thống thay vì xây mới ồ ạt. Tính năng nổi bật nhất trong tuần qua bao gồm quản lý Categories bên phía Admin, tinh chỉnh quy trình các trạng thái bán hàng chuẩn xác cho shipper và ăn tại chỗ, và tái cấu trúc code rule-based cho AI (sử dụng pseudo code thực tiễn truy xuất lịch sử order thật). Việc chuyển dịch các model AI (sang Qwen/Hugging Face) đang chạy rất ổn định. Nhóm cũng đã hoàn thiện các kịch bản báo cáo và slide thuyết trình thông qua Reveal.js chuẩn bị sẵn sàng cho đợt review sắp tới. 

## VI. AI REPORT:

**1. Ninh Văn Khải:**
*   **Bối cảnh:** Hiện tại rule-base đang gán cứng, cần truy xuất theo đơn hàng thực, tiến hành chỉnh lại logic rule-base.
*   **Prompt sử dụng:** (Đưa ra Pseudo code minh hoạ rõ ràng từng điều kiện MIN_REVIEWS, MIN_ORDERS, các luật "AT RISK", "HOT SELLER",...) kết hợp với câu prompt:
    *   "Trên đây là pseudo code, Hiện tại rule-base đang gán cứng, tôi cần truy xuất theo đơn hàng thực, hãy chỉnh lại logic rule-base theo hướng pseudo code trên cho tôi."
*   **Kết quả:** Nhờ sử dụng cách đưa ra pseudo code và hướng AI Agent thực hiện lại logic rule-base theo ý muốn, hệ thống đã tránh được việc AI tự code dẫn đến sai khác, đảm bảo độ chuẩn xác cao dựa trên order thật.

**2. Nguyễn Tấn Thắng:**
*   **Bối cảnh:** Cần code tính năng quản trị Danh mục (Categories) linh hoạt, đẹp mắt trên hệ thống Food Delivery app (front-end UI).
*   **Prompt sử dụng:**
    *   "Viết code (React Native/Flutter - tùy bạn chọn) cho màn hình 'Category List' của ứng dụng Food Delivery. Yêu cầu: Giao diện dạng Grid (2 cột). Mỗi item gồm: Hình ảnh danh mục (Bo góc), Tên danh mục bên dưới, và Badge hiển thị số lượng món ăn đang có. Có thanh Search bar ở trên đầu để tìm kiếm nhanh danh mục... Sử dụng dữ liệu mẫu (mock data) là một mảng các object. Style hiện đại, màu sắc chủ đạo là Cam/Trắng."
    *   "Tạo một component Form trongđể 'Thêm mới hoặc Chỉnh sửa danh mục' món ăn. Các trường thông tin bao gồm: Category Name: Input text có validation (không được để trống). Icon/Image: Một nút chọn ảnh từ thư viện máy điện thoại. Status: Một Switch (Toggle)... Nút 'Save' và 'Cancel'... Yêu cầu: Nếu là mode 'Edit', form phải tự động điền (pre-fill) dữ liệu cũ."
*   **Kết quả:** AI đã sinh mã front-end form nhập liệu, Grid UI, validation chính xác, đẩy nhanh tốc độ code luồng quản lý danh mục bên Admin.

**3. Bùi Dương Duy Cường:**
*   **Bối cảnh:** Sử dụng AI để research, phân tích và hệ thống hóa kiến trúc Push Notification trước khi nhúng vào code thật, giúp nhóm có cái nhìn kỹ thuật chuẩn xác.
*   **Prompt sử dụng:**
    *   "Sử dụng AI để hỗ trợ phân tích và hệ thống hóa nội dung liên quan đến chức năng thông báo trong ứng dụng... Phân tích hiện trạng hệ thống thông báo trong ứng dụng, Xác định các trường hợp có thể phát sinh... Giải thích khái niệm push notification, cách hoạt động khi tắt app. Mô tả luồng xử lý tổng thể... Đề xuất kiến trúc Supabase. Các bước triển khai thực tế. So sánh Push Notification Services như Firebase Cloud Messaging (FCM), OneSignal..."
*   **Kết quả:** Nhận được bản phân tích kiến trúc Backend rõ ràng. Đóng vai trò là tài liệu thiết kế hệ thống vững chắc làm nền tảng cho việc cài đặt Out-app notification vào tuần sau.

**4. Trần Mạnh Hùng:**
*   **Bối cảnh:** Xử lý các nghiệp vụ nâng cao ở Navbar Admin bị lỗi phân quyền, flow trạng thái đơn hàng (Đang giao) và kịch bản thuyết trình bảo vệ dự án.
*   **Prompt sử dụng:**
    *   "Hiện tại logic đang là Chờ xác nhận -> Chờ chế biến -> Đã phục vụ -> Đã hủy. Tui muốn bạn thêm trạng thái đang giao nữa thành Chờ xác nhận -> Chờ chế biến -> Đang giao -> Đã phục vụ -> Đã hủy. Các đơn hàng mà ăn tại quán thì nhảy thẳng lên đã phục vụ chứ không có đang giao. Sửa code cho admin như vậy luôn."
    *   "Fix lỗi navbar của admin hiện tại nếu bấm nhiều lần sẽ chuyển qua acc user"
    *   "Dựa vào dự án source code của tôi hãy tạo cho tôi 1 script thuyết trình gồm các SLIDES đi"
    *   "Dựng 1 slides bằng Reveal.js đi dựa vào script có sẵn"
*   **Kết quả:** Sửa triệt để bug nghiêm trọng ở Dashboard. Hoàn thiện đúng nghiệp vụ giao đồ ăn thực tế. Dựng thành công framework bài thuyết trình trên nền HTML/JS Reveal.js một cách nhanh chóng.
