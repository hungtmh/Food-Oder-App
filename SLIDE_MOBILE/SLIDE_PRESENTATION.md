# 📊 NỘI DUNG SLIDE TRÌNH BÀY - ỨNG DỤNG ĐẶT MÓN ĂN

> **Nhóm 06 — Môn học: Phát triển ứng dụng di động**
> Tổng cộng: **25 slides**

---

## 📌 Slide 1: Trang bìa (Title Slide)

**Tiêu đề:** 🍔 **FOOD APPLICATION — Ứng dụng Đặt Món Ăn Online**

**Nội dung:**
- Tên dự án: **Food Application**
- Nhóm: **06**
- Môn học: Phát triển ứng dụng di động
- Giảng viên hướng dẫn: *(điền tên GV)*
- Học kỳ: 2 — Năm học: 2025–2026
- Logo/Ảnh minh họa ứng dụng

---

## 📌 Slide 2: Giới thiệu nhóm

**Tiêu đề:** 👥 **THÀNH VIÊN NHÓM**

| STT | MSSV      | Họ và Tên            | Vai trò                        |
| --- | --------- | -------------------- | ------------------------------ |
| 1   | 23127195  | Trần Mạnh Hùng       | Developer — Tài khoản, Chat, Địa chỉ |
| 2   | 23127033  | Bùi Dương Duy Cường  | Developer — Trang chủ, UI/UX, Đánh giá |
| 3   | 23127391  | Nguyễn Anh Khoa      | Developer — Giỏ hàng, Đặt hàng, Thanh toán |
| 4   | 23127259  | Nguyễn Tấn Thắng     | Developer — Admin: Món ăn, Đơn hàng, Doanh thu |
| 5   | 23127060  | Ninh Văn Khải        | Developer — Admin: Phản hồi, AI features |

---

## 📌 Slide 3: Tổng quan dự án

**Tiêu đề:** 📱 **TỔNG QUAN DỰ ÁN**

**Nội dung:**
- **Mục tiêu:** Xây dựng ứng dụng Android đặt món ăn online hoàn chỉnh với hệ thống quản trị cho Admin
- **Đối tượng người dùng:**
  - 👤 **Khách hàng (User):** Duyệt menu, đặt món, thanh toán, đánh giá
  - 🔧 **Quản trị viên (Admin):** Quản lý món ăn, đơn hàng, doanh thu, AI phân tích
- **Nền tảng:** Android (Java)
- **Backend:** Supabase (PostgreSQL + REST API)
- **Điểm nổi bật:** Tích hợp **AI phân tích cảm xúc** & **dự đoán xu hướng** món ăn

---

## 📌 Slide 4: Kiến trúc hệ thống

**Tiêu đề:** 🏗️ **KIẾN TRÚC HỆ THỐNG**

**Nội dung:**
- **Mô hình:** MVC (Model – View – Controller)
- **Kiến trúc tổng thể:** Thick Client – Thin Server (BaaS)

**Sơ đồ kiến trúc:**

```
┌─────────────────────────────────────┐
│         📱 Android App (Java)       │
│  ┌───────────┬──────────┬────────┐  │
│  │   View    │Controller│ Model  │  │
│  │(XML Layout)│(Activity)│(POJO) │  │
│  └───────────┴──────────┴────────┘  │
│              ↕ Retrofit 2           │
├─────────────────────────────────────┤
│   🌐 Supabase (Backend-as-a-Service)     │
│  ┌──────────┬──────────┬─────────┐  │
│  │PostgREST │ Storage  │Edge Func│  │
│  │(REST API)│ (Images) │(AI API) │  │
│  └──────────┴──────────┴─────────┘  │
│         ↕ PostgreSQL Database       │
└─────────────────────────────────────┘
```

- **Xác thực:** Custom Auth (SHA-256 hashing + SharedPreferences session)
- **Network:** OkHttp 4.12 + Interceptor tự động gắn API Key

---

## 📌 Slide 5: Công nghệ sử dụng

**Tiêu đề:** 🛠️ **TECHNOLOGY STACK**

