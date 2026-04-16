# 📋 NỘI DUNG CÔNG VIỆC — ỨNG DỤNG ĐẶT MÓN ĂN

> **Tổng hợp toàn bộ công việc nhóm đã thực hiện** — Dựa trên lịch sử commit Git từ ngày **16/02/2026** đến **16/04/2026**

---

## 👥 Thành viên nhóm

| STT | Thành viên | GitHub Username | Vai trò   | Phụ trách chính | Số commit |
| --- | ---------- | --------------- | --------- | --------------- | --------- |
| 1   | **Hùng**   | hungtmh         | Developer | Phần User       | 37        |
| 2   | **Cường**  | Dicun           | Developer | Phần User       | 7         |
| 3   | **Khoa**   | wokovn          | Developer | Phần User       | 3         |
| 4   | **Thắng**  | thangak18       | Developer | Phần Admin      | 26        |
| 5   | **Khải**   | nvkhai238       | Developer | Phần Admin      | 25        |

---

## 📱 PHẦN USER

---

### 👤 Hùng — Tài khoản, Hồ sơ, Tìm kiếm & Nhiều tính năng bổ sung

#### 1. Quản lý tài khoản

| Tính năng              | Mô tả                                                          | Ngày commit   | Trạng thái    |
| ---------------------- | -------------------------------------------------------------- | ------------- | ------------- |
| Đăng nhập              | Email + mật khẩu, ghi nhớ đăng nhập, phân quyền Admin/User     | 16/02/2026    | ✅ Hoàn thành |
| Đăng ký                | Họ tên, Email, SĐT, Mật khẩu, đồng ý điều khoản                | 16/02/2026    | ✅ Hoàn thành |
| Quên mật khẩu          | Gửi mã xác nhận qua email (Gmail SMTP)                         | 16/02/2026    | ✅ Hoàn thành |
| Đổi mật khẩu           | Nhập mật khẩu cũ để xác thực, đặt mật khẩu mới                 | 16/02/2026    | ✅ Hoàn thành |
| Fix lỗi đổi mật khẩu   | Sửa lỗi & xóa các categories trùng lặp                        | 07/03/2026    | ✅ Hoàn thành |
| Hồ sơ cá nhân          | Xem/sửa Tên, SĐT, Địa chỉ, Ảnh đại diện. Email không đổi được | 16/02/2026    | ✅ Hoàn thành |
| Cập nhật avatar hệ thống | Giao diện avatar mới cho toàn hệ thống                        | 21/03/2026    | ✅ Hoàn thành |
| Đăng xuất              | Thoát tài khoản, xóa session                                   | 16/02/2026    | ✅ Hoàn thành |

#### 2. Tìm kiếm món ăn

| Tính năng             | Mô tả                                            | Ngày commit | Trạng thái    |
| --------------------- | ------------------------------------------------- | ----------- | ------------- |
| Tìm kiếm theo tên     | Ô tìm kiếm, kết quả hiển thị ngay khi gõ         | 16/02/2026  | ✅ Hoàn thành |
| Lọc theo danh mục     | Tất cả, Món chính, Đồ uống, Tráng miệng, Khai vị | 16/02/2026  | ✅ Hoàn thành |
| Lưu lịch sử tìm kiếm | Lưu & xóa lịch sử                                | 16/02/2026  | ✅ Hoàn thành |
| Sắp xếp món ăn        | Giá thấp→cao, cao→thấp, đánh giá cao nhất         | 08/03/2026  | ✅ Hoàn thành |

#### 3. Yêu thích sản phẩm ⭐ *(Tính năng bổ sung)*

| Tính năng                | Mô tả                                      | Ngày commit | Trạng thái    |
| ------------------------ | ------------------------------------------ | ----------- | ------------- |
| Lưu món yêu thích        | Click icon trái tim để lưu/bỏ yêu thích    | 08/03/2026  | ✅ Hoàn thành |
| Xem danh sách yêu thích  | Xem tất cả món đã lưu, thêm nhanh vào giỏ  | 08/03/2026  | ✅ Hoàn thành |

#### 4. Liên hệ với quán ⭐ *(Tính năng bổ sung)*

