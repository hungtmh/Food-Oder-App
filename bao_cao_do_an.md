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

*(Ghi chú: Các chức năng chính về luồng mua hàng và quản trị được đưa lên trước, các chức năng phụ trợ và tài khoản được để ở phần sau)*

### 2.1 Hiển thị món phổ biến (Slideshow) và Trang chủ
- **Mô tả:** Chức năng hiển thị các món ăn nổi bật hoặc phổ biến nhất dưới dạng slideshow ảnh lớn chuyển động trên cùng trang chủ, giúp thu hút sự chú ý của người dùng ngay khi mở app.
- **Luồng sự kiện chính:** Người dùng mở ứng dụng -> Trang chủ tải danh sách món phổ biến từ database -> Hiển thị trên slideshow tự động trượt ngang sau mỗi 3 giây. Người dùng bấm vào ảnh sẽ chuyển đến chi tiết món.
- *(Hình minh họa: Màn hình trang chủ với Slideshow đang hiển thị hình ảnh một món ăn bắt mắt, bên dưới là danh sách các danh mục)*

### 2.2 Gợi ý món ăn (Personalized Recommendations)
- **Mô tả:** Hệ thống tự động gợi ý danh sách món ăn dưới dạng lưới (grid) 2 cột trên trang chủ, mang tính cá nhân hóa để tăng tỷ lệ chuyển đổi.
- **Luồng sự kiện chính:** Người dùng lướt xuống dưới trên trang chủ -> App gọi API lấy danh sách gợi ý dựa trên lịch sử xem/mua hàng -> Hiển thị danh sách món với tên, giá, ảnh.
- *(Hình minh họa: Mục "Có thể bạn sẽ thích" hiển thị ít nhất 4 món ăn đa dạng có hình ảnh rõ nét và giá tiền)*

### 2.3 Banner ưu đãi (Hot Offers)
- **Mô tả:** Giao diện chuyên biệt hiển thị các chương trình khuyến mãi, giảm giá hot nhất của quán thông qua các banner quảng cáo trực quan.
- **Luồng sự kiện chính:** Quản trị viên cập nhật banner -> Người dùng mở app thấy mục Hot Offers -> Bấm vào banner để xem chi tiết danh sách món được giảm giá hoặc copy mã.
- *(Hình minh họa: Giao diện chứa các banner khuyến mãi như "Giảm 20% cho đơn đầu tiên")*

### 2.4 Tìm kiếm món ăn Real-time
- **Mô tả:** Chức năng tìm kiếm món ăn nhanh chóng với kết quả trả về ngay lập tức mỗi khi người dùng gõ thêm một ký tự, giúp tiết kiệm thời gian.
- **Luồng sự kiện chính:** Người dùng nhập từ khóa "Gà" vào thanh tìm kiếm -> Ứng dụng truy vấn database liên tục -> Hiển thị ngay lập tức danh sách các món có chứa từ "Gà" (VD: Gà rán, Gà xối mỡ).
- *(Hình minh họa: Màn hình tìm kiếm có sẵn từ khóa "Gà" và danh sách 3-4 món ăn kết quả hiện ra bên dưới)*

### 2.5 Lọc và sắp xếp món ăn
- **Mô tả:** Cung cấp bộ lọc theo danh mục (Món chính, Đồ uống, Tráng miệng) và sắp xếp kết quả theo các tiêu chí như giá tăng/giảm hoặc theo đánh giá sao.
- **Luồng sự kiện chính:** Tại màn hình danh sách -> Người dùng chọn tab "Đồ uống" và chọn sắp xếp "Giá thấp đến cao" -> Danh sách cập nhật lại chỉ hiện đồ uống, ly rẻ nhất đứng đầu.
- *(Hình minh họa: Màn hình kết quả đã được lọc theo tab "Đồ uống" và sắp xếp giá tăng dần)*

### 2.6 Chi tiết món ăn
- **Mô tả:** Màn hình cung cấp toàn bộ thông tin chi tiết về một món ăn bao gồm hình ảnh phóng to, tên, giá, điểm số đánh giá trung bình và mô tả nguyên liệu.
- **Luồng sự kiện chính:** Người dùng bấm vào một món ăn -> Ứng dụng truy xuất thông tin chi tiết -> Hiển thị đầy đủ thông tin, người dùng có thể bấm nút (+) hoặc (-) để chỉnh số lượng trước khi "Thêm vào giỏ".
- *(Hình minh họa: Màn hình chi tiết món "Pizza Hải Sản" với giá tiền, điểm 4.8 sao, đoạn mô tả chi tiết và số lượng đang chọn là 2)*

