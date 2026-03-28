# WEEKLY REPORT
**Week 4, 22/03/2026 – 28/03/2026**

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
*   **Meeting date:** 27/03/2026
*   **All member are present.**

## II. ACHIEVEMENTS SINCE LAST WEEK:
Dưới đây là bảng tổng hợp liền mạch toàn bộ các chức năng đã được lập trình và tích hợp thành công vào dự án tính đến thời điểm hiện tại:

| STT | Tính năng | Mô tả chi tiết các công việc đã hoàn thành | Người phụ trách | Tiến độ | Hạn hoàn thành |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | AI Sentiment & Prediction | Chuyển đổi kiến trúc tự train sang gọi API (Hugging Face BERT model) bằng Supabase Edge Functions để phân tích cảm xúc đánh giá. Tạo logic Score-based Decision Tree dự đoán tình trạng món ăn. Tích hợp mô hình Gemini để hỗ trợ phân tích. | Ninh Văn Khải | 100% | 28/03/2026 |
| 2 | UI/UX Trang chủ | Gắn header có background wave, hỗ trợ hiển thị/thêm địa chỉ giao hàng và màn hình chọn địa chỉ. Tạo placeholder tìm kiếm thân thiện. Layout danh sách Categories dạng cuộn ngang (scroll) cực gọn gàng, hiện đại. | Bùi Dương Duy Cường | 100% | 28/03/2026 |
| 3 | Thanh toán Sepay & Voucher | Tích hợp hệ thống mã QR chuyển khoản ngân hàng qua Sepay cùng tính năng check webhook bằng Supabase. Cải tiến hệ thống logic cấp Voucher (Public / Private) tương thích schema database. | Nguyễn Anh Khoa | 100% | 28/03/2026 |
| 4 | Admin Revenue Dashboard & Security | Gộp các luồng báo cáo vào 1 màn hình Dashboard, vẽ chart doanh thu theo tháng. Ứng dụng AI Gemini 3 Flash để tự nhận xét báo cáo (human-like commentary). Cấu hình bảo mật `.gitignore` ẩn các token/key, giải quyết triệt để lỗi navigation XML. | Nguyễn Tấn Thắng | 100% | 27/03/2026 |
| 5 | Đánh giá, Chat, & Quản lý Địa Chỉ | Đưa giao diện Đánh Giá ra trang riêng, cho phép up nhiều ảnh cùng lúc, fix trung bình sao. Cải tiến Phản Hồi -> Chat luồng 1-N (1 user chat với nhiều admin). Tách rời Địa Chỉ ra khỏi Profile, cấu hình form spinner Tỉnh/Thành nhờ file JSON. | Trần Mạnh Hùng | 100% | 25/03/2026 |

## III. ISSUES AND IMPACTS:
*   Việc phát triển thêm AI cho Food Prediction bằng Edge AI hoặc gọi API FastAPI sắp tới trong tương lai hứa hẹn sẽ khá tốn effort và logic phức tạp.
*   Merge branch trên Github đôi lúc nảy sinh conflict và tốn khá nhiều thời gian đồng bộ (ví dụ: `AndroidManifest.xml` hoặc khi merge code AI sentiment). Việc config ẩn API keys đôi khi gây một số lỗi build. Hiện tại tất cả đã được giải quyết triệt để.

