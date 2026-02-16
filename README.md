# 🍔 Food Order App

Ứng dụng đặt món ăn trực tuyến trên nền tảng Android, được xây dựng bằng **Java/XML** với backend **Supabase** (PostgreSQL + REST API).

## 📱 Thông tin ứng dụng

| Thông tin | Chi tiết |
|---|---|
| Ngôn ngữ | Java 11 |
| Nền tảng | Android (minSdk 29 / Android 10) |
| Target SDK | 36 |
| Kiến trúc | MVC (Model - View - Controller) |
| Backend | Supabase (REST API + PostgreSQL) |
| Xác thực | Custom Auth (SHA-256, SharedPreferences) |

---

## ✨ Tính năng đã hoàn thành

### 👤 Nhóm 1: Quản lý tài khoản
- ✅ Đăng ký tài khoản (email, mật khẩu, họ tên, SĐT)
- ✅ Đăng nhập với tùy chọn "Ghi nhớ đăng nhập"
- ✅ Phân quyền: **User** và **Admin** (tự động điều hướng theo role)
- ✅ Quên mật khẩu (gửi mã xác nhận qua Gmail SMTP)
- ✅ Xác nhận mã reset & đổi mật khẩu
- ✅ Xem & chỉnh sửa hồ sơ cá nhân
- ✅ Đổi mật khẩu trong app
- ✅ Đăng xuất

### 🍕 Nhóm 6: Món ăn phổ biến
- ✅ Slider tự động hiển thị món phổ biến (auto-scroll 3 giây)
- ✅ Chỉ báo dot indicator cho slider

### ⭐ Nhóm 7: Gợi ý món ăn
- ✅ Hiển thị danh sách món gợi ý dạng grid 2 cột
- ✅ Lọc theo danh mục (Tất cả, Món chính, Đồ uống, Tráng miệng, Khai vị)

### 🔍 Nhóm 8: Tìm kiếm, lọc & sắp xếp
- ✅ Tìm kiếm món ăn theo tên
- ✅ Lọc theo danh mục
- ✅ Lưu lịch sử tìm kiếm
- ✅ Xóa lịch sử tìm kiếm

### 📋 Nhóm 9: Chi tiết món ăn
- ✅ Hiển thị thông tin chi tiết (hình, tên, giá, giảm giá, mô tả)
- ✅ Xem đánh giá & bình luận của người dùng khác
- ✅ Gửi đánh giá (1-5 sao + bình luận)
- ✅ Thêm vào giỏ hàng

### 🛒 Nhóm 10: Giỏ hàng
- ✅ Xem danh sách sản phẩm trong giỏ
- ✅ Tăng / giảm số lượng
- ✅ Xóa sản phẩm khỏi giỏ
- ✅ Hiển thị tổng tiền, giảm giá, thành tiền
- ✅ Chuyển sang thanh toán

### 📦 Nhóm 11: Đặt hàng & thanh toán
- ✅ Nhập thông tin giao hàng (tên, SĐT, địa chỉ)
- ✅ Chọn phương thức thanh toán (COD / Chuyển khoản)
- ✅ Ghi chú đơn hàng
- ✅ Tạo mã đơn hàng tự động
- ✅ Xác nhận đơn hàng thành công

### 🔧 Nhóm 2: Quản lý món ăn (Admin)
- ✅ Xem danh sách tất cả món ăn
- ✅ Tìm kiếm món ăn
- ✅ Thêm món ăn mới (tên, giá, giảm giá, danh mục, mô tả, hình ảnh URL)
- ✅ Chỉnh sửa thông tin món ăn
- ✅ Đánh dấu phổ biến / gợi ý / còn hàng
- ✅ Xóa món ăn (có xác nhận)

### 💬 Nhóm 3: Quản lý phản hồi (Admin)
- ✅ Xem danh sách phản hồi khách hàng
- ✅ Lọc theo trạng thái (Tất cả / Mới / Đã đọc)
- ✅ Xem chi tiết phản hồi (đánh giá, nội dung, thông tin người gửi)
- ✅ Tự động đánh dấu đã đọc khi xem
- ✅ Xóa phản hồi

### 📊 Nhóm 4: Quản lý đơn hàng (Admin)
- ✅ Xem danh sách tất cả đơn hàng
- ✅ Lọc theo trạng thái (Chờ xác nhận / Đang xử lý / Hoàn thành / Đã hủy)
- ✅ Tìm kiếm theo mã đơn / tên khách hàng
- ✅ Xem chi tiết đơn hàng (sản phẩm, số lượng, giá, thông tin giao hàng)
- ✅ Cập nhật trạng thái đơn hàng (pending → confirmed → preparing → delivering → delivered / cancelled)