| Tính năng                | Mô tả                                            | Ngày commit | Trạng thái    |
| ------------------------ | ------------------------------------------------ | ----------- | ------------- |
| Liên hệ với quán         | Facebook, Skype, Gọi điện, YouTube, Zalo, Email   | 08/03/2026  | ✅ Hoàn thành |
| Chỉnh sửa giao diện liên hệ | Gắn cố định navbar ở dưới, UI trang liên hệ mới | 01/04/2026  | ✅ Hoàn thành |

#### 5. Đánh giá & Nhận xét sản phẩm ⭐ *(Tính năng bổ sung)*

| Tính năng                    | Mô tả                                                             | Ngày commit | Trạng thái    |
| ---------------------------- | ----------------------------------------------------------------- | ----------- | ------------- |
| Đánh giá chi tiết sản phẩm   | UI đánh giá đẹp, hỗ trợ gửi ảnh kèm đánh giá                     | 22/03/2026  | ✅ Hoàn thành |
| Fix đánh giá & thêm nhiều ảnh | Fix trung bình số sao, tách trang viết đánh giá riêng             | 24/03/2026  | ✅ Hoàn thành |

#### 6. Chat với Admin ⭐ *(Tính năng bổ sung)*

| Tính năng              | Mô tả                                                               | Ngày commit | Trạng thái    |
| ---------------------- | ------------------------------------------------------------------- | ----------- | ------------- |
| Chat với admin quán     | Đổi phản hồi thành chat, chat thúc giục món ăn, gọi món nhanh lên   | 25/03/2026  | ✅ Hoàn thành |

#### 7. Quản lý địa chỉ & Đặt hàng tại quán ⭐ *(Tính năng bổ sung)*

| Tính năng                        | Mô tả                                                                               | Ngày commit | Trạng thái    |
| -------------------------------- | ----------------------------------------------------------------------------------- | ----------- | ------------- |
| Tách quản lý địa chỉ vào checkout | Tích hợp địa chỉ vào bước thanh toán, Spinner Tỉnh/Thành/Phường/Quận                | 25/03/2026  | ✅ Hoàn thành |
| Đặt ăn tại quán                  | Chọn chế độ ăn tại quán (điền số bàn), tự động set địa chỉ mặc định                 | 21/03/2026  | ✅ Hoàn thành |
| Thêm trạng thái "Đang giao"      | Trạng thái giao hàng mới cho đơn delivery                                           | 01/04/2026  | ✅ Hoàn thành |

#### 8. Quản lý đơn hàng (User + Admin) ⭐ *(Tính năng bổ sung)*

| Tính năng                  | Mô tả                                         | Ngày commit | Trạng thái    |
| -------------------------- | --------------------------------------------- | ----------- | ------------- |
| Logic quản lý đơn hàng     | Thêm logic đơn hàng cho cả admin và user       | 07/03/2026  | ✅ Hoàn thành |

#### 9. Bảo mật & Cấu hình ⭐ *(Tính năng bổ sung)*

| Tính năng                | Mô tả                                                | Ngày commit | Trạng thái    |
| ------------------------ | ---------------------------------------------------- | ----------- | ------------- |
| Giấu API key             | Đổi SupabaseConfig + giấu các API tối mật             | 30/03/2026  | ✅ Hoàn thành |
| Fix SupabaseConfig        | Sửa lỗi cấu hình Supabase                            | 01/04/2026  | ✅ Hoàn thành |

#### 10. UI/UX & Fix lỗi ⭐ *(Tính năng bổ sung)*

| Tính năng                   | Mô tả                                                     | Ngày commit | Trạng thái    |
| --------------------------- | --------------------------------------------------------- | ----------- | ------------- |
| Network error UI             | Giao diện lỗi mạng với nút retry, cải thiện error logging  | 06/03/2026  | ✅ Hoàn thành |
| Sửa navbar admin             | Fix giao diện bottom navigation admin                      | 31/03/2026  | ✅ Hoàn thành |
| Fix bottom nav admin         | Fix lỗi bottom nav tự chuyển acc admin sang user           | 03/04/2026  | ✅ Hoàn thành |
| Gắn cố định navbar           | Cố định navbar ở dưới cho tất cả các trang                 | 01/04/2026  | ✅ Hoàn thành |

#### 11. Tài liệu & Slides