| Công nghệ | Mục đích |
| --- | --- |
| **Java 11** | Ngôn ngữ lập trình chính |
| **Android XML** | Thiết kế giao diện UI |
| **Supabase (PostgreSQL)** | Backend & Database |
| **Retrofit 2.9.0 + OkHttp 4.12** | Gọi REST API |
| **Gson 2.10.1** | Serialize/Deserialize JSON |
| **Glide 4.16.0** | Tải & cache hình ảnh |
| **Material Design 1.13** | UI Components chuẩn Google |
| **ViewPager2 + CardView** | Slider & Card UI |
| **JavaMail 1.6.7** | Gửi email reset mật khẩu |
| **Hugging Face BERT** | AI phân tích cảm xúc (qua Edge Function) |
| **Gemini AI** | Nhận xét doanh thu & hỗ trợ phân tích |
| **Sepay** | Thanh toán QR chuyển khoản ngân hàng |

---

## 📌 Slide 6: Cơ sở dữ liệu

**Tiêu đề:** 🗄️ **THIẾT KẾ CƠ SỞ DỮ LIỆU**

**Nội dung:** Hệ thống gồm **13+ bảng** trên Supabase PostgreSQL:

| Bảng | Mô tả |
| --- | --- |
| `users` | Thông tin người dùng (email, password SHA-256, role) |
| `categories` | Danh mục món ăn |
| `foods` | Thông tin món ăn (tên, giá, mô tả, đánh giá) |
| `food_images` | Thư viện ảnh cho từng món |
| `carts` / `cart_items` | Giỏ hàng người dùng |
| `orders` / `order_items` | Đơn đặt hàng & chi tiết |
| `addresses` | Địa chỉ giao hàng |
| `reviews` | Đánh giá & nhận xét (+sentiment) |
| `feedbacks` | Phản hồi của khách |
| `notifications` | Thông báo đẩy |
| `search_history` | Lịch sử tìm kiếm |
| `food_trends` | Dự đoán xu hướng AI |
| `chat_messages` | Tin nhắn chat User ↔ Admin |

**Đặc biệt:**
- Sử dụng **Views** (food_sentiment_stats, overall_sentiment_stats) cho báo cáo AI
- Hỗ trợ **RLS (Row Level Security)** và **Triggers** tự động cập nhật timestamp

---

## 📌 Slide 7: Luồng hoạt động tổng thể

**Tiêu đề:** 🔄 **LUỒNG HOẠT ĐỘNG CHÍNH**

**Sơ đồ:**

```
Đăng ký/Đăng nhập
       ↓
   Trang chủ (Slideshow, Gợi ý, Danh mục)
       ↓
  Tìm kiếm / Duyệt menu
       ↓
  Xem chi tiết món ăn → Đánh giá
       ↓
  Thêm vào giỏ hàng
       ↓
  Checkout (Chọn địa chỉ, Voucher, Thanh toán)
       ↓
  Xác nhận đơn hàng → Thông báo
       ↓
  Theo dõi lịch sử đơn hàng
```

---

## 📌 Slide 8: Đăng ký & Đăng nhập

**Tiêu đề:** 🔐 **QUẢN LÝ TÀI KHOẢN**

**Các tính năng:**

| Tính năng | Chi tiết |
| --- | --- |
| **Đăng ký** | Họ tên, Email, SĐT, Mật khẩu (SHA-256), Đồng ý điều khoản |
| **Đăng nhập** | Email + Mật khẩu, Ghi nhớ đăng nhập, Phân quyền Admin/User |
| **Quên mật khẩu** | Gửi mã xác nhận 6 số qua Gmail SMTP → Xác minh → Đổi mật khẩu mới |
| **Đổi mật khẩu** | Nhập mật khẩu cũ để xác thực → Đặt mật khẩu mới |
| **Hồ sơ cá nhân** | Xem/Sửa Tên, SĐT, Ảnh đại diện (Email không đổi được) |

**Kỹ thuật:**
- Mật khẩu mã hóa **SHA-256** trước khi lưu
- Session quản lý bằng **SharedPreferences** (SessionManager)
- Validation email format, độ dài mật khẩu phía client

> 💡 *Screenshot: Màn hình Login, Register, Forgot Password*

---

## 📌 Slide 9: Trang chủ

**Tiêu đề:** 🏠 **TRANG CHỦ — HOME SCREEN**

**Các thành phần:**
1. **Header hiện đại:**
   - Background wave gradient
   - Hiển thị "Giao tới: [địa chỉ mặc định]"
   - Nút Thông báo 🔔 & Yêu thích ❤️ (nền tròn blur)
