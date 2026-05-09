# Phát triển phần mềm cho thiết bị di động Báo cáo đồ án

## 1 TỰ ĐÁNH GIÁ ĐỒ ÁN

**Điểm tự đánh giá cho toàn đồ án: 9.5 / 10.**

**Lý do:**
Nhóm đã hoàn thành xuất sắc các yêu cầu cơ bản và thực hiện thêm rất nhiều chức năng bổ sung có độ khó và phức tạp cao. Thẩm mỹ tốt, hiệu năng và tốc độ ổn định. Ứng dụng tích hợp AI phân tích cảm xúc (Hugging Face / Qwen), thanh toán QR qua SEPay, hỗ trợ chat trực tiếp User-Admin, biểu đồ doanh thu phân tích xu hướng và có sự đầu tư mạnh mẽ về tính tiện dụng cũng như giao diện. Không tốn quá nhiều thời gian nhưng có đóng góp lớn và đồng đều từ các thành viên. Số lượng chức năng hoàn thành vượt xa yêu cầu chuẩn (88 chức năng/task lớn nhỏ).

### 1.1 Mô tả dự án

**a. Tên của dự án:** Ứng dụng đặt món ăn (Food Order App)

**b. Môi trường thực thi:** Thiết bị di động hệ điều hành Android (Sử dụng Java, Android Studio).

**c. Mục tiêu của chương trình:**
Cung cấp nền tảng đặt món ăn trực tuyến tiện lợi cho người dùng (khách hàng) và hệ thống quản lý toàn diện cho chủ nhà hàng (Admin), hỗ trợ từ khâu tiếp nhận đơn hàng, quản lý món ăn đến thống kê doanh thu và phân tích cảm xúc khách hàng.

**d. Lý do ra đời của dự án:**
Xuất phát từ nhu cầu thực tế về việc số hóa quy trình kinh doanh của nhà hàng, quán ăn nhỏ lẻ không muốn phụ thuộc hoàn toàn vào các nền tảng bên thứ ba với mức chiết khấu cao. Ứng dụng ra đời nhằm giúp quán tự quản lý đơn hàng, theo dõi khách hàng và tích hợp AI để cải thiện chất lượng dịch vụ, mang lại sự chủ động trong kinh doanh.

**e. Các phần mềm có chức năng tương tự hoặc có liên quan:**
- **ShopeeFood / GrabFood:** Đây là các ứng dụng đa quán, có quy mô lớn. 
  - *Ưu điểm:* Lượng người dùng đông, hệ thống giao nhận mạnh. 
  - *Nhược điểm:* Thu phí chiết khấu cao đối với nhà hàng, giao diện phức tạp và không thể tùy biến riêng cho một quán cụ thể.
- **The Coffee House / KFC Vietnam App:**
  - *Ưu điểm:* Ứng dụng chuyên biệt cho một thương hiệu, giao diện đẹp, giữ chân khách hàng tốt.
  - *Nhược điểm:* Chỉ dành cho chuỗi lớn có nhiều vốn. Đồ án này là giải pháp tương tự nhưng dễ triển khai cho các nhà hàng quy mô vừa và nhỏ.

**f. Điểm khác biệt của chương trình:**
- Tích hợp AI (Hugging Face / Qwen / Gemini) phân tích cảm xúc đánh giá, dự đoán xu hướng món ăn (Món bán chạy, món có nguy cơ giảm doanh thu).
- Thanh toán tiện lợi với SEPay QR Payment tự động xác nhận.
- Tích hợp Chat trực tiếp giữa Admin và User ngay trong ứng dụng để thúc giục món hoặc hỗ trợ khách hàng.
- Biểu đồ kép phân tích xu hướng tháng tích hợp Gemini AI cho Admin.
- Logic đặt ăn tại quán và "Đang giao" cho các phương thức khác nhau.

### 1.2 Đóng góp của các thành viên cho dự án

**Tỉ lệ đóng góp**

| STT | MSSV | Họ và tên | Tỉ lệ |
| --- | ---- | --------- | ----- |
| 01 | (Cần bổ sung) | Hùng (hungtmh) | 20% |
| 02 | (Cần bổ sung) | Cường (Dicun) | 20% |
| 03 | (Cần bổ sung) | Khoa (wokovn) | 20% |
| 04 | (Cần bổ sung) | Thắng (thangak18) | 20% |
| 05 | (Cần bổ sung) | Khải (nvkhai238) | 20% |
| | | **Tổng:** | **100%** |