| Tính năng                | Mô tả                                    | Ngày commit | Trạng thái    |
| ------------------------ | ---------------------------------------- | ----------- | ------------- |
| README.md                | Tài liệu dự án đầy đủ                    | 16/02/2026  | ✅ Hoàn thành |
| Phân công công việc       | File phân công nhiệm vụ cho nhóm         | 22/02/2026  | ✅ Hoàn thành |
| Báo cáo tuần 3           | Cập nhật README cho báo cáo tuần 3       | 21/03/2026  | ✅ Hoàn thành |
| SLIDES thuyết trình       | Slides seminar thuyết trình dự án         | 01/04/2026  | ✅ Hoàn thành |

---

### 🍕 Cường — Trang chủ, Slider, Gợi ý & Thông báo đẩy

#### 1. Hiển thị món phổ biến (Slideshow)

| Tính năng              | Mô tả                                                  | Ngày commit | Trạng thái    |
| ---------------------- | ------------------------------------------------------ | ----------- | ------------- |
| Slideshow món phổ biến | Slide tự động chuyển, hình ảnh đẹp, click xem chi tiết | 16/02/2026  | ✅ Hoàn thành |

#### 2. Gợi ý món ăn

| Tính năng           | Mô tả                                             | Ngày commit | Trạng thái    |
| ------------------- | ------------------------------------------------- | ----------- | ------------- |
| Danh sách món gợi ý | Hiển thị grid 2 cột ở trang chủ, cập nhật tự động | 16/02/2026  | ✅ Hoàn thành |

#### 3. Hot Offers & Banner ⭐ *(Tính năng bổ sung)*

| Tính năng           | Mô tả                                                    | Ngày commit | Trạng thái    |
| ------------------- | -------------------------------------------------------- | ----------- | ------------- |
| Hot Offers UI        | Giao diện hiển thị ưu đãi hot, chuyển slider sang banners | 20/03/2026  | ✅ Hoàn thành |

#### 4. Personalized Recommendations ⭐ *(Tính năng bổ sung)*

| Tính năng              | Mô tả                                           | Ngày commit | Trạng thái    |
| ---------------------- | ----------------------------------------------- | ----------- | ------------- |
| Gợi ý cá nhân hóa      | Gợi ý món ăn theo sở thích, cập nhật UI trang chủ | 20/03/2026  | ✅ Hoàn thành |

#### 5. Push Notification khi thay đổi trạng thái ⭐ *(Tính năng bổ sung)*

| Tính năng                    | Mô tả                                                    | Ngày commit | Trạng thái    |
| ---------------------------- | -------------------------------------------------------- | ----------- | ------------- |
| Thông báo đẩy khi đổi status | Tạo push notification khi admin thay đổi trạng thái đơn  | 09/04/2026  | ✅ Hoàn thành |

---

### 🛒 Khoa — Giỏ hàng, Đặt hàng, Thanh toán & Tích hợp QR

#### 1. Giỏ hàng

| Tính năng            | Mô tả                                               | Ngày commit | Trạng thái    |
| -------------------- | --------------------------------------------------- | ----------- | ------------- |
| Thêm món vào giỏ     | Chọn số lượng, hiệu ứng thêm thành công             | 16/02/2026  | ✅ Hoàn thành |
| Xem giỏ hàng         | Danh sách món: Ảnh, Tên, Giá, Số lượng, Tổng tiền   | 16/02/2026  | ✅ Hoàn thành |
| Thay đổi số lượng    | Tăng/giảm, tổng tiền tự động cập nhật               | 16/02/2026  | ✅ Hoàn thành |
| Xóa món khỏi giỏ     | Xóa từng món, vuốt trái để xóa, Snackbar Hoàn tác   | 16/02/2026  | ✅ Hoàn thành |
| Xóa toàn bộ giỏ hàng | Xóa tất cả, Snackbar Hoàn tác, tổng tiền reset về 0 | 16/02/2026  | ✅ Hoàn thành |

#### 2. Đặt hàng