2. **Thanh tìm kiếm:** Placeholder "Bạn đang thèm gì nào?"
3. **Slideshow banner:** Tự động chuyển đổi món phổ biến (ViewPager2)
4. **Danh mục cuộn ngang:** Tất cả / Món chính / Đồ uống / Tráng miệng / Khai vị
5. **Hot Offers:** Các món đang giảm giá đặc biệt
6. **Gợi ý cho bạn (Personalized):** Grid 2 cột hiển thị món ăn
7. **Danh sách tất cả món ăn**

> 💡 *Screenshot: Toàn bộ giao diện Home với header wave*

---

## 📌 Slide 10: Tìm kiếm món ăn

**Tiêu đề:** 🔍 **TÌM KIẾM MÓN ĂN**

**Tính năng:**
- **Tìm kiếm realtime:** Kết quả hiển thị ngay khi gõ (debounce search)
- **Lọc theo danh mục:** Chọn nhanh Tất cả / Món chính / Đồ uống / Tráng miệng / Khai vị
- **Lịch sử tìm kiếm:** Lưu lại các từ khóa đã tìm, hỗ trợ xóa từng mục hoặc xóa tất cả
- **Hiển thị kết quả:** Grid 2 cột với ảnh, tên, giá, đánh giá sao

> 💡 *Screenshot: Màn hình Search với lịch sử & kết quả*

---

## 📌 Slide 11: Chi tiết món ăn

**Tiêu đề:** 🍽️ **CHI TIẾT MÓN ĂN**

**Thông tin hiển thị:**
- 📸 Ảnh món ăn (lớn, chất lượng cao)
- 📝 Tên món & Mô tả chi tiết
- 💰 Giá gốc & Giá sau giảm (nếu có discount)
- ⭐ Đánh giá trung bình & Tổng số reviews
- ❤️ Nút Yêu thích (toggle)
- 🛒 Nút **"Thêm vào giỏ hàng"** nổi bật

**Liên kết:**
- Click vào phần đánh giá → Mở trang **FoodReviewsActivity** (xem tất cả reviews, viết đánh giá)

> 💡 *Screenshot: Màn hình Food Detail*

---

## 📌 Slide 12: Đánh giá & Nhận xét

**Tiêu đề:** ⭐ **HỆ THỐNG ĐÁNH GIÁ**

**Tính năng:**
- **Trang đánh giá riêng** (FoodReviewsActivity) – tách biệt khỏi chi tiết món
- **Rating summary:** Điểm trung bình, phân bố sao (1→5) bằng progress bar
- **Viết đánh giá:** Chọn sao (1–5), viết nhận xét, **đính kèm nhiều ảnh** cùng lúc
- **Xem đánh giá** người khác: Avatar, tên, sao, bình luận, ảnh, thời gian
- **Lọc theo sao:** "Lọc theo sao ↓" → Dialog chọn từ 1–5 sao

**Thiết kế theo phong cách Shopee/Hasaki:**
- Layout 2 cột: Bên trái (Average + Stars + Total), Bên phải (Star distribution bars)
- Nút "VIẾT ĐÁNH GIÁ" nổi bật

> 💡 *Screenshot: Trang Reviews với rating summary & danh sách*

---

## 📌 Slide 13: Giỏ hàng

**Tiêu đề:** 🛒 **GIỎ HÀNG**

**Tính năng:**

| Thao tác | Chi tiết |
| --- | --- |
| ➕ Thêm món | Chọn số lượng, hiệu ứng animation thêm thành công |
| 👀 Xem giỏ | Danh sách: Ảnh, Tên, Giá, Số lượng, Tổng tiền mỗi món |
| 🔢 Thay đổi SL | Tăng/Giảm bằng nút +/-, tổng tiền tự động cập nhật |
| 🗑️ Xóa 1 món | Vuốt trái (Swipe to Delete) + Snackbar "Hoàn tác" |
| 🗑️ Xóa toàn bộ | Xóa tất cả, Snackbar "Hoàn tác", tổng về 0 |
| 💰 Tổng tiền | Hiển thị ở bottom bar + Nút "Đặt hàng" |

> 💡 *Screenshot: Màn hình Cart*

---

## 📌 Slide 14: Checkout & Đặt hàng

**Tiêu đề:** 📋 **CHECKOUT & ĐẶT HÀNG**