## IV. GOALS FOR NEXT WEEK
| Hạng mục (Item) | Mô tả chi tiết (Description) | Hạn hoàn thành (Due Date) | Người phụ trách (Responsibility) |
| :--- | :--- | :--- | :--- |
| Tối ưu Food Prediction AI | Chuyển hướng dự đoán theo mô hình Random Forest / Dùng TF Lite hoặc Server chạy Python bằng FastAPI (Render/Railway) gọi cho Client. | 04/04/2026 | Ninh Văn Khải |
| Đồng bộ hệ thống UI/UX | Chỉnh sửa các lỗi front-end nhỏ tàn dư, cải thiện hoạt họa ở các màn hình con, duy trì sự nhất quán. | 04/04/2026 | Bùi Dương Duy Cường |
| Chạy thử nghiệm Sepay | Thử thanh toán end-to-end webhook khi lên đơn và ráp voucher vào logic trừ tiền ở Cart. | 04/04/2026 | Nguyễn Anh Khoa |
| Kiểm thử AI ở Admin | Tinh chỉnh độ nhạy ngôn ngữ của Gemini 3 Flash khi nhận xét doanh thu tháng, ổn định layout biểu đồ. | 04/04/2026 | Nguyễn Tấn Thắng |
| Kiểm thử luồng Chat & Địa chỉ | Testing tính realtime của công cụ Chat giữa User/Admin. Rà soát UI dropdown địa chỉ. | 04/04/2026 | Trần Mạnh Hùng |

## V. SUMMARY:
Trọng tâm hoạt động của cả nhóm trong tuần thứ 4 là đưa AI vào dự án một cách thực tiễn nhất. AI chạy ngầm để Sentiment bài Review món ăn, AI giúp Admin làm báo cáo nhận định tài chính, hay dùng AI hỗ trợ code layout UI xịn và logic Sepay/Vouchers. Bên cạnh đó, luồng quản lý địa chỉ được làm lại toàn bộ, thêm chat trực tiếp với admin và dọn dẹp Git branch là một bước tiến cực quan trọng của team.

## VI. AI REPORT:

**1. Ninh Văn Khải:**
*   **Bối cảnh:** Trong tuần đầu làm dự án định tự train AI cục bộ. Do tốn kém tài nguyên máy và method dictionary làm không khả quan bèn đổi qua API-based architecture. Tích hợp AI (Hugging Face / BERT model bằng Supabase Edge Functions) để tự phân tích câu Review (khen/chê).
*   **Prompt sử dụng:**
    *   "Hướng đi hiện tại của tôi đang dựa trên Heuristic đúng không ?"
    *   "Vậy là phải truy xuất ra dữ liệu trước rồi mới build decision tree được à ? Hồi xưa tôi học thì hình như thầy tôi cho dùng python liệu java có build được ko ? hay là chủ yếu dùng câu lệnh if-else + truy xuất đếm"
    *   "Thay vì chỉ if-else đơn giản, hãy biến nó thành một Score-based Decision Tree: Tính điểm tích lũy: SalesScore=(GrowthRate>1.2)?50:20;... Tôi muốn cải tiến code theo hướng này"
    *   "/fix Hiện tại tôi đã code xong decision tree và các phân tích AI... bạn có thấy nước ép cam 5 sao nhưng lại bị dính tiêu cực. Liệu có nhầm lẫn gì bởi kết quả trả về từ Gemini Flash không ? tìm lỗi gây ra bug này?"
*   **Kết quả:** Sửa thành công được thiết kế kiến trúc hệ thống, fix được bug nhãn cảm xúc trả về không chuẩn. Tuần tiếp theo dự kiến sẽ build mô hình Random Forest bọc FastAPI lên server miễn phí để lấy API Prediction.

**2. Bùi Dương Duy Cường:**
*   **Bối cảnh:** AI hỗ trợ cải thiện UI/UX, phát hiện lỗi thiếu sót và logic hiển thị mặt tiền App. Tất cả những đề xuất từ AI đều được verify cẩn thận thay vì apply trực tiếp. 
*   **Prompt sử dụng:**
    *   "Thiết kế và cải thiện UI cho trang chủ của ứng dụng đặt đồ ăn (Android) như sau: Ở phần trên cùng hiển thị “Giao tới: [địa chỉ mặc định của user]”, nếu user chưa có địa chỉ... mở màn hình chọn giống flow đặt hàng... bên phải gồm nút Thông báo và Yêu thích"
    *   "Phần header cần có background là ảnh wave từ thư mục drawable và tất cả các thành phần... đè lên trên ảnh này (gợi ý cách implement để layer UI nằm trên background image)"
    *   "Thanh tìm kiếm đổi placeholder thành 'Bạn đang thèm gì nào'; các icon (thông báo, yêu thích) có nền tròn blur phía sau và icon chỉ giữ... danh sách categories dạng scroll ngang... theo hướng hiện đại, gợi ý code bằng Android XML hoặc Jetpack Compose."