*(Ghi chú: Cả 5 thành viên đều có khối lượng chức năng lập trình chuyên sâu, có độ khó cao và đồng đều nhau.)*

**Chi tiết các công việc đã thực hiện**

| STT | SV thực hiện | Tên chức năng / công việc | Chú ý |
| --- | ------------ | ------------------------- | ----- |
| 1 | Hùng | Quản lý tài khoản (Đăng nhập, Đăng ký, Quên mật khẩu, Đổi mật khẩu) | Phân quyền Admin/User, mã xác nhận Gmail |
| 2 | Hùng | Quản lý hồ sơ, Cập nhật avatar hệ thống | UI đổi mới |
| 3 | Hùng | Tìm kiếm món ăn, lọc danh mục, lịch sử tìm kiếm, sắp xếp món | Real-time search |
| 4 | Hùng | Lưu món yêu thích, liên hệ quán, đánh giá chi tiết sản phẩm | Hỗ trợ ảnh review |
| 5 | Hùng | Chat User-Admin, logic đặt ăn tại quán, bảo mật API | Nâng cấp từ Góp ý |
| 6 | Cường | Hiển thị món phổ biến (Slideshow), gợi ý món ăn | Trang chủ động |
| 7 | Cường | Hot Offers & Banner, Personalized Recommendations | UI hấp dẫn |
| 8 | Cường | Push Notification khi thay đổi trạng thái | FCM |
| 9 | Khoa | Giỏ hàng, thay đổi số lượng, xóa/hoàn tác xóa món | Snackbar, Swipe to delete |
| 10 | Khoa | Đặt hàng, xác nhận đặt hàng, quản lý địa chỉ nâng cao | Lưu nhiều địa chỉ |
| 11 | Khoa | Lịch sử đơn hàng, xem chi tiết, hủy đơn, thanh toán COD | Phân tab trạng thái |
| 12 | Khoa | Thanh toán bằng QR code qua SEPay, tích hợp voucher thanh toán | Webhook tự động |
| 13 | Thắng | Quản lý món ăn, danh mục món ăn (CRUD cho Admin) | Xử lý ảnh |
| 14 | Thắng | Quản lý đơn hàng (Admin), xem chi tiết, cập nhật trạng thái | Cập nhật realtime |
| 15 | Thắng | Thống kê đơn hàng (tổng đơn, biểu đồ, UI dashboard thống kê) | UI trực quan |
| 16 | Thắng | Theo dõi doanh thu theo ngày, khoảng thời gian, top 10 món | Lọc linh hoạt |
| 17 | Thắng | Báo cáo & thống kê nâng cao (Monthly Trend Analysis + Gemini AI) | Biểu đồ kép |
| 18 | Khải | Phân tích cảm xúc đánh giá bằng AI, thống kê cảm xúc | Sentiment analysis |
| 19 | Khải | Dự đoán xu hướng món ăn, Dashboard AI Insights | Predictive analysis |
| 20 | Khải | Nâng cấp AI (API call, chuyển đổi Gemini -> Hugging Face -> Qwen) | Rule-based logic |
| 21 | Khải | Quản lý phản hồi khách hàng, gửi thông báo đẩy (Admin) | |
| 22 | Khải | Quản lý Voucher (Tạo, sửa, xóa, vô hiệu hóa, xem thống kê) | Khuyến mãi |

### 1.3 Thông tin cần thiết để thực thi chương trình

- **Cách cài đặt:** Clone source code từ repo GitHub. Mở project bằng Android Studio (đảm bảo cài đặt Android SDK API 34+).
- **Môi trường:** Java 11, Gradle phiên bản mới nhất. Đồng bộ hóa Gradle (Sync Project with Gradle Files).
- **Cơ sở dữ liệu:** Cấu hình file `supabase_schema.sql` và các file `migration.sql` vào Supabase SQL Editor.
- **Biến môi trường (Security):** Thiết lập Supabase URL, Supabase Anon Key, API Key của AI (Hugging Face / Qwen) vào file `local.properties` để build.
- **Tài khoản test (ví dụ):**
  - Admin: `admin@gmail.com` / Mật khẩu: `123456`
  - User: `user@gmail.com` / Mật khẩu: `123456`