| Tính năng             | Mô tả                                                        | Ngày commit | Trạng thái    |
| --------------------- | ------------------------------------------------------------ | ----------- | ------------- |
| Tạo đơn hàng          | Nhập tên, SĐT, địa chỉ, chọn thanh toán, ghi chú             | 16/02/2026  | ✅ Hoàn thành |
| Xác nhận đặt hàng     | Hiển thị mã đơn, thời gian dự kiến, nút xem đơn/về trang chủ | 16/02/2026  | ✅ Hoàn thành |
| Lưu địa chỉ giao hàng | Lưu nhiều địa chỉ, đặt mặc định, chọn nhanh khi checkout     | 16/02/2026  | ✅ Hoàn thành |

#### 3. Lịch sử đơn hàng

| Tính năng                | Mô tả                                                            | Ngày commit | Trạng thái    |
| ------------------------ | ---------------------------------------------------------------- | ----------- | ------------- |
| Xem danh sách đơn đã đặt | Phân màu theo trạng thái, lọc theo tab (Tất cả/Chờ/Chế biến/Hủy) | 16/02/2026  | ✅ Hoàn thành |
| Xem chi tiết đơn hàng    | Mã đơn, trạng thái, danh sách món, thông tin giao hàng (expand)  | 16/02/2026  | ✅ Hoàn thành |
| Hủy đơn hàng             | Hủy khi "Chờ xác nhận", dialog xác nhận                          | 16/02/2026  | ✅ Hoàn thành |
| Đặt lại đơn hàng         | Thêm tất cả món vào giỏ, chuyển sang CartActivity để sửa & đặt   | 07/03/2026  | ✅ Hoàn thành |

#### 4. Quản lý địa chỉ nâng cao ⭐ *(Tính năng bổ sung)*

| Tính năng                | Mô tả                                                  | Ngày commit | Trạng thái    |
| ------------------------ | ------------------------------------------------------ | ----------- | ------------- |
| Quản lý địa chỉ          | Thêm/sửa/xóa nhiều địa chỉ, hoàn tác xóa giỏ hàng     | 07/03/2026  | ✅ Hoàn thành |

#### 5. Thanh toán

| Tính năng          | Mô tả                         | Ngày commit | Trạng thái    |
| ------------------ | ----------------------------- | ----------- | ------------- |
| Thanh toán COD     | Trả tiền mặt khi nhận hàng    | 16/02/2026  | ✅ Hoàn thành |

#### 6. Thanh toán SEPay QR ⭐ *(Tính năng bổ sung)*

| Tính năng                   | Mô tả                                                  | Ngày commit | Trạng thái    |
| --------------------------- | ------------------------------------------------------ | ----------- | ------------- |
| Tích hợp SEPay QR Payment    | Hỗ trợ quét mã QR thanh toán qua SEPay                 | 26/03/2026  | ✅ Hoàn thành |
| Voucher & mã giảm giá (User) | Tích hợp áp dụng voucher khi thanh toán                 | 26/03/2026  | ✅ Hoàn thành |
| Hướng dẫn test webhook SEPay | Tài liệu hướng dẫn test webhook SEPay bằng tiếng Việt  | 26/03/2026  | ✅ Hoàn thành |

---

## 🔧 PHẦN ADMIN

---

### 📦 Thắng — Quản lý món ăn, Đơn hàng, Doanh thu & Thống kê

#### 1. Quản lý món ăn (Admin)

| Tính năng            | Mô tả                                           | Ngày commit | Trạng thái    |
| -------------------- | ----------------------------------------------- | ----------- | ------------- |
| Xem danh sách món ăn | Hình ảnh, Tên, Giá, Trạng thái Còn/Hết          | 16/02/2026  | ✅ Hoàn thành |
| Thêm món ăn mới      | Hình, Tên, Giá, Danh mục, Mô tả, Phổ biến/Gợi ý | 16/02/2026  | ✅ Hoàn thành |
| Chỉnh sửa món ăn     | Cập nhật thông tin món ăn đã có                 | 16/02/2026  | ✅ Hoàn thành |
| Xóa món ăn           | Xóa món, có xác nhận trước khi xóa              | 16/02/2026  | ✅ Hoàn thành |
| Tìm kiếm món ăn      | Tìm nhanh trong danh sách                       | 16/02/2026  | ✅ Hoàn thành |

#### 2. Quản lý danh mục (Admin) ⭐ *(Tính năng bổ sung)*

