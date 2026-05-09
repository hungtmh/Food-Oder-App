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

### 2.1 Quản lý tài khoản
- **Mô tả:** Đăng nhập, đăng ký, quên mật khẩu (gửi mã qua email) và đổi mật khẩu. Phân quyền tự động Admin/User.
- *(Hình minh họa: Màn hình Đăng nhập / Màn hình Đăng ký / Màn hình Quên mật khẩu)*

### 2.2 Quản lý Hồ sơ cá nhân
- **Mô tả:** Xem và cập nhật các thông tin cá nhân như Tên, SĐT, Địa chỉ, và ảnh đại diện.
- *(Hình minh họa: Màn hình Profile cá nhân của người dùng)*

### 2.3 Hiển thị món phổ biến (Slideshow)
- **Mô tả:** Slideshow hình ảnh các món ăn phổ biến trên cùng trang chủ, tự động chuyển đổi.
- *(Hình minh họa: Khung Slideshow trên trang chủ)*

### 2.4 Gợi ý món ăn (Personalized Recommendations)
- **Mô tả:** Hệ thống gợi ý danh sách món ăn dưới dạng Grid 2 cột ở trang chủ dựa trên sở thích người dùng.
- *(Hình minh họa: Mục "Có thể bạn sẽ thích" trên trang chủ)*

### 2.5 Banner ưu đãi (Hot Offers & Banner)
- **Mô tả:** Giao diện hiển thị các ưu đãi hot, banner quảng cáo.
- *(Hình minh họa: Giao diện Hot Offers ở Trang chủ)*

### 2.6 Tìm kiếm món ăn
- **Mô tả:** Ô tìm kiếm trả về kết quả ngay lập tức (real-time) khi người dùng gõ từ khóa. Lưu lịch sử tìm kiếm.
- *(Hình minh họa: Màn hình Tìm kiếm và kết quả trả về)*

### 2.7 Lọc và sắp xếp món ăn
- **Mô tả:** Bộ lọc đa dạng: Tất cả, Món chính, Đồ uống... và tính năng sắp xếp theo giá, theo đánh giá.
- *(Hình minh họa: Menu lọc và sắp xếp món ăn)*

### 2.8 Chi tiết món ăn
- **Mô tả:** Hiển thị rõ hình ảnh, Tên món, Giá tiền, Điểm đánh giá, và Mô tả chi tiết trước khi thêm vào giỏ.
- *(Hình minh họa: Màn hình Chi tiết một món ăn cụ thể)*

### 2.9 Yêu thích sản phẩm (Wishlist)
- **Mô tả:** Người dùng bấm thả tim để lưu món vào danh sách yêu thích, hỗ trợ thêm nhanh món từ danh sách này vào giỏ.
- *(Hình minh họa: Màn hình Danh sách món ăn yêu thích)*

### 2.10 Giỏ hàng
- **Mô tả:** Quản lý món trong giỏ: tăng/giảm số lượng, tính tổng tiền. Tính năng vuốt trái để xóa món ăn có hoàn tác.
- *(Hình minh họa: Màn hình Giỏ hàng với các món ăn đã chọn)*

### 2.11 Đặt hàng
- **Mô tả:** Hỗ trợ nhập Tên, SĐT, chọn phương thức thanh toán, nhập ghi chú đơn hàng. Hỗ trợ đặt ăn tại quán (Dine-in).
- *(Hình minh họa: Màn hình Checkout chuẩn bị đặt hàng)*

### 2.12 Quản lý địa chỉ giao hàng nâng cao
- **Mô tả:** Lưu trữ nhiều địa chỉ cho một người dùng. Spinner chọn Tỉnh/Quận/Phường nhanh chóng khi Checkout.
- *(Hình minh họa: Màn hình Quản lý địa chỉ và Spinner chọn địa chỉ)*

### 2.13 Thanh toán COD và SEPay QR
- **Mô tả:** Tích hợp 2 hình thức: Trả tiền mặt (COD) và Quét mã QR thanh toán ngân hàng tự động nhận tiền qua SEPay webhook.
- *(Hình minh họa: Mã QR thanh toán SEPay hiển thị trên ứng dụng)*