---

## 2 CÁC CHỨC NĂNG ĐÃ THỰC HIỆN

### 2.1 Quản lý tài khoản (Đăng nhập / Đăng ký / Quên mật khẩu)
- **Mô tả:** Hệ thống cho phép người dùng đăng ký tài khoản mới, đăng nhập với quyền phân biệt (Admin/User), khôi phục mật khẩu qua mã xác nhận Gmail SMTP và quản lý hồ sơ cá nhân.
- **Luồng sự kiện chính:** Người dùng nhập Email, Mật khẩu. Bấm Đăng nhập -> Hệ thống gọi API kiểm tra -> Điều hướng vào Main User hoặc Dashboard Admin tùy quyền.
*(Hình minh họa: Màn hình đăng nhập điền sẵn user/pass và màn hình thông tin cá nhân của người dùng)*

### 2.2 Đặt hàng và Giỏ hàng
- **Mô tả:** Thêm món ăn vào giỏ, điều chỉnh số lượng, vuốt để xóa, quản lý địa chỉ nhận hàng và tiến hành checkout với nhiều phương thức thanh toán.
- **Luồng sự kiện chính:** Khách hàng ở màn hình chi tiết món -> chọn số lượng -> Thêm vào giỏ. Vào Giỏ hàng -> Chọn địa chỉ -> Áp dụng mã Voucher -> Chọn thanh toán COD hoặc SEPay QR -> Xác nhận đặt hàng -> Đơn hàng chuyển sang trạng thái chờ xác nhận.
*(Hình minh họa: Màn hình giỏ hàng chứa 3 món ăn và tổng tiền; Màn hình checkout với Spinner Tỉnh/Thành/Phường/Quận)*

### 2.3 Thanh toán quét mã QR SEPay
- **Mô tả:** Tích hợp cổng thanh toán SEPay giúp khách hàng quét mã QR để thanh toán tự động, hệ thống dùng webhook để lắng nghe giao dịch.
- **Luồng sự kiện chính:** Khách chọn thanh toán QR -> Màn hình hiển thị QR Code của ngân hàng -> Khách dùng app ngân hàng quét -> SEPay báo thành công -> Hệ thống tự đổi trạng thái đơn hàng.
*(Hình minh họa: Màn hình hiển thị mã QR thanh toán có số tiền của đơn hàng)*

### 2.4 Quản lý đơn hàng và món ăn (Admin)
- **Mô tả:** Màn hình dành cho Admin giúp theo dõi toàn bộ đơn đặt hàng, thay đổi trạng thái (Chờ xác nhận, Đang xử lý, Đang giao, Hoàn thành), và CRUD món ăn.
- **Luồng sự kiện chính:** Admin đăng nhập -> Vào mục Đơn hàng -> Nhấn vào một đơn hàng Mới -> Đổi trạng thái sang "Đang chế biến" -> Hệ thống tự động đẩy push notification (FCM) đến điện thoại của khách hàng.
*(Hình minh họa: Danh sách đơn hàng đa màu sắc theo trạng thái; Form thêm món ăn mới)*

### 2.5 Báo cáo thống kê và Doanh thu (Admin)
- **Mô tả:** Biểu đồ trực quan thống kê đơn hàng, doanh thu theo ngày/khoảng thời gian. Phân tích xu hướng tháng kép kết hợp Gemini AI.
- **Luồng sự kiện chính:** Admin vào mục Thống kê -> Chọn khoảng thời gian -> Biểu đồ cột hiển thị doanh thu, danh sách top 10 món bán chạy nhất hiện ra.
*(Hình minh họa: Biểu đồ doanh thu tháng 10 và Top 10 món bán chạy nhất)*

### 2.6 Trí tuệ nhân tạo (AI Insights & Sentiment Analysis)
- **Mô tả:** Sử dụng API của Hugging Face/Qwen/Gemini để phân tích cảm xúc (Tích cực, Tiêu cực, Trung tính) từ các review của khách hàng. Dự đoán món nào sắp hot, món nào giảm sức hút.
- **Luồng sự kiện chính:** AI tự động quét đánh giá mới -> Dashboard AI cập nhật tỷ lệ cảm xúc -> Gợi ý Admin các món cần cải thiện chất lượng.
*(Hình minh họa: Dashboard AI với biểu đồ tròn cảm xúc và danh sách món "At Risk")*