| Tính năng                     | Mô tả                                                  | Ngày commit | Trạng thái    |
| ----------------------------- | ------------------------------------------------------ | ----------- | ------------- |
| Quản lý danh mục cơ bản       | Thêm/sửa/xóa danh mục món ăn cho admin                  | 30/03/2026  | ✅ Hoàn thành |
| Nâng cấp quản lý danh mục     | Cải thiện UI & thêm màn hình chi tiết danh mục           | 10/04/2026  | ✅ Hoàn thành |

#### 3. Quản lý đơn hàng (Admin)

| Tính năng               | Mô tả                                                    | Ngày commit | Trạng thái    |
| ----------------------- | -------------------------------------------------------- | ----------- | ------------- |
| Xem tất cả đơn hàng     | Mã đơn, Tên khách, Tổng tiền, Trạng thái. Lọc & tìm kiếm | 16/02/2026  | ✅ Hoàn thành |
| Xem chi tiết đơn hàng   | Thông tin khách, danh sách món, ghi chú, tổng tiền       | 16/02/2026  | ✅ Hoàn thành |
| Cập nhật trạng thái     | Chờ xác nhận → Đang xử lý → Hoàn thành / Đã hủy          | 16/02/2026  | ✅ Hoàn thành |

#### 4. Thống kê đơn hàng ⭐ *(Tính năng bổ sung)*

| Tính năng                 | Mô tả                                                          | Ngày commit | Trạng thái    |
| ------------------------- | -------------------------------------------------------------- | ----------- | ------------- |
| Thống kê đơn hàng         | Tổng đơn, đơn theo trạng thái, biểu đồ                        | 06/03/2026  | ✅ Hoàn thành |
| UI thống kê admin          | Giao diện thống kê cập nhật                                    | 10/03/2026  | ✅ Hoàn thành |
| Order Statistics nâng cao  | Thống kê đơn hàng chi tiết, cải thiện UI, cập nhật thông báo   | 15/03/2026  | ✅ Hoàn thành |
| Tách thống kê & doanh thu  | Tách riêng module Statistics khỏi Revenue theo yêu cầu dự án   | 15/03/2026  | ✅ Hoàn thành |

#### 5. Theo dõi doanh thu (Admin)

| Tính năng                       | Mô tả                                              | Ngày commit | Trạng thái    |
| ------------------------------- | -------------------------------------------------- | ----------- | ------------- |
| Doanh thu theo ngày             | Chọn ngày, xem tổng tiền, số đơn, món bán chạy     | 16/02/2026  | ✅ Hoàn thành |
| Doanh thu theo khoảng thời gian | Chọn từ ngày - đến ngày                            | 16/02/2026  | ✅ Hoàn thành |
| Top 10 món bán chạy             | Số lượng đã bán và doanh thu                       | 16/02/2026  | ✅ Hoàn thành |

#### 6. Báo cáo & Thống kê nâng cao ⭐ *(Tính năng bổ sung)*

| Tính năng                       | Mô tả                                                            | Ngày commit | Trạng thái    |
| ------------------------------- | ---------------------------------------------------------------- | ----------- | ------------- |
| Module report-statistics         | Thêm module báo cáo và nâng cấp phân tích doanh thu              | 15/03/2026  | ✅ Hoàn thành |
| Report/Statistics screens        | Cập nhật màn hình báo cáo/thống kê và custom bottom nav           | 20/03/2026  | ✅ Hoàn thành |
| Monthly Trend Analysis           | Biểu đồ kép phân tích xu hướng tháng, tích hợp Gemini AI         | 27/03/2026  | ✅ Hoàn thành |

#### 7. Thông báo đơn hàng (Admin) ⭐ *(Tính năng bổ sung)*

| Tính năng             | Mô tả                                                  | Ngày commit | Trạng thái    |
| --------------------- | ------------------------------------------------------ | ----------- | ------------- |
| Thông báo đơn hàng     | Thông báo khi có đơn mới, cải thiện UI admin            | 06/03/2026  | ✅ Hoàn thành |

#### 8. Bảo mật & Cấu hình ⭐ *(Tính năng bổ sung)*