### 2.14 Khuyến mãi & Mã giảm giá
- **Mô tả:** Khách hàng có thể nhập mã voucher hoặc chọn từ danh sách để được trừ trực tiếp tiền trong đơn hàng.
- *(Hình minh họa: Giao diện chọn và áp dụng Voucher thành công)*

### 2.15 Lịch sử đơn hàng (User)
- **Mô tả:** Xem lại đơn hàng đã đặt, phân loại theo tab trạng thái (Chờ xác nhận, Đang xử lý, Đang giao, Đã hoàn thành/hủy). Cho phép hủy đơn.
- *(Hình minh họa: Tab Lịch sử đơn hàng có phân loại trạng thái)*

### 2.16 Phản hồi & Đánh giá sản phẩm
- **Mô tả:** Người dùng viết đánh giá sao (1-5), có thể đính kèm hình ảnh thực tế sau khi trải nghiệm.
- *(Hình minh họa: Màn hình Đánh giá món ăn có up hình ảnh)*

### 2.17 Chat trực tiếp User ↔ Admin
- **Mô tả:** Hệ thống nhắn tin real-time giúp khách hàng trao đổi trực tiếp với quán để hỏi thông tin, giục đồ ăn.
- *(Hình minh họa: Giao diện Chat trực tiếp giữa Khách và Quán)*

### 2.18 Thông tin liên hệ quán
- **Mô tả:** Các nút liên kết chuyển hướng nhanh tới Facebook, Gọi điện thoại, Zalo của quán.
- *(Hình minh họa: Màn hình Thông tin Liên hệ)*

### 2.19 Quản lý danh mục món ăn (Admin)
- **Mô tả:** Chức năng cho Admin thêm, sửa, xóa, hiển thị danh mục phân loại đồ ăn.
- *(Hình minh họa: Màn hình Quản lý danh mục Admin)*

### 2.20 Quản lý món ăn (Admin)
- **Mô tả:** Admin thêm món mới, cập nhật giá, thêm hình ảnh, thiết lập món gợi ý và tình trạng còn/hết hàng.
- *(Hình minh họa: Màn hình Quản lý món ăn Admin)*

### 2.21 Quản lý đơn hàng & Trạng thái (Admin)
- **Mô tả:** Admin theo dõi đơn hàng, thay đổi các trạng thái: Chờ xác nhận → Chờ chế biến → Đang giao → Hoàn thành.
- *(Hình minh họa: Màn hình Quản lý đơn hàng Admin nhiều màu sắc)*

### 2.22 Quản lý Voucher (Admin)
- **Mô tả:** Tạo mã giảm giá (giảm % hoặc trừ tiền), set thời gian, vô hiệu hóa mã, thống kê lượt dùng mã.
- *(Hình minh họa: Màn hình Quản lý Voucher của Admin)*

### 2.23 Theo dõi doanh thu và Thống kê (Admin)
- **Mô tả:** Thống kê tổng đơn, doanh thu theo ngày/tháng, danh sách Top 10 món bán chạy nhất.
- *(Hình minh họa: Màn hình Thống kê doanh thu và Top món)*

### 2.24 Báo cáo phân tích xu hướng tháng (Admin)
- **Mô tả:** Biểu đồ kép trực quan theo dõi lượng đơn và doanh thu tháng, đưa ra nhận xét dựa trên dữ liệu.
- *(Hình minh họa: Màn hình biểu đồ Monthly Trend Analysis)*

### 2.25 Tính năng AI: Phân tích cảm xúc & Dự đoán
- **Mô tả:** Dùng Hugging Face/Qwen/Gemini quét các bài đánh giá để phân tích cảm xúc (Positive/Negative). Dự đoán món Hot/At Risk.
- *(Hình minh họa: Dashboard AI tổng quan Insights của quán)*

### 2.26 Tính năng Thông báo đẩy (Push Notification)
- **Mô tả:** Sử dụng Firebase Cloud Messaging để gửi thông báo lập tức đến người dùng khi đơn hàng đổi trạng thái.
- *(Hình minh họa: Thông báo đẩy hiển thị trên điện thoại)*

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