### 2.7 Chat trực tiếp User - Admin
- **Mô tả:** Tính năng nhắn tin thời gian thực giúp khách hàng trao đổi trực tiếp với quán, giục đơn hoặc hỏi chi tiết món.
- **Luồng sự kiện chính:** User mở màn hình Chat -> Nhập tin nhắn "Đơn của em bao giờ tới ạ?" -> Admin nhận được thông báo -> Trả lời khách ngay lập tức.
*(Hình minh họa: Giao diện khung chat bong bóng giữa Khách và Quán)*

### 2.8 Quản lý Voucher (Admin & User)
- **Mô tả:** Admin tạo mã giảm giá (VD: Giảm 20%, giảm 50k), set giới hạn thời gian. User nhập mã vào giỏ hàng để được trừ tiền.
- **Luồng sự kiện chính:** Admin tạo mã khuyến mãi "GIAM20" -> Khách áp mã tại màn hình Checkout -> Hệ thống kiểm tra điều kiện -> Trừ tiền trực tiếp vào tổng thanh toán.
*(Hình minh họa: Màn hình danh sách Voucher của Admin và phần áp mã thành công ở User)*

---

## 3 NHỮNG ĐIỂM ĐẶC BIỆT TRONG ĐỒ ÁN

- **Sử dụng Trí tuệ nhân tạo (AI):** Khác với các app đặt đồ ăn thông thường, ứng dụng tích hợp NLP Models (Hugging Face / Qwen) để phân tích cảm xúc đánh giá tự động và dự đoán xu hướng món ăn, giúp chủ quán kinh doanh tốt hơn.
- **Tự động hóa thanh toán ngân hàng:** Tích hợp nền tảng mã mở SEPay để nhận diện chuyển khoản tự động qua QR code, mang lại trải nghiệm tiện lợi không kém các ví điện tử lớn.
- **Kiến trúc Cloud & Bảo mật:** Sử dụng Supabase thay thế Firebase truyền thống (PostgreSQL + REST API) giúp quản lý dữ liệu quan hệ mạnh mẽ. Các API Key quan trọng được mã hóa và ẩn trong `local.properties` để không bị push lên Git.
- **Real-time Notifications & Chat:** Ứng dụng tích hợp Firebase Cloud Messaging để Push notification và có chức năng Chat trực tiếp hỗ trợ khách hàng tức thì.
- **Giao diện đa dạng và thẩm mỹ cao:** Tự thiết kế hệ thống Slideshow, Banners, giao diện Chat, Dashboard thống kê với các biểu đồ trực quan (Bar chart, Pie chart), Custom Bottom Navigation Bar.
- **Phát triển liên tục, quy trình rõ ràng:** Nhóm đã commit 98 lần trong 2 tháng với danh sách 88 tính năng (bao gồm cả các tính năng bổ sung không có trong kế hoạch ban đầu) chứng tỏ sự tập trung và cường độ làm việc nghiêm túc.

---

## 4 CÁC THAM KHẢO

**SÁCH / TÀI LIỆU**
1. Documentations của Android Developers, Google. Năm xuất bản: 2024.
2. Supabase Official Documentation (PostgreSQL for Mobile).

**BÀI VIẾT / BÀI HƯỚNG DẪN / NỀN TẢNG**
1. **Material Design 3 cho Android**
   - Liên kết: https://m3.material.io/
   - Lần truy cập cuối: 10/04/2026
2. **Hướng dẫn tích hợp thanh toán SEPay QR**
   - Liên kết: https://sepay.vn/docs/
   - Lần truy cập cuối: 26/03/2026
3. **Hugging Face Inference API / Qwen Documentation**
   - Liên kết: https://huggingface.co/docs/api-inference/index
   - Lần truy cập cuối: 03/04/2026
4. **Retrofit & OkHttp Networking in Android**
   - Liên kết: https://square.github.io/retrofit/
   - Lần truy cập cuối: 20/03/2026