| Tính năng                    | Mô tả                                                     | Ngày commit | Trạng thái    |
| ---------------------------- | --------------------------------------------------------- | ----------- | ------------- |
| Giấu GeminiAiConfig.java     | Bảo mật file cấu hình AI                                  | 27/03/2026  | ✅ Hoàn thành |
| Exclude config directory      | Loại toàn bộ thư mục config khỏi Git tracking              | 27/03/2026  | ✅ Hoàn thành |
| Update AI model               | Cập nhật model lên gemini-3-flash-preview                  | 27/03/2026  | ✅ Hoàn thành |
| Fix build errors               | Restore constants, navigation IDs, missing imports          | 27/03/2026  | ✅ Hoàn thành |

---

### 💬 Khải — AI, Phản hồi, Voucher & Thông báo đẩy

#### 1. Tính năng AI — Phân tích cảm xúc & Dự đoán

| Tính năng                  | Mô tả                                                                | Ngày commit | Trạng thái    |
| -------------------------- | -------------------------------------------------------------------- | ----------- | ------------- |
| Phân tích cảm xúc đánh giá | AI phân tích review: Tích cực / Tiêu cực / Trung tính                | 05/03/2026  | ✅ Hoàn thành |
| Thống kê cảm xúc           | Tổng số, tỷ lệ %, biểu đồ trực quan cho từng món                     | 05/03/2026  | ✅ Hoàn thành |
| Dự đoán xu hướng món ăn    | Phân tích lịch sử → dự đoán Hot Seller / Declining / At Risk         | 05/03/2026  | ✅ Hoàn thành |
| Dashboard AI Insights      | Tổng quan cảm xúc, top yêu thích, top cần cải thiện, dự đoán 30 ngày | 05/03/2026  | ✅ Hoàn thành |

#### 2. Nâng cấp AI liên tục ⭐ *(Tính năng bổ sung)*

| Tính năng                         | Mô tả                                                              | Ngày commit | Trạng thái    |
| --------------------------------- | ------------------------------------------------------------------ | ----------- | ------------- |
| Sentiment & Prediction bằng API   | Chuyển sang gọi API cho phân tích cảm xúc và dự đoán               | 28/03/2026  | ✅ Hoàn thành |
| Thay Gemini bằng Hugging Face      | Đổi API KEY GEMINI sang Hugging Face                                | 02/04/2026  | ✅ Hoàn thành |
| Sửa UI Sentiment                   | Cập nhật giao diện phân tích cảm xúc                               | 02/04/2026  | ✅ Hoàn thành |
| Sửa filter + Insight nhận xét AI   | Thêm filter và insight nhận xét từ AI                              | 02/04/2026  | ✅ Hoàn thành |
| UI AI Insight + Logic Prediction    | Chỉnh sửa UI AI Insight, thêm file logic prediction                | 02/04/2026  | ✅ Hoàn thành |
| Chuyển Gemini → Qwen                | Đổi mô hình AI sang Qwen                                          | 03/04/2026  | ✅ Hoàn thành |
| Logic rule-based từ order thực      | Thay đổi logic dự đoán dựa trên dữ liệu đơn hàng thực             | 03/04/2026  | ✅ Hoàn thành |
| Nâng cấp Dashboard, Sentiment, Prediction | Dashboard mới, cải thiện Sentiment & Prediction, thêm Insight Detail | 06/04/2026  | ✅ Hoàn thành |

#### 3. Quản lý phản hồi (Admin)

| Tính năng              | Mô tả                                                     | Ngày commit | Trạng thái    |
| ---------------------- | --------------------------------------------------------- | ----------- | ------------- |
| Xem danh sách phản hồi | Tên khách, Nội dung, Sao, Thời gian. Phân loại Mới/Đã đọc | 05/03/2026  | ✅ Hoàn thành |
| Xem chi tiết phản hồi  | Đọc đầy đủ nội dung, tự động đánh dấu "Đã đọc"            | 05/03/2026  | ✅ Hoàn thành |
| Xóa phản hồi           | Xóa phản hồi không phù hợp                                | 05/03/2026  | ✅ Hoàn thành |

#### 4. Thông báo đẩy (Admin)

| Tính năng         | Mô tả                                                     | Ngày commit | Trạng thái    |
| ----------------- | --------------------------------------------------------- | ----------- | ------------- |
| Gửi thông báo đẩy | Gửi cho khách khi thay đổi trạng thái đơn, khuyến mãi mới | 21/03/2026  | ✅ Hoàn thành |