**Luồng Checkout:**
1. **Chọn địa chỉ giao hàng:**
   - Dropdown Tỉnh/Thành phố → Quận/Huyện → Phường/Xã (dữ liệu từ file JSON toàn quốc)
   - Thêm/sửa/xóa địa chỉ, chọn địa chỉ mặc định
2. **Hỗ trợ Ăn tại quán (Dine-in):** Nhập tên bàn
3. **Nhập thông tin:** Tên người nhận, SĐT, Ghi chú
4. **Chọn Voucher:** Public voucher hoặc nhập mã Private voucher
5. **Phương thức thanh toán:**
   - 💵 Tiền mặt khi nhận hàng (COD)
   - 🏦 Chuyển khoản ngân hàng QR (Sepay)
6. **Xác nhận đơn:** Hiển thị mã đơn, thời gian dự kiến, nút xem đơn / về trang chủ

> 💡 *Screenshot: Màn hình Checkout*

---

## 📌 Slide 15: Thanh toán Sepay (QR Banking)

**Tiêu đề:** 💳 **THANH TOÁN QR — SEPAY**

**Cơ chế hoạt động:**
1. Khách chọn "Chuyển khoản ngân hàng"
2. Hệ thống sinh **mã QR** chứa thông tin chuyển khoản (Sepay API)
3. Khách quét QR bằng app ngân hàng → Chuyển tiền
4. Khách bấm **"Đã chuyển khoản"**
5. Server xác thực giao dịch qua **Webhook Sepay** (Supabase Edge Function)
6. Đơn hàng chuyển trạng thái thành **"Đã thanh toán"**

**Sơ đồ:**
```
App → Sepay API → QR Code
User quét QR → Chuyển khoản
Ngân hàng → Webhook → Supabase Edge Function → Xác nhận đơn
```

> 💡 *Screenshot: Popup QR thanh toán*

---

## 📌 Slide 16: Lịch sử đơn hàng

**Tiêu đề:** 📦 **LỊCH SỬ ĐƠN HÀNG**

**Tính năng:**
- **Phân loại tab:** Tất cả / Chờ xác nhận / Đang xử lý / Hoàn thành / Đã hủy
- **Phân màu trạng thái:**
  - 🟡 Chờ xác nhận (Pending)
  - 🔵 Đang xử lý (Preparing)
  - 🟢 Hoàn thành (Delivered)
  - 🔴 Đã hủy (Cancelled)
- **Xem chi tiết:** Mã đơn, trạng thái, danh sách món, thông tin giao hàng (expandable)
- **Hủy đơn:** Chỉ khi "Chờ xác nhận" → Dialog xác nhận
- **Đặt lại:** Thêm tất cả món vào giỏ → Chuyển sang Cart để sửa & đặt lại

> 💡 *Screenshot: Màn hình Order History*

---

## 📌 Slide 17: Yêu thích & Thông báo

**Tiêu đề:** ❤️ **YÊU THÍCH & 🔔 THÔNG BÁO**

### ❤️ Yêu thích:
- Click icon trái tim trên card món ăn hoặc trang chi tiết
- Xem danh sách tất cả món đã lưu
- Thêm nhanh vào giỏ hàng từ danh sách yêu thích

### 🔔 Thông báo:
- **Push Notification:** Thông báo trạng thái đơn hàng (xác nhận, đang xử lý, hoàn thành)
- **In-app Notification:** Xem lại tất cả thông báo, đánh dấu đã đọc/chưa đọc, xóa
- **Từ Admin:** Khuyến mãi mới, thông tin quán

> 💡 *Screenshot: Màn hình Favorites & Notifications*

---

## 📌 Slide 18: Chat trực tiếp

**Tiêu đề:** 💬 **CHAT TRỰC TIẾP USER ↔ ADMIN**

**Mô hình:**
- **User:** 1 khung chat duy nhất → nhắn tin với quán (admin nào trả lời cũng được)
- **Admin:** Danh sách nhiều khung chat tương ứng với từng User

**Tính năng:**
- Gửi/nhận tin nhắn realtime
- Hiển thị preview tin nhắn mới nhất, thời gian gửi
- Admin quản lý danh sách chat (ChatListActivity)
- Bubble chat phân biệt tin nhắn gửi/nhận

