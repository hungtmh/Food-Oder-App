# Bộ Câu Hỏi Phỏng Vấn & Gợi Ý Trả Lời (Food Delivery App)

Dựa trên CV của bạn, dưới đây là danh sách các câu hỏi phỏng vấn mô phỏng (Mock Interview) được phân loại theo từng tính năng và vai trò, kèm theo gợi ý trả lời chi tiết. Việc bám sát các luồng logic thực tế của dự án sẽ giúp bạn ghi điểm tuyệt đối.

---

## 🏗️ 1. Quản Lý & Kiến Trúc Dự Án (Leadership & Architecture)

**Câu hỏi 1: Bạn đã lead một team 5 người trong dự án này. Hãy kể về cách bạn phân chia công việc, quản lý tiến độ và xử lý xung đột code (conflict)?**
*   **Trả lời:** 
    *   *Quản lý công việc:* Em áp dụng Agile/Scrum cơ bản, chia task theo tính năng (Frontend UI, Backend logic, AI, Payments). Mỗi tuần có báo cáo tiến độ rõ ràng (như báo cáo tuần 9).
    *   *Quản lý source code:* Team dùng Git. Mỗi tính năng mới được làm trên một branch riêng (ví dụ: `Thang`, `Khoa`, `NVK`).
    *   *Xử lý conflict:* Khi có chức năng lớn merge vào `main`, em là người trực tiếp review code và resolve conflicts (như lần xử lý xung đột AndroidManifest.xml và các file config), đảm bảo không ghi đè tính năng của nhau, luôn pull code mới nhất về trước khi push.

**Câu hỏi 2: Tại sao team lại chọn hệ sinh thái Supabase kết hợp với Java/XML (Native Android) thay vì dùng Firebase hay các framework Cross-platform như Flutter/React Native?**
*   **Trả lời:** 
    *   Về Native (Java/XML): Team hướng tới việc tối ưu hiệu suất trên thiết bị di động, nắm vững vòng đời (Lifecycle) của Android và tận dụng tối đa phần cứng (Camera up ảnh review, Location).
    *   Về Supabase: Nó cung cấp cơ sở dữ liệu quan hệ mạnh mẽ (PostgreSQL) rất phù hợp cho ứng dụng E-commerce (ràng buộc quan hệ User - Order - Product - Category - Review) so với NoSQL của Firebase. Hơn nữa Supabase hỗ trợ Realtime, Storage, và Auth rất đầy đủ với chi phí tối ưu.

---

## 🛒 2. Quản Lý Đơn Hàng & State Machine (Order Management)

**Câu hỏi 3: Trong CV em ghi có làm luồng "Dine-in" và "Delivery". Business logic của hai luồng này trong code và database khác nhau như thế nào?**
*   **Trả lời:** 
    *   *Với Delivery (Giao hàng):* Flow trạng thái sẽ đi qua 5 bước: `Chờ xác nhận -> Chờ chế biến -> Đang giao -> Đã phục vụ -> Hoàn thành/Đã hủy`. UI sẽ bắt buộc người dùng chọn địa chỉ tĩnh.
    *   *Với Dine-in (Ăn tại quán):* Em đã can thiệp vào logic State Machine để bỏ qua bước "Đang giao". Đơn hàng sẽ đi từ `Chờ chế biến -> Đã phục vụ`. Thay vì địa chỉ, khách chỉ cần nhập "Số bàn", và hệ thống tự động thiết lập giả lập địa chỉ bằng số bàn đó để bypass validation.

**Câu hỏi 4: Ứng dụng của bạn cập nhật realtime tiến độ đơn hàng như thế nào? Điều gì xảy ra nếu mạng của user bị chập chờn?**
*   **Trả lời:** 
    *   *Triển khai:* Em sử dụng tính năng **Supabase Realtime** (lắng nghe các thay đổi trên bảng `Orders`). Khi có bảng ghi nào thay đổi cột `status`, callback ở client-side lập tức được trigger để update dòng UI thông qua `runOnUiThread()`.
    *   *Xử lý rớt mạng:* Khi rớt mạng, Supabase SDK client sẽ cố gắng reconnect. Em xử lý thêm logic lấy lại (fetch-logic) trạng thái mới nhất từ database vào lúc `onResume()` của Activity/Fragment để tránh việc mất hook realtime khi kết nối lại.

---

## 📍 3. Checkout Flow & Dynamic Address (Luồng thanh toán)

**Câu hỏi 5: Em đã implement chức năng chọn "Tỉnh/Thành, Quận/Huyện, Phường/Xã" sử dụng Spinners như thế nào? Dữ liệu này được load từ đâu?**
*   **Trả lời:** 
    *   Em băm tách (decouple) việc lưu địa chỉ tĩnh trong Profile ra để có thể linh hoạt chọn lúc checkout. 
    *   Giao diện dùng 3 Spinners (Tỉnh - Huyện - Xã) dạng cascading (Spinner 2 phụ thuộc vào ID của Spinner 1). Sự kiện `onItemSelectedListener` của Tỉnh sẽ gọi API hoặc load data JSON cục bộ để render danh sách Huyện tương ứng.
    *   Để tránh lag UI, quá trình parse JSON được em thực hiện ở Background Thread (ví dụ: dùng ExecutorService hoặc RxJava/Coroutines nếu có dùng chung với Kotlin), sau đó đẩy dữ liệu lên qua UI Thread.

