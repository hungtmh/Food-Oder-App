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

### 2.1 Quản lý tài khoản và Hồ sơ (User)
- **Đăng nhập:** Hỗ trợ đăng nhập bằng Email và mật khẩu, chức năng ghi nhớ đăng nhập. Tự động phân quyền và điều hướng người dùng (Admin vào Dashboard, User vào trang chủ).
  *(Hình minh họa: Màn hình Đăng nhập có nhập sẵn thông tin)*
- **Đăng ký:** Cho phép đăng ký tài khoản mới với đầy đủ thông tin: Họ tên, Email, Số điện thoại, Mật khẩu. Bắt buộc đồng ý điều khoản.
  *(Hình minh họa: Màn hình Đăng ký tài khoản)*
- **Quên mật khẩu & Đổi mật khẩu:** Hỗ trợ gửi mã xác nhận qua email (Sử dụng Gmail SMTP). Đổi mật khẩu yêu cầu nhập lại mật khẩu cũ để xác thực.
  *(Hình minh họa: Màn hình Quên mật khẩu và giao diện Email nhận mã xác nhận)*
- **Hồ sơ cá nhân:** Xem và chỉnh sửa Tên, SĐT, Địa chỉ, và cập nhật Ảnh đại diện hệ thống. (Không cho phép đổi email).
  *(Hình minh họa: Màn hình Profile cá nhân hiển thị ảnh đại diện và thông tin)*

### 2.2 Trang chủ, Tìm kiếm và Khám phá món ăn (User)
- **Slideshow & Banner:** Hiển thị tự động các món phổ biến và Hot Offers (Ưu đãi) dưới dạng hình ảnh lớn chuyển động trên cùng trang chủ.
  *(Hình minh họa: Trang chủ với Banner chuyển động)*
- **Gợi ý món ăn cá nhân hóa:** Hệ thống gợi ý danh sách món ăn dưới dạng Grid 2 cột ở trang chủ, thay đổi dựa trên sở thích và lịch sử của người dùng.
  *(Hình minh họa: Mục "Có thể bạn sẽ thích" trên trang chủ)*
- **Tìm kiếm & Lọc món ăn:** Cung cấp ô tìm kiếm real-time ngay khi gõ. Tích hợp bộ lọc theo các danh mục: Tất cả, Món chính, Đồ uống, Tráng miệng, Khai vị.
  *(Hình minh họa: Màn hình Tìm kiếm với kết quả trả về)*
- **Lịch sử tìm kiếm & Sắp xếp:** Lưu lại các từ khóa khách hàng đã tìm. Tính năng sắp xếp món ăn theo Giá (thấp/cao), và theo đánh giá sao.
  *(Hình minh họa: Mục lịch sử tìm kiếm hiển thị các từ khóa cũ)*

### 2.3 Chi tiết món ăn, Đánh giá và Yêu thích (User)
- **Chi tiết món ăn:** Hiển thị rõ hình ảnh, Tên món, Giá tiền, Điểm đánh giá, và Mô tả chi tiết.
  *(Hình minh họa: Màn hình Chi tiết một món ăn cụ thể)*
- **Yêu thích (Wishlist):** Tính năng bấm thả tim để lưu món vào danh sách yêu thích, xem lại và thêm nhanh vào giỏ hàng từ màn hình Yêu thích.
  *(Hình minh họa: Màn hình Danh sách món ăn đã lưu thả tim)*
- **Đánh giá & Nhận xét:** Người dùng viết đánh giá (1-5 sao) và đính kèm hình ảnh thực tế sau khi mua. Xem nhận xét của những người dùng khác.
  *(Hình minh họa: Màn hình Viết đánh giá món ăn có chọn ảnh)*

### 2.4 Giỏ hàng và Quản lý địa chỉ (User)
- **Giỏ hàng:** Thêm món từ trang chi tiết, tuỳ chỉnh số lượng tăng giảm, tự động tính tổng tiền. Tính năng vuốt sang trái để xóa món ăn có kèm nút Hoàn tác (Undo). Xóa toàn bộ giỏ.
  *(Hình minh họa: Màn hình Giỏ hàng với danh sách món và tổng tiền)*
- **Quản lý đa địa chỉ giao hàng:** Lưu trữ nhiều địa chỉ cho một người dùng. Tính năng chọn địa chỉ mặc định và chọn nhanh địa chỉ qua Spinner (Tỉnh/Quận/Phường) khi Checkout.
  *(Hình minh họa: Màn hình Quản lý địa chỉ nhận hàng)*