**Thay thế:** Chức năng "Góp ý" 1 chiều cũ → Chat 2 chiều tương tác

> 💡 *Screenshot: Màn hình Chat Room*

---

## 📌 Slide 19: Liên hệ & Thông tin quán

**Tiêu đề:** 📞 **LIÊN HỆ VỚI QUÁN**

**Các kênh liên hệ:**
- 📘 Facebook
- 💬 Zalo
- 📧 Email
- 📞 Gọi điện trực tiếp
- 🎥 YouTube
- 💻 Skype
- 🗺️ Xem vị trí trên **Google Maps** + Nút "Chỉ đường"

> 💡 *Screenshot: Màn hình Contact*

---

## 📌 Slide 20: Admin — Quản lý món ăn & Danh mục

**Tiêu đề:** 🔧 **ADMIN: QUẢN LÝ MÓN ĂN & DANH MỤC**

### Quản lý món ăn:
| Chức năng | Mô tả |
| --- | --- |
| Xem danh sách | Hình ảnh, Tên, Giá, Trạng thái Còn/Hết |
| Thêm mới | Upload ảnh, Tên, Giá, Danh mục, Mô tả, Tag (Phổ biến/Gợi ý) |
| Chỉnh sửa | Cập nhật mọi thông tin |
| Xóa | Dialog xác nhận trước khi xóa |
| Tìm kiếm | Tìm nhanh theo tên |

### Quản lý danh mục:
| Chức năng | Mô tả |
| --- | --- |
| CRUD | Thêm / Sửa / Xóa danh mục |
| Icon & Thứ tự | Đặt icon, sắp xếp thứ tự hiển thị |
| Bật/Tắt | Ẩn/hiện danh mục |

> 💡 *Screenshot: Màn hình Admin quản lý món ăn*

---

## 📌 Slide 21: Admin — Quản lý đơn hàng

**Tiêu đề:** 📦 **ADMIN: QUẢN LÝ ĐƠN HÀNG**

**Tính năng:**
- **Danh sách đơn hàng:** Mã đơn, Tên khách, Tổng tiền, Trạng thái
- **Lọc & Tìm kiếm:** Theo trạng thái, theo mã đơn
- **Xem chi tiết:** Thông tin khách, danh sách món chi tiết, ghi chú, tổng tiền
- **Cập nhật trạng thái:**
  ```
  Chờ xác nhận → Đang xử lý → Đang giao → Hoàn thành
                                         → Đã hủy
  ```
- **Thống kê đơn hàng:** Tổng đơn, đơn theo từng trạng thái, biểu đồ
- **Gửi thông báo đẩy:** Tự động khi thay đổi trạng thái đơn

> 💡 *Screenshot: Màn hình Admin Orders*

---

## 📌 Slide 22: Admin — Dashboard Doanh thu & AI

**Tiêu đề:** 📊 **ADMIN: DASHBOARD DOANH THU & AI INSIGHTS**

### 💰 Dashboard Doanh thu:
- Doanh thu theo **ngày / tháng / khoảng thời gian**
- **Biểu đồ bar chart** doanh thu theo tháng
- **Top 10 món bán chạy** (số lượng & doanh thu)
- **AI Gemini nhận xét:** Tự động phân tích doanh thu bằng Gemini 3 Flash → đưa ra nhận xét business giống con người

### 🤖 AI Sentiment Dashboard:
- **Tổng quan cảm xúc:** Phân bố Tích cực 😊 / Trung tính 😐 / Tiêu cực 😞
- **Top 5 món được yêu thích nhất** (positive sentiment cao)
- **Top 5 món cần cải thiện** (negative sentiment cao)
- **Nút phân tích lại** tất cả đánh giá

> 💡 *Screenshot: Admin Revenue Dashboard + AI Dashboard*

---

## 📌 Slide 23: AI — Phân tích cảm xúc & Dự đoán xu hướng

**Tiêu đề:** 🤖 **TÍNH NĂNG AI — SENTIMENT ANALYSIS & TREND PREDICTION**

### 📊 Phân tích cảm xúc (Sentiment Analysis):
- **Công nghệ:** Hugging Face BERT model qua Supabase Edge Functions
- **Hoạt động:** Phân tích tự động text review → Phân loại:
  - 😊 Tích cực (Positive) — score > 60%
  - 😐 Trung tính (Neutral) — score 40–60%
  - 😞 Tiêu cực (Negative) — score < 40%