### 2.7 Giỏ hàng
- **Mô tả:** Quản lý các món ăn khách hàng đã chọn mua, tính toán tổng tiền tạm tính. Tính năng vuốt để xóa tiện lợi.
- **Luồng sự kiện chính:** Người dùng thêm món từ chi tiết -> Vào Giỏ hàng xem danh sách. Người dùng vuốt một món sang trái -> Món bị xóa khỏi giỏ, hiện thanh Snackbar cho phép "Hoàn tác" (Undo). Tổng tiền lập tức giảm xuống.
- *(Hình minh họa: Màn hình Giỏ hàng chứa 3 món khác nhau với số lượng cụ thể, hiển thị tổng tiền ở góc dưới cùng)*

### 2.8 Quản lý địa chỉ giao hàng nâng cao
- **Mô tả:** Tính năng lưu trữ và quản lý nhiều địa chỉ cho một người dùng. Hỗ trợ chọn nhanh địa chỉ cấp Tỉnh/Quận/Phường qua Spinner.
- **Luồng sự kiện chính:** Người dùng vào mục Địa chỉ -> Bấm Thêm mới -> Chọn Tỉnh, Quận, Phường từ menu thả xuống (Spinner), nhập số nhà -> Lưu lại để dùng cho các lần đặt sau.
- *(Hình minh họa: Màn hình Quản lý địa chỉ hiển thị danh sách 2 địa chỉ đã lưu (Nhà riêng, Công ty))*

### 2.9 Áp dụng Khuyến mãi & Mã giảm giá (Voucher)
- **Mô tả:** Cho phép khách hàng nhập mã giảm giá hoặc chọn voucher từ danh sách có sẵn để được trừ trực tiếp tiền trong đơn hàng.
- **Luồng sự kiện chính:** Ở màn hình Checkout -> Người dùng chọn "Áp dụng Voucher" -> Chọn mã giảm 20k -> Hệ thống kiểm tra điều kiện (tổng đơn > 100k) -> Thành công, tổng tiền thanh toán tự động giảm 20k.
- *(Hình minh họa: Màn hình Checkout hiển thị mục Voucher đã được áp dụng "GIAM20K" và dòng trừ tiền 20.000đ)*

### 2.10 Đặt hàng (Checkout)
- **Mô tả:** Quá trình tổng hợp thông tin cuối cùng bao gồm địa chỉ nhận, danh sách món, ghi chú, phương thức thanh toán trước khi gửi yêu cầu lên hệ thống. Hỗ trợ cả đặt ăn tại bàn (Dine-in).
- **Luồng sự kiện chính:** Người dùng rà soát lại đơn -> Nhập ghi chú "Không hành" -> Bấm "Xác nhận đặt hàng" -> Ứng dụng tạo đơn trên Supabase, trả về thông báo thành công và chuyển đơn sang trạng thái "Chờ xác nhận".
- *(Hình minh họa: Màn hình Checkout đầy đủ thông tin địa chỉ, món, tổng tiền, ghi chú)*

### 2.11 Thanh toán QR Code tự động (SEPay)
- **Mô tả:** Tích hợp thanh toán quét mã QR qua SEPay. Ứng dụng sinh mã QR động chứa sẵn số tài khoản và số tiền cần thanh toán.
- **Luồng sự kiện chính:** Khách chọn thanh toán QR -> Bấm Đặt hàng -> Màn hình hiện mã QR -> Khách dùng app ngân hàng quét và chuyển tiền -> SEPay webhook gửi thông báo về server -> App tự động chuyển trạng thái đơn thành "Đã thanh toán".
- *(Hình minh họa: Dialog hiển thị mã QR thanh toán ngân hàng với số tiền chính xác của đơn hàng)*

### 2.12 Lịch sử đơn hàng và Theo dõi trạng thái
- **Mô tả:** Khách hàng theo dõi tiến độ đơn hàng theo các tab trạng thái rõ ràng: Chờ xác nhận, Đang xử lý, Đang giao, Đã hoàn thành/Hủy.
- **Luồng sự kiện chính:** Khách hàng mở tab Lịch sử -> Chọn mục "Đang xử lý" -> Ứng dụng hiển thị danh sách các đơn đang được nhà hàng chuẩn bị, kèm theo nút Xem chi tiết.
- *(Hình minh họa: Tab Lịch sử đơn hàng (mục Đang xử lý) hiển thị 2 đơn hàng với mã đơn và ngày tháng cụ thể)*