### 💰 Nhóm 5: Thống kê doanh thu (Admin)
- ✅ Chọn khoảng thời gian (DatePicker)
- ✅ Bộ lọc nhanh: Hôm nay / Tháng này / Tháng trước
- ✅ Tổng doanh thu & số đơn hoàn thành
- ✅ Thống kê đơn hàng theo trạng thái
- ✅ Top 10 món bán chạy nhất (xếp hạng theo doanh thu)

---

## 🗂️ Cấu trúc dự án

```
app/src/main/java/com/example/food_order_app/
├── MainActivity.java                  # Splash screen / Router
│
├── adapter/                           # Adapters cho RecyclerView
│   ├── AdminFeedbackAdapter.java      # Danh sách phản hồi (Admin)
│   ├── AdminFoodAdapter.java          # Danh sách món ăn (Admin)
│   ├── AdminOrderAdapter.java         # Danh sách đơn hàng (Admin)
│   ├── CartAdapter.java               # Giỏ hàng
│   ├── CategoryAdapter.java           # Danh mục
│   ├── FoodAdapter.java               # Lưới món ăn
│   ├── OrderItemAdapter.java          # Chi tiết đơn hàng
│   ├── ReviewAdapter.java             # Đánh giá
│   ├── SliderAdapter.java             # Slider ảnh
│   └── TopFoodAdapter.java            # Top món bán chạy
│
├── config/
│   └── SupabaseConfig.java            # URL & API Key Supabase
│
├── controller/                        # Activities (Controllers)
│   ├── LoginActivity.java             # Đăng nhập
│   ├── RegisterActivity.java          # Đăng ký
│   ├── ForgotPasswordActivity.java    # Quên mật khẩu
│   ├── VerifyResetCodeActivity.java   # Xác nhận mã reset
│   ├── ChangePasswordActivity.java    # Đổi mật khẩu
│   ├── ProfileActivity.java           # Hồ sơ cá nhân
│   ├── HomeActivity.java              # Trang chủ (User)
│   ├── FoodDetailActivity.java        # Chi tiết món ăn
│   ├── SearchActivity.java            # Tìm kiếm
│   ├── CartActivity.java              # Giỏ hàng
│   ├── CheckoutActivity.java          # Thanh toán
│   ├── OrderConfirmationActivity.java # Xác nhận đơn hàng
│   ├── AdminHomeActivity.java         # Quản lý món ăn (Admin)
│   ├── AdminAddEditFoodActivity.java  # Thêm/sửa món (Admin)
│   ├── AdminFeedbackActivity.java     # Quản lý phản hồi (Admin)
│   ├── AdminFeedbackDetailActivity.java # Chi tiết phản hồi (Admin)
│   ├── AdminOrdersActivity.java       # Quản lý đơn hàng (Admin)
│   ├── AdminOrderDetailActivity.java  # Chi tiết đơn hàng (Admin)
│   └── AdminRevenueActivity.java      # Thống kê doanh thu (Admin)
│
├── model/                             # Data Models
│   ├── User.java
│   ├── Food.java
│   ├── Category.java
│   ├── Cart.java & CartItem.java
│   ├── Order.java & OrderItem.java
│   ├── Review.java
│   ├── Feedback.java
│   ├── Address.java
│   ├── SearchHistory.java
│   ├── PasswordResetCode.java
│   ├── AuthRequest.java & AuthResponse.java
│   ├── ForgotPasswordRequest.java
│   └── ChangePasswordRequest.java
│
├── network/                           # API Layer
│   ├── RetrofitClient.java            # Singleton Retrofit + OkHttp
│   ├── SupabaseDbService.java         # REST API endpoints (CRUD)
│   └── SupabaseAuthService.java       # Auth endpoints
│
└── utils/                             # Utilities
    ├── SessionManager.java            # SharedPreferences session
    ├── PasswordUtils.java             # SHA-256 hashing
    ├── ValidationUtils.java           # Input validation
    └── EmailSender.java               # Gmail SMTP sender
```

---

## 🗄️ Cơ sở dữ liệu (Supabase PostgreSQL)