- **Hỗ trợ:** Tiếng Việt & Tiếng Anh
- **Hiệu năng:** ~5ms/review, ~500ms cho 100 reviews

### 🔮 Dự đoán xu hướng (Trend Prediction):
- **Phương pháp:** Score-based Decision Tree
- **Các loại xu hướng:**
  - 🔥 **Hot Seller** — Bán chạy, sentiment tích cực cao (confidence: 85%)
  - 📉 **Declining** — Đang giảm doanh số (confidence: 70%)
  - ⚠️ **At Risk** — Nguy cơ bị bỏ rơi, sentiment tiêu cực cao (confidence: 75%)
  - ➡️ **Stable** — Ổn định (confidence: 65%)
- **Dữ liệu phân tích:** Lịch sử bán hàng 30 ngày + Cảm xúc khách hàng

> 💡 *Screenshot: Sentiment Statistics + Food Trend Prediction*

---

## 📌 Slide 24: Demo ứng dụng

**Tiêu đề:** 🎬 **DEMO ỨNG DỤNG**

**Kịch bản demo:**

**1. Luồng User:**
- Đăng nhập → Trang chủ → Tìm kiếm "Phở bò"
- Xem chi tiết → Đọc đánh giá → Thêm vào giỏ
- Mở giỏ hàng → Checkout → Chọn địa chỉ → Thanh toán COD
- Xem lịch sử đơn → Chat với Admin
- Viết đánh giá (5 sao + ảnh)

**2. Luồng Admin:**
- Đăng nhập Admin → Xem đơn hàng mới → Xác nhận đơn
- Xem Dashboard doanh thu → AI nhận xét
- Vào AI Dashboard → Phân tích cảm xúc → Dự đoán xu hướng
- Gửi thông báo cho User
- Trả lời chat User

> 🎥 *Chạy demo trực tiếp trên thiết bị / emulator*

---

## 📌 Slide 25: Tổng kết & Hướng phát triển

**Tiêu đề:** 🚀 **TỔNG KẾT & HƯỚNG PHÁT TRIỂN**

### ✅ Đã hoàn thành:
- Hệ thống đặt món ăn hoàn chỉnh từ User → Admin
- Tích hợp AI Sentiment Analysis & Trend Prediction
- Thanh toán QR Banking qua Sepay
- Chat realtime User ↔ Admin
- Dashboard doanh thu với AI Gemini nhận xét
- Quản lý địa chỉ cấu trúc Tỉnh/Huyện/Xã

### 🔮 Hướng phát triển:
| Hạng mục | Chi tiết |
| --- | --- |
| 🧠 AI nâng cao | Random Forest / TF Lite cho Prediction chính xác hơn |
| 📊 Charts | Tích hợp MPAndroidChart cho biểu đồ chuyên nghiệp |
| 💳 Thanh toán | Thêm Momo, ZaloPay |
| 📄 Xuất báo cáo | Export PDF/Excel cho Admin |
| 🔔 Realtime Alert | Thông báo tức thì khi có sentiment tiêu cực |
| 🗺️ Bản đồ | Tích hợp Google Maps tracking đơn hàng |
| 🌐 Đa ngôn ngữ | Hỗ trợ English/Vietnamese |

### 🙏 Cảm ơn thầy/cô và các bạn đã lắng nghe!

---

## 📎 PHỤ LỤC: Gợi ý thiết kế slide

### 🎨 Màu sắc đề xuất:
- **Primary:** `#FF6B35` (Orange — Food theme)
- **Secondary:** `#004E89` (Dark Blue — Professional)
- **Accent:** `#1A936F` (Green — Success/Positive)
- **Background:** `#FFF8F0` (Warm White)
- **Dark mode variant:** `#1E1E2E` (Deep Navy)

### 🖼️ Mỗi slide nên có:
- Screenshot thực tế của app (chụp từ emulator/thiết bị)
- Icon/Emoji phù hợp nội dung
- Không quá 6–7 dòng text mỗi slide
- Font chữ: **Montserrat** hoặc **Roboto** (clean, modern)

### ⏱️ Thời lượng dự kiến:
- Mỗi slide: ~1–2 phút → Tổng: **25–40 phút** (bao gồm demo)
- Slide demo: để nhiều thời gian nhất (~5–8 phút)