### 2.13 Quản lý đơn hàng (Admin)
- **Mô tả:** Bảng điều khiển trung tâm dành cho chủ quán (Admin) để tiếp nhận đơn hàng mới, duyệt đơn và thay đổi trạng thái giao hàng.
- **Luồng sự kiện chính:** Có đơn mới vào -> Admin xem chi tiết danh sách món -> Bấm "Xác nhận" -> Đơn chuyển sang "Đang chế biến". Tiếp tục bấm "Giao hàng" -> Đơn chuyển sang "Đang giao". Trạng thái được đồng bộ realtime.
- *(Hình minh họa: Màn hình Danh sách Đơn hàng của Admin có các nhãn màu khác nhau như Vàng (Chờ), Xanh dương (Đang xử lý))*

### 2.14 Quản lý món ăn (Admin)
- **Mô tả:** Chức năng (CRUD) cho phép quản trị viên thêm mới, cập nhật giá, hình ảnh, thay đổi trạng thái còn/hết hàng hoặc đánh dấu món ăn gợi ý.
- **Luồng sự kiện chính:** Admin bấm Thêm món -> Điền tên "Trà sữa trân châu", giá 30.000, tải ảnh lên từ thư viện điện thoại, chọn danh mục "Đồ uống" -> Bấm Lưu -> Món mới lập tức xuất hiện trên app của khách.
- *(Hình minh họa: Form Thêm món ăn mới đã được điền đầy đủ dữ liệu ảnh, tên, giá, mô tả)*

### 2.15 Quản lý danh mục món ăn (Admin)
- **Mô tả:** Hỗ trợ Admin tạo và quản lý cấu trúc phân loại món ăn, giúp khách hàng dễ dàng điều hướng tìm kiếm.
- **Luồng sự kiện chính:** Admin chọn Quản lý Danh mục -> Bấm Sửa danh mục "Tráng miệng" -> Đổi tên thành "Đồ ngọt" kèm theo ảnh icon mới -> Lưu lại.
- *(Hình minh họa: Màn hình danh sách các danh mục món ăn của Admin)*

### 2.16 Quản lý Voucher (Admin)
- **Mô tả:** Chức năng phát hành và kiểm soát các chiến dịch khuyến mãi: tạo mã giảm theo % hoặc số tiền cố định, vô hiệu hóa mã khi hết hạn.
- **Luồng sự kiện chính:** Admin nhập mã "TET2026", chọn loại giảm "Phần trăm", mức giảm 10%, thiết lập hạn dùng đến cuối tháng -> Lưu lại. Bảng thống kê hiển thị số lượt mã này đã được khách hàng sử dụng.
- *(Hình minh họa: Màn hình Quản lý Voucher của Admin hiển thị danh sách các mã đang Active/Inactive)*

### 2.17 Theo dõi doanh thu và Thống kê (Admin)
- **Mô tả:** Cung cấp báo cáo tài chính chi tiết với thống kê tổng số đơn, doanh thu theo khoảng thời gian và xếp hạng Top 10 món bán chạy nhất.
- **Luồng sự kiện chính:** Admin chọn ngày bắt đầu (01/03) và ngày kết thúc (31/03) -> App truy xuất database tính tổng doanh thu -> Hiển thị con số (VD: 50.000.000đ) và danh sách Top món bán chạy nhất.
- *(Hình minh họa: Màn hình Thống kê hiển thị tổng doanh thu 50 triệu và danh sách 3 món đứng đầu)*

### 2.18 Báo cáo phân tích xu hướng tháng (Admin)
- **Mô tả:** Biểu đồ kép trực quan (cột và đường) so sánh lượng đơn và doanh thu tháng, kết hợp AI đưa ra lời khuyên chiến lược.
- **Luồng sự kiện chính:** Admin mở tab Xu hướng -> Hệ thống vẽ biểu đồ doanh thu các tháng gần nhất -> Gửi dữ liệu số tới API AI -> AI trả về văn bản nhận xét (VD: "Tháng này doanh thu tăng 20% so với tháng trước nhờ chiến dịch Voucher").
- *(Hình minh họa: Màn hình biểu đồ Monthly Trend Analysis với các cột doanh thu cao/thấp và đoạn text nhận xét của AI)*

### 2.19 Dashboard AI: Phân tích cảm xúc & Dự đoán (Admin)
- **Mô tả:** Sử dụng AI (Hugging Face/Qwen) để quét toàn bộ bài đánh giá của khách, phân loại cảm xúc (Tích cực/Tiêu cực) và dự đoán món ăn nào đang "Hot" hoặc có nguy cơ ế (At Risk).
- **Luồng sự kiện chính:** Admin mở Dashboard AI -> Hệ thống tự động phân tích hàng trăm review -> Vẽ biểu đồ tròn tỷ lệ 80% Positive, 20% Negative. Đưa ra danh sách "Món cần cải thiện" dựa trên các review tiêu cực.
- *(Hình minh họa: Dashboard AI với biểu đồ tròn phân bố cảm xúc nhiều màu và danh sách "At Risk Foods")*