#### 5. Quản lý Voucher (Admin) ⭐ *(Tính năng bổ sung)*

| Tính năng                        | Mô tả                                                     | Ngày commit | Trạng thái    |
| -------------------------------- | --------------------------------------------------------- | ----------- | ------------- |
| Quản lý Voucher cho Admin         | Tạo/sửa/xóa voucher, phần trăm/số tiền giảm, thời hạn    | 06/04/2026  | ✅ Hoàn thành |
| Tích hợp Voucher vào Checkout     | Áp dụng voucher khi thanh toán, hiển thị tiền giảm         | 06/04/2026  | ✅ Hoàn thành |
| Xem số lượt sử dụng               | Thống kê lượt dùng mã                                     | 06/04/2026  | ✅ Hoàn thành |
| Vô hiệu hóa mã                   | Tắt mã không còn sử dụng                                  | 06/04/2026  | ✅ Hoàn thành |
| File migration/seed data           | Thêm file migration và seed data cho Voucher                | 06/04/2026  | ✅ Hoàn thành |

#### 6. UI Admin & Navigation ⭐ *(Tính năng bổ sung)*

| Tính năng                       | Mô tả                                                       | Ngày commit | Trạng thái    |
| ------------------------------- | ----------------------------------------------------------- | ----------- | ------------- |
| Navigation bar thống nhất        | Sửa navigation bar cho tất cả tab admin                     | 04/04/2026  | ✅ Hoàn thành |
| Admin Drawer menu                | Cập nhật giao diện menu Admin Drawer, cấu hình Manifest      | 06/04/2026  | ✅ Hoàn thành |

#### 7. Bảo mật ⭐ *(Tính năng bổ sung)*

| Tính năng       | Mô tả                                    | Ngày commit | Trạng thái    |
| --------------- | ---------------------------------------- | ----------- | ------------- |
| Xóa file env bị lộ | Xóa file chứa biến môi trường bị push lên | 28/03/2026  | ✅ Hoàn thành |

---

## 📊 TỔNG HỢP KẾT QUẢ

### Theo thành viên

| Thành viên | Tính năng gốc (kế hoạch) | Tính năng bổ sung | Tổng tính năng đã làm | Số commit | Giai đoạn hoạt động         |
| ---------- | ------------------------ | ----------------- | --------------------- | --------- | --------------------------- |
| **Hùng**   | 10                       | 15                | 25                    | 37        | 16/02/2026 – 03/04/2026     |
| **Cường**  | 3                        | 3                 | 6                     | 7         | 20/03/2026 – 09/04/2026     |
| **Khoa**   | 11                       | 3                 | 14                    | 3         | 07/03/2026 – 26/03/2026     |
| **Thắng**  | 9                        | 12                | 21                    | 26        | 06/03/2026 – 10/04/2026     |
| **Khải**   | 7                        | 15                | 22                    | 25        | 05/03/2026 – 06/04/2026     |
| **Tổng**   | **40**                   | **48**            | **88**                | **98**    | **16/02/2026 – 10/04/2026** |

### Theo nhóm tính năng