| # | Bảng | Mô tả |
|---|---|---|
| 1 | `users` | Người dùng (email, password SHA-256, role) |
| 2 | `password_reset_codes` | Mã xác nhận đặt lại mật khẩu |
| 3 | `categories` | Danh mục món ăn |
| 4 | `foods` | Món ăn (giá, giảm giá, phổ biến, gợi ý, còn hàng) |
| 5 | `food_images` | Hình ảnh bổ sung cho món ăn |
| 6 | `reviews` | Đánh giá & bình luận |
| 7 | `carts` | Giỏ hàng (1 user = 1 cart) |
| 8 | `cart_items` | Sản phẩm trong giỏ |
| 9 | `addresses` | Địa chỉ giao hàng |
| 10 | `orders` | Đơn hàng (trạng thái, thanh toán) |
| 11 | `order_items` | Chi tiết sản phẩm trong đơn |
| 12 | `search_history` | Lịch sử tìm kiếm |
| 13 | `feedbacks` | Phản hồi khách hàng |

> File SQL đầy đủ: [`supabase_schema.sql`](supabase_schema.sql) — chạy 1 lần trong Supabase SQL Editor.

---

## 🛠️ Công nghệ sử dụng

| Công nghệ | Phiên bản | Mục đích |
|---|---|---|
| **Retrofit** | 2.9.0 | HTTP client gọi Supabase REST API |
| **OkHttp** | 4.12.0 | HTTP networking + interceptor |
| **Gson** | 2.10.1 | JSON serialization/deserialization |
| **Glide** | 4.16.0 | Tải & cache hình ảnh |
| **CircleImageView** | 3.1.0 | Avatar hình tròn |
| **ViewPager2** | 1.1.0 | Slider hình ảnh |
| **Material Design** | 1.13.0 | BottomNavigationView, FAB, CardView |
| **JavaMail** | 1.6.7 | Gửi email reset mật khẩu qua Gmail SMTP |

---

## 🚀 Hướng dẫn cài đặt

### 1. Clone dự án
```bash
git clone https://github.com/hungtmh/Food-Oder-App.git
```

### 2. Mở bằng Android Studio
- Mở Android Studio → **Open** → chọn thư mục dự án
- Đợi Gradle sync hoàn tất

### 3. Thiết lập Supabase
1. Tạo project trên [supabase.com](https://supabase.com)
2. Vào **SQL Editor** → chạy file [`supabase_schema.sql`](supabase_schema.sql)
3. Cập nhật `SUPABASE_URL` và `SUPABASE_ANON_KEY` trong file `SupabaseConfig.java`

### 4. Tạo tài khoản Admin
Chạy SQL sau trong Supabase SQL Editor (thay email và password hash tương ứng):
```sql
INSERT INTO public.users (email, password, full_name, role)
VALUES ('admin@example.com', '<SHA-256 hash>', 'Admin', 'admin');
```

### 5. Build & chạy
```bash
./gradlew assembleDebug
```
Hoặc nhấn **Run** trong Android Studio.

---

## 📸 Luồng hoạt động

```
┌─────────────┐
│  Splash      │
│  (MainActivity)│
└──────┬──────┘
       │
  ┌────▼────┐     Chưa đăng nhập
  │  Login   │◄──────────────────┐
  └────┬────┘                    │
       │                         │
  ┌────▼────────────────┐       │
  │  Kiểm tra role       │       │
  └────┬───────┬────────┘       │
       │       │                 │
   User│       │Admin            │
       │       │                 │
  ┌────▼──┐ ┌──▼──────────┐    │
  │ Home  │ │ AdminHome    │    │
  │ Activity│ │ (Food Mgmt) │    │
  └───┬───┘ └──┬──────────┘    │
      │        │                │
      │   ┌────▼─────────┐     │
      │   │ Bottom Nav    │     │
      │   │ ┌─ Đồ ăn     │     │
      │   │ ├─ Phản hồi  │     │
      │   │ ├─ Đơn hàng  │     │
      │   │ └─ Tài khoản ─┼─── │
      │   └──────────────┘     │
      │                         │
 ┌────▼─────────┐              │
 │ Bottom Nav    │              │
 │ ┌─ Home      │              │
 │ ├─ Giỏ hàng  │              │
 │ ├─ Phản hồi  │              │
 │ ├─ Liên hệ   │              │
 │ └─ Tài khoản ─┼──────────────┘
 └──────────────┘
```

---

## 👨‍💻 Tác giả

- **GitHub:** [hungtmh](https://github.com/hungtmh)