**Câu hỏi 6: Em đã tích hợp thanh toán SEPay như thế nào vào luồng Checkout?**
*   **Trả lời:** 
    *   Khi user chọn thanh toán chuyển khoản, app sẽ render một mã QR động chứa số tài khoản, số tiền và nội dung chuyển khoản được generate ngẫu nhiên khớp với `Order_ID`.
    *   Bên phía Supabase, em sử dụng Edge Functions (hoặc Webhooks) để lắng nghe callback từ SEPay gửi về. Khi webhook nhận thông tin "đã thanh toán đủ", nó cập nhật bảng `Orders` (chuyển trạng thái thanh toán là `PAID`), thông qua Supabase Realtime, ứng dụng tự động nhảy sang màn hình Success mà user không cần làm gì thêm.

---

## 💬 4. Real-time Chat Module (Tính năng Chat)

**Câu hỏi 7: Module Chat Real-time giữa khách và admin được xây dựng thế nào? Làm sao đảm bảo hiệu năng khi danh sách tin nhắn quá dài?**
*   **Trả lời:** 
    *   Mỗi đơn hàng hoặc mỗi User sẽ có một `room_id` riêng trong database. Em theo dõi table `chat_messages` bằng Supabase Realtime.
    *   Về cấu trúc UI: Em dùng `RecyclerView` trong Android. Khi có tin nhắn mới, em không `notifyDataSetChanged()` toàn bộ list mà chỉ gọi `adapter.notifyItemInserted()` ở phần tử cuối cùng và scroll mượt xuống đáy.
    *   Để tối ưu load, em áp dụng cơ chế Pagination (phân trang), chỉ load 20-30 tin nhắn cũ nhất, khi User kéo lên trên đỉnh RecyclerView (dùng `addOnScrollListener`) thì mới fetch tiếp dữ liệu lịch sử.

---

## ⭐ 5. Review & Rating (Đánh giá)

**Câu hỏi 8: Em xử lý việc người dùng upload "Nhiều hình ảnh (multi-image uploads)" trong phần Review như thế nào?**
*   **Trả lời:** 
    *   *Phía Client:* Em dùng API mới `ActivityResultContracts.GetMultipleContents()` của Android để mở thư viện, cho phép user pick nhiều ảnh cùng lúc, hiển thị thumbnail trên một RecyclerView nằm ngang. Trước khi up, em resize/compress bitmap để giảm dung lượng file.
    *   *Tiến trình Upload:* Vì upload nhiều file có thể gián đoạn, em duyệt mảng URI ảnh và đẩy bất đồng bộ (Async) lên Supabase Storage bucket. Em gom các `URL` kết quả lại thành một mảng (JSON array hoặc String list).
    *   *Transaction lưu Review:* Sau khi upload xong lấy được list URLs, em mới tiến hành `INSERT` bản ghi vào bảng `Reviews`. 

**Câu hỏi 9: Chức năng "Tự động tính toán số sao trung bình (dynamically calculating average rating)" được em thực hiện ở Frontend hay Backend? Tại sao?**
*   **Trả lời:** 
    *   Em thực hiện ở phía **Backend / Database** là tối ưu nhất. Thay vì bắt client loop qua tất cả review để tính trung bình cộng (gây nặng máy, sai lệch nếu data lớn), em dùng **Database Triggers** (hoặc RPC/View trong PostgreSQL). 
    *   Quy trình: Mỗi khi có thao tác `INSERT` hoặc `UPDATE` vào bảng `Reviews`, một Trigger sẽ tự động đếm tổng số review và chia trung bình số sao cho sản phẩm đó, rồi cập nhật ngược lại cột `average_rating` nằm trong bảng `Products`. Client chỉ việc fetch cột này về để hiển thị một cách nhẹ bén.

---

## 🤖 6. AI Integration (Câu hỏi thưởng / Mở rộng)

**Câu hỏi 10: Trong CV em không nhắc nhiều đến AI nhưng có ghi "with AI integration". Thực tế em đã dùng AI cho phần nào của app?**
*   **Trả lời:** 
    *   *(Dựa vào thực tế project)* Em đã ứng dụng AI (Gemini / chuyển sang Qwen & Hugging Face) để phân tích "Sentiment" từ những bài feedback review của khách hàng.
    *   Bên cạnh bình luận text bình thường, AI sẽ dựa trên log nội dung để rút trích các báo cáo kinh doanh, dự đoán các sản phẩm hot hoặc khách hàng có nguy cơ rời bỏ (at-risk) bằng các luật (rule-based) kết hợp với dữ liệu truy xuất đơn hàng thật để tạo thành chức năng AI Insights cho cấp quản lý (Admin).