### 2.5 Thanh toán và Đặt hàng (User)
- **Tích hợp thanh toán linh hoạt:** Hỗ trợ thanh toán tiền mặt khi nhận hàng (COD) và tích hợp cổng thanh toán trực tuyến tự động qua SEPay (Quét mã QR).
  *(Hình minh họa: Màn hình Checkout chọn phương thức thanh toán)*
  *(Hình minh họa: Dialog hiển thị QR Code thanh toán SEPay)*
- **Chế độ đặt ăn tại quán:** Người dùng chọn chế độ "Dine in", nhập số bàn và hệ thống bỏ qua bước chọn địa chỉ giao hàng.
  *(Hình minh họa: Chế độ đặt ăn tại bàn trên màn hình Checkout)*
- **Áp dụng Mã giảm giá (Voucher):** Nhập mã hoặc chọn voucher từ danh sách có sẵn, hệ thống tính toán trừ tiền tự động trước khi xác nhận.
  *(Hình minh họa: Giao diện chọn Voucher tại bước thanh toán)*

### 2.6 Quản lý và Lịch sử đơn hàng (User)
- **Theo dõi trạng thái đơn hàng:** Đơn hàng được phân chia màu sắc theo trạng thái (Chờ xác nhận, Đang xử lý, Đang giao, Đã hoàn thành, Đã hủy). Hỗ trợ hủy đơn khi đang chờ.
  *(Hình minh họa: Tab Lịch sử đơn hàng có phân loại trạng thái)*
- **Thông báo đẩy (Push Notification):** Khách hàng nhận thông báo đẩy về điện thoại ngay khi Admin thay đổi trạng thái đơn hàng.
  *(Hình minh họa: Thông báo đẩy hiển thị trên thanh trạng thái điện thoại)*

### 2.7 Chăm sóc khách hàng và Chat (User & Admin)
- **Chat trực tiếp (Real-time):** Khách hàng có thể chat ngay với quán để hỏi thông tin, giục đồ ăn. Admin có màn hình quản lý chat để trả lời lại lập tức.
  *(Hình minh họa: Màn hình Chat giữa khách hàng và Admin)*
- **Liên hệ quán:** Các nút bấm liên kết nhanh đến Facebook, Skype, Gọi điện, Zalo của nhà hàng.
  *(Hình minh họa: Màn hình Thông tin Liên hệ)*

### 2.8 Quản lý Món ăn và Danh mục (Admin)
- **CRUD Danh mục:** Thêm mới, chỉnh sửa, xóa và quản lý chi tiết các danh mục món ăn.
  *(Hình minh họa: Màn hình Quản lý danh mục của Admin)*
- **CRUD Món ăn:** Thêm món ăn với hình ảnh, giá, mô tả, đánh dấu gợi ý/phổ biến. Xem danh sách món với trạng thái Còn/Hết hàng.
  *(Hình minh họa: Màn hình Quản lý món ăn và Form thêm món mới)*

### 2.9 Quản lý Đơn hàng và Voucher (Admin)
- **Quản lý đơn hàng toàn diện:** Xem danh sách đơn, lọc theo trạng thái, cập nhật trạng thái đơn cho khách (Tự động trigger Notification).
  *(Hình minh họa: Màn hình Quản lý đơn hàng Admin)*
- **Hệ thống Voucher:** Tạo mã giảm giá (theo % hoặc số tiền cố định), thiết lập thời hạn, theo dõi số lượt dùng và vô hiệu hóa mã khi cần.
  *(Hình minh họa: Màn hình Quản lý Voucher của Admin)*

### 2.10 Dashboard AI, Thống kê và Báo cáo (Admin)
- **Theo dõi doanh thu:** Thống kê doanh thu theo ngày, theo khoảng thời gian tùy chọn. Hiển thị danh sách Top 10 món bán chạy nhất.
  *(Hình minh họa: Màn hình Doanh thu và Top món bán chạy)*
- **Phân tích xu hướng tháng:** Biểu đồ kép trực quan theo dõi lượng đơn và doanh thu tháng, kết hợp AI đưa ra lời khuyên.
  *(Hình minh họa: Biểu đồ thống kê kết hợp nhận xét từ AI)*
- **AI Insights & Phân tích cảm xúc:** Hệ thống AI (Qwen/Hugging Face) quét tất cả đánh giá, thống kê tỷ lệ Tích cực/Tiêu cực bằng biểu đồ tròn. Đưa ra dự đoán món Hot Seller, Declining và At Risk.
  *(Hình minh họa: Dashboard AI phân tích đánh giá và dự đoán món ăn)*

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