### 2.20 Phản hồi & Đánh giá sản phẩm (User)
- **Mô tả:** Khách hàng viết đánh giá (chấm điểm sao và nhận xét) sau khi nhận được đồ ăn. Có thể đính kèm hình ảnh thực tế.
- **Luồng sự kiện chính:** Khách hàng vào chi tiết món đã mua -> Bấm Đánh giá -> Chấm 5 sao, viết "Đồ ăn rất ngon", đính kèm ảnh chụp -> Gửi đánh giá. Đánh giá này sẽ hiển thị ở trang món ăn và làm dữ liệu cho AI phân tích.
- *(Hình minh họa: Màn hình form viết đánh giá đã điền 5 sao, nội dung nhận xét và đính kèm 1 ảnh)*

### 2.21 Chat trực tiếp User ↔ Admin
- **Mô tả:** Hệ thống nhắn tin real-time giúp khách hàng trao đổi trực tiếp với quán để hỏi thông tin, giục đồ ăn hoặc phản ánh sự cố.
- **Luồng sự kiện chính:** Khách hàng mở chat -> Gửi "Quán ơi đơn của em thêm nhiều tương ớt nhé" -> Tin nhắn đẩy ngay lên màn hình quản lý Chat của Admin -> Admin rep "Dạ vâng quán đã ghi nhận" -> Khách hàng thấy ngay lập tức.
- *(Hình minh họa: Giao diện Chat bong bóng giữa Khách và Quán với lịch sử tin nhắn thực tế)*

### 2.22 Tính năng Thông báo đẩy (Push Notification)
- **Mô tả:** Sử dụng Firebase Cloud Messaging để gửi thông báo tự động đến thiết bị của người dùng, giúp họ không bỏ lỡ thông tin đơn hàng.
- **Luồng sự kiện chính:** Admin đổi trạng thái đơn sang "Đang giao" -> Hệ thống gọi API FCM -> Điện thoại của khách hàng rung và hiển thị popup thông báo "Đơn hàng của bạn đang trên đường giao".
- *(Hình minh họa: Popup thông báo đẩy (push notification) xuất hiện trên thanh trạng thái của điện thoại)*

### 2.23 Yêu thích sản phẩm (Wishlist)
- **Mô tả:** Chức năng lưu trữ các món ăn khách hàng quan tâm nhưng chưa muốn mua ngay.
- **Luồng sự kiện chính:** Khách hàng bấm icon "Trái tim" ở góc món ăn -> Món được lưu vào danh sách Yêu thích. Từ danh sách này khách có thể gạt sang trái để xóa hoặc bấm nút "Thêm vào giỏ" nhanh.
- *(Hình minh họa: Màn hình danh sách yêu thích chứa 2-3 món)*

### 2.24 Thông tin liên hệ quán
- **Mô tả:** Cung cấp thông tin và các nút gọi mở ứng dụng bên thứ 3 (Facebook, Điện thoại, Zalo) để kết nối với nhà hàng.
- **Luồng sự kiện chính:** Người dùng mở màn hình Liên hệ -> Bấm nút "Gọi điện" -> Hệ thống mở app Điện thoại của máy với số hotline điền sẵn.
- *(Hình minh họa: Màn hình Thông tin Liên hệ có các icon social media và số hotline)*

### 2.25 Quản lý tài khoản (Đăng nhập, Đăng ký)
- **Mô tả:** Xác thực người dùng, bảo mật thông tin và phân quyền tự động. Có tích hợp khôi phục mật khẩu qua email.
- **Luồng sự kiện chính:** Người dùng nhập Email và Mật khẩu sai -> Hệ thống báo lỗi. Người dùng chọn "Quên mật khẩu" -> Nhập email -> Hệ thống gửi một mã OTP qua Gmail. Người dùng nhập mã này vào app để tạo mật khẩu mới.
- *(Hình minh họa: Màn hình điền mã xác nhận OTP vừa được gửi từ email)*

### 2.26 Quản lý Hồ sơ cá nhân
- **Mô tả:** Cập nhật các thông tin cá nhân như Tên, SĐT, Địa chỉ mặc định và thay đổi ảnh đại diện.
- **Luồng sự kiện chính:** Người dùng vào Profile -> Đổi Tên thành "Nguyễn Văn A", chọn ảnh mới từ thư viện máy -> Bấm Lưu -> Giao diện toàn app cập nhật avatar mới.
- *(Hình minh họa: Màn hình Profile cá nhân đã điền đầy đủ thông tin Tên, Email, SĐT và một hình đại diện)*

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