| Nhóm          | Tên                                    | Người phụ trách | Trạng thái             |
| ------------- | -------------------------------------- | --------------- | ---------------------- |
| 1             | Quản lý tài khoản                      | Hùng            | ✅ Hoàn thành          |
| 2             | Quản lý món ăn (Admin)                 | Thắng           | ✅ Hoàn thành          |
| 3             | Quản lý phản hồi (Admin)              | Khải            | ✅ Hoàn thành          |
| 4             | Quản lý đơn hàng (Admin)              | Thắng           | ✅ Hoàn thành          |
| 5             | Theo dõi doanh thu (Admin)            | Thắng           | ✅ Hoàn thành          |
| 6             | Hiển thị món phổ biến                  | Cường           | ✅ Hoàn thành          |
| 7             | Gợi ý món ăn                          | Cường           | ✅ Hoàn thành          |
| 8             | Tìm kiếm món ăn                       | Hùng            | ✅ Hoàn thành          |
| 9             | Chi tiết món ăn                        | Cường           | ✅ Hoàn thành          |
| 10            | Giỏ hàng                              | Khoa            | ✅ Hoàn thành          |
| 11            | Đặt hàng                              | Khoa            | ✅ Hoàn thành          |
| 12            | Lịch sử đơn hàng                      | Khoa            | ✅ Hoàn thành          |
| 13            | Phản hồi & Đánh giá (User)            | Hùng            | ✅ Hoàn thành          |
| 14            | Thông tin liên hệ                     | Hùng            | ✅ Hoàn thành          |
| 15            | Tính năng AI                          | Khải            | ✅ Hoàn thành          |
| 16            | Thông báo                             | Cường + Khải    | ✅ Hoàn thành          |
| 17            | Yêu thích                             | Hùng            | ✅ Hoàn thành          |
| 18            | Khuyến mãi & Mã giảm giá             | Khoa + Khải     | ✅ Hoàn thành          |
| 19            | Báo cáo & Thống kê (Admin)           | Thắng           | ✅ Hoàn thành          |
| 20            | Thanh toán                            | Khoa            | ✅ Hoàn thành (COD + SEPay QR) |
| **Bổ sung**   | Quản lý danh mục (Admin)              | Thắng           | ✅ Hoàn thành          |
| **Bổ sung**   | Chat User ↔ Admin                     | Hùng            | ✅ Hoàn thành          |
| **Bổ sung**   | Đặt ăn tại quán                       | Hùng            | ✅ Hoàn thành          |
| **Bổ sung**   | Trạng thái "Đang giao"               | Hùng            | ✅ Hoàn thành          |
| **Bổ sung**   | Hot Offers & Banner                   | Cường           | ✅ Hoàn thành          |
| **Bổ sung**   | Personalized Recommendations          | Cường           | ✅ Hoàn thành          |
| **Bổ sung**   | Thanh toán QR (SEPay)                 | Khoa            | ✅ Hoàn thành          |
| **Bổ sung**   | Quản lý Voucher (Admin + User)        | Khải            | ✅ Hoàn thành          |
| **Bổ sung**   | Nâng cấp AI (Hugging Face → Qwen)    | Khải            | ✅ Hoàn thành          |
| **Bổ sung**   | Monthly Trend Analysis + Gemini AI    | Thắng           | ✅ Hoàn thành          |
| **Bổ sung**   | Địa chỉ Tỉnh/Quận/Phường Spinner     | Hùng            | ✅ Hoàn thành          |
| **Bổ sung**   | Bảo mật API keys & Config             | Hùng + Thắng + Khải | ✅ Hoàn thành     |

---

## 🛠️ CÔNG NGHỆ SỬ DỤNG

| Công nghệ                        | Mục đích                    |
| -------------------------------- | --------------------------- |
| Java 11 + Android XML            | Ngôn ngữ chính & giao diện  |
| Supabase (PostgreSQL + REST API) | Backend & Database           |
| Retrofit 2.9.0 + OkHttp 4.12.0   | Gọi API                     |
| Gson 2.10.1                      | Xử lý JSON                  |
| Glide 4.16.0                     | Tải & cache hình ảnh        |
| Material Design 1.13.0           | UI Components                |
| JavaMail 1.6.7                   | Gửi email reset mật khẩu    |
| ViewPager2 + CardView            | Slider & Card UI             |
| Gemini AI / Qwen / Hugging Face  | Phân tích cảm xúc & dự đoán |
| SEPay QR Payment                 | Thanh toán quét mã QR        |
| Firebase Cloud Messaging (FCM)   | Push Notification            |

---

## 📝 GHI CHÚ

- **Kiến trúc**: MVC (Model - View - Controller)
- **Xác thực**: Custom Auth (SHA-256 hashing, SharedPreferences session)
- **Database schema**: File [`supabase_schema.sql`](supabase_schema.sql)
- **GitHub**: [https://github.com/hungtmh/Food-Oder-App](https://github.com/hungtmh/Food-Oder-App)
- **Tổng thời gian phát triển**: ~2 tháng (16/02/2026 – 10/04/2026)
- **Tổng số commit**: 98 commits
- Các tính năng đánh dấu ⭐ là **tính năng bổ sung** ngoài kế hoạch phân công ban đầu, do các thành viên chủ động thêm vào.