*   **Kết quả:** Khung layout header cải thiện 100% cảm giác gọn gàng và trải nghiệm người dùng, giúp dev định hình được XML cấu trúc đúng. 

**3. Nguyễn Anh Khoa:**
*   **Bối cảnh:** Cần một phương pháp hiện đại tích hợp hệ thống Voucher và webhook qua Sepay - thanh toán chuyển khoản nhanh chóng.
*   **Prompt sử dụng:**
    *   "Sepay là gì, cách hoạt động của Sepay"
    *   "Tích hợp Sepay vào ứng dụng khi khách hàng chọn 'Chuyển khoản ngân hàng', hiện thị pop up qr và có nút khách hàng xác nhận 'Đã chuyển khoản' để hệ thống xác thực."
    *   "Cách hoạt động của webhook Sepay và cách tích hợp vào supabase edge function."
    *   "Dựa vào thiết kế database, thiết kế hệ thống voucher và giao diện, cho phép user chọn các voucher public, hoặc nhập mã để lấy voucher private"
*   **Kết quả:** Ứng dụng đã có nền tảng cấu trúc code sẵn sàng để đẩy luồng Sepay QR Pay và quản lý Private/Public Voucher thông minh trên Database.

**4. Nguyễn Tấn Thắng:**
*   **Bối cảnh:** Cần gộp hàng loạt các màn Hình Rời Rạc về Admin Report, và đồng thời nhúng tính năng phân tích Business trực tiếp bằng model AI.
*   **Prompt sử dụng:**
    *   "Consolidate the existing 'Revenue' features into a single, unified 'Admin Revenue Dashboard.' Implement a data table that shows monthly trends... Add a bar chart to visually represent monthly revenue performance... Integrate the gemini-3-flash-preview AI model to analyze the revenue data and provide automated, human-like commentary and insights. Ensure data fetched reliably... Refactor bottom navigation bar... Resolve build errors... Update .gitignore to hide config/ directory"
*   **Kết quả:** Tùy biến AI cực tốt tạo ra được trang Analytics chuyên nghiệp có nhận xét giống hệt con người và gỡ lỗi XML Nav Bottom hiệu quả. Bảo mật file thành công.

**5. Trần Mạnh Hùng:**
*   **Bối cảnh:** Sửa lại luồng Quản lý địa chỉ giao hàng và Đánh giá / Phản hồi sát với thực tế sử dụng của App hơn.
*   **Prompt sử dụng:**
    *   "Hãy chỉnh sửa phần đánh giá tui theo giao diện như ảnh cho phép người dùng đăng nhiều ảnh khi viết đánh giá, và fix lại tính trung bình tổng đánh giá luôn, tách viết đánh giá thành 1 trang riêng"
    *   "Đổi tab phản hồi thành tab chat , với user thì sẽ chat với nhiều admin quán admin nào trả lời cũng được bên acc user chỉ hiện 1 khung chat với admin, còn admin thì có nhiều khung chat ứng với mỗi user"
    *   "Bỏ quản lý địa chỉ ra khỏi profile cá nhân, địa chỉ chỉ được thêm vào trong lúc đang chọn món ăn, tạo 1 file json chứa các địa chỉ tỉnh thành phố của toàn quốc, chỉnh sửa lại khung nhập địa chỉ cho chính xác hơn"
*   **Kết quả:** Luồng chức năng Đánh Giá đã hỗ trợ đa ảnh, Form Checkout tự bắt địa chỉ Dropdown chuẩn từ JSON và thay thế được mục Phản hồi 1 chiều thành Chatbox tương tác realtime.
