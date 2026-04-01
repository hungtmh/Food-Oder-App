import re

path = r'd:\SLIDE_MOBLIDE\slide_mobile.html'
with open(path, 'r', encoding='utf-8') as f:
    html = f.read()

idx_27 = html.find('<!-- Slide 27:')
idx_29 = html.find('<!-- Slide 29:')
idx_30 = html.find('<!-- Slide 30:')
end_idx = html.find('        </div>\n    </div>\n\n    <!-- Reveal.js Scripts -->')

if -1 in [idx_27, idx_29, idx_30, end_idx]:
    print("Error finding markers")
    exit(1)

ai_slide = html[idx_29:idx_30]
ai_slide = ai_slide.replace('<!-- Slide 29:', '<!-- Slide 27:')

new_slides = ai_slide + """            <!-- Slide 28: Tiến độ công việc - Thành viên 1 -->
            <section>
                <h2>TIẾN ĐỘ: THÀNH VIÊN 1 (Hùng - hungtmh)</h2>
                <div style="font-size: 0.55em; width: 95%; margin: 0 auto;">
                    <table style="width: 100%; border-collapse: collapse; line-height: 1.4; text-align: left;">
                        <tr><th style="width: 5%; border-bottom: 2px solid white; padding-bottom: 5px;">STT</th><th style="width: 20%; border-bottom: 2px solid white; padding-bottom: 5px;">Tính năng</th><th style="border-bottom: 2px solid white; padding-bottom: 5px;">Mô tả chi tiết công việc</th><th style="width: 15%; border-bottom: 2px solid white; padding-bottom: 5px;">Tiến độ</th></tr>
                        <tr><td style="padding: 8px 0; border-bottom: 1px solid #555;">1</td><td style="padding: 8px 0; border-bottom: 1px solid #555;"><strong>Core & Auth</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #555;">Khởi tạo dự án, thiết lập Supabase liên kết db, fix lỗi đổi mật khẩu.</td><td style="padding: 8px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 8px 0; border-bottom: 1px solid #555;">2</td><td style="padding: 8px 0; border-bottom: 1px solid #555;"><strong>Quản lý đơn hàng</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #555;">Thiết lập logic luồng đặt hàng hoàn chỉnh từ User tới Admin (Chờ duyệt, Chờ chế biến, Đang giao, Đã phục vụ).</td><td style="padding: 8px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 8px 0; border-bottom: 1px solid #555;">3</td><td style="padding: 8px 0; border-bottom: 1px solid #555;"><strong>UI/UX Món Ăn</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #555;">Giao diện đánh giá chi tiết (Star rating), tải tính năng thêm ảnh review, yêu thích sản phẩm.</td><td style="padding: 8px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 8px 0; border-bottom: 1px solid #555;">4</td><td style="padding: 8px 0; border-bottom: 1px solid #555;"><strong>Thanh toán & Địa chỉ</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #555;">Tách quản lý địa chỉ khỏi Profile, áp dụng Tỉnh/Huyện/Xã bằng Spinner trực tiếp lúc Checkout. Thêm chế độ Ăn Tại Quán.</td><td style="padding: 8px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 8px 0;">5</td><td style="padding: 8px 0;"><strong>Giao tiếp Realtime</strong></td><td style="padding: 8px 0;">Thiết lập chat trực tiếp User &harr; Admin để phản hồi, giục món. Fix giao diện navbar ghim cố định đáy màn hình.</td><td style="padding: 8px 0; color: #4CAF50;">Khớp 100%</td></tr>
                    </table>
                </div>
            </section>

            <!-- Slide 29: Tiến độ công việc - Thành viên 2 -->
            <section>
                <h2>TIẾN ĐỘ: THÀNH VIÊN 2 (Thắng - thangak18)</h2>
                <div style="font-size: 0.55em; width: 95%; margin: 0 auto;">
                    <table style="width: 100%; border-collapse: collapse; line-height: 1.4; text-align: left;">
                        <tr><th style="width: 5%; border-bottom: 2px solid white; padding-bottom: 5px;">STT</th><th style="width: 20%; border-bottom: 2px solid white; padding-bottom: 5px;">Tính năng</th><th style="border-bottom: 2px solid white; padding-bottom: 5px;">Mô tả chi tiết công việc</th><th style="width: 15%; border-bottom: 2px solid white; padding-bottom: 5px;">Tiến độ</th></tr>
                        <tr><td style="padding: 8px 0; border-bottom: 1px solid #555;">1</td><td style="padding: 8px 0; border-bottom: 1px solid #555;"><strong>Admin Thống Kê</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #555;">Thiết kế Dashboard Báo Cáo. Tích hợp thư viện MPAndroidChart vẽ biểu đồ Doanh Thu & Đơn Hàng (Bar/PieChart).</td><td style="padding: 8px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 8px 0; border-bottom: 1px solid #555;">2</td><td style="padding: 8px 0; border-bottom: 1px solid #555;"><strong>Phân tích AI Gemini</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #555;">Nâng cấp AI model lên gemini-3-flash-preview chuyên phân tích doanh số và Monthly Trend thông minh.</td><td style="padding: 8px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 8px 0; border-bottom: 1px solid #555;">3</td><td style="padding: 8px 0; border-bottom: 1px solid #555;"><strong>Admin Category</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #555;">Thiết lập Quản lý danh mục chuyên sâu (Lọc theo Doanh Thu, Tìm kiếm, Kéo thả đổi vị trí).</td><td style="padding: 8px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 8px 0; border-bottom: 1px solid #555;">4</td><td style="padding: 8px 0; border-bottom: 1px solid #555;"><strong>Xuất báo cáo</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #555;">Tích hợp tính năng tự động tạo bản PDF & Excel báo cáo gửi thẳng vào Cổng Email chia sẻ từ App.</td><td style="padding: 8px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 8px 0;">5</td><td style="padding: 8px 0;"><strong>System Utils</strong></td><td style="padding: 8px 0;">Viết custom layout cấu trúc chia 8-TAB cho Custom Bottom Nav Admin, quản lý ẩn Config an toàn.</td><td style="padding: 8px 0; color: #4CAF50;">Khớp 100%</td></tr>
                    </table>
                </div>
            </section>

            <!-- Slide 30: Tiến độ công việc - Thành viên 3 -->
            <section>
                <h2>TIẾN ĐỘ: THÀNH VIÊN 3 (Khải - nvkhai238)</h2>
                <div style="font-size: 0.55em; width: 95%; margin: 0 auto;">
                    <table style="width: 100%; border-collapse: collapse; line-height: 1.4; text-align: left;">
                        <tr><th style="width: 5%; border-bottom: 2px solid white; padding-bottom: 5px;">STT</th><th style="width: 20%; border-bottom: 2px solid white; padding-bottom: 5px;">Tính năng</th><th style="border-bottom: 2px solid white; padding-bottom: 5px;">Mô tả chi tiết công việc</th><th style="width: 15%; border-bottom: 2px solid white; padding-bottom: 5px;">Tiến độ</th></tr>
                        <tr><td style="padding: 12px 0; border-bottom: 1px solid #555;">1</td><td style="padding: 12px 0; border-bottom: 1px solid #555;"><strong>Sentiment AI</strong></td><td style="padding: 12px 0; border-bottom: 1px solid #555;">Tích hợp công nghệ dự đoán trí tuệ nhân tạo (Hugging Face) xử lý Data Cảm Xúc & Phân tích qua API Call.</td><td style="padding: 12px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 12px 0; border-bottom: 1px solid #555;">2</td><td style="padding: 12px 0; border-bottom: 1px solid #555;"><strong>Admin Notification</strong></td><td style="padding: 12px 0; border-bottom: 1px solid #555;">Thực thi luồng Push Broadcast Notification hàng loạt từ Admin đến tất cả User (quảng bá khuyến mãi).</td><td style="padding: 12px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 12px 0;">3</td><td style="padding: 12px 0;"><strong>System Config</strong></td><td style="padding: 12px 0;">Dọn dẹp môi trường Environment (.env), rà soát bảo mật lỗi cấp quyền. Resolve Git Conflicts.</td><td style="padding: 12px 0; color: #4CAF50;">Khớp 100%</td></tr>
                    </table>
                </div>
            </section>

            <!-- Slide 31: Tiến độ công việc - Thành viên 4 -->
            <section>
                <h2>TIẾN ĐỘ: THÀNH VIÊN 4 (Khoa - Dicun)</h2>
                <div style="font-size: 0.55em; width: 95%; margin: 0 auto;">
                    <table style="width: 100%; border-collapse: collapse; line-height: 1.4; text-align: left;">
                        <tr><th style="width: 5%; border-bottom: 2px solid white; padding-bottom: 5px;">STT</th><th style="width: 20%; border-bottom: 2px solid white; padding-bottom: 5px;">Tính năng</th><th style="border-bottom: 2px solid white; padding-bottom: 5px;">Mô tả chi tiết công việc</th><th style="width: 15%; border-bottom: 2px solid white; padding-bottom: 5px;">Tiến độ</th></tr>
                        <tr><td style="padding: 12px 0; border-bottom: 1px solid #555;">1</td><td style="padding: 12px 0; border-bottom: 1px solid #555;"><strong>UI Khuyến mãi</strong></td><td style="padding: 12px 0; border-bottom: 1px solid #555;">Thêm tính năng Giao diện Hot Offers, cấu hình hệ thống Adaptor xử lý dạng thanh trượt Slider cho các Banner Quảng Cáo.</td><td style="padding: 12px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 12px 0; border-bottom: 1px solid #555;">2</td><td style="padding: 12px 0; border-bottom: 1px solid #555;"><strong>Gợi ý món ăn</strong></td><td style="padding: 12px 0; border-bottom: 1px solid #555;">Thiết lập Personalized Recommendations, hiển thị đề xuất món ăn được cá nhân hóa tự động theo sở thích trên trang chủ.</td><td style="padding: 12px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 12px 0;">3</td><td style="padding: 12px 0;"><strong>Tối ưu HMI/UX</strong></td><td style="padding: 12px 0;">Đồng bộ hóa lại layout chính xác chuẩn User Experience trên trang Client Main View.</td><td style="padding: 12px 0; color: #4CAF50;">Khớp 100%</td></tr>
                    </table>
                </div>
            </section>

            <!-- Slide 32: Tiến độ công việc - Thành viên 5 -->
            <section>
                <h2>TIẾN ĐỘ: THÀNH VIÊN 5 (Vũ/Quân - wokovn)</h2>
                <div style="font-size: 0.55em; width: 95%; margin: 0 auto;">
                    <table style="width: 100%; border-collapse: collapse; line-height: 1.4; text-align: left;">
                        <tr><th style="width: 5%; border-bottom: 2px solid white; padding-bottom: 5px;">STT</th><th style="width: 20%; border-bottom: 2px solid white; padding-bottom: 5px;">Tính năng</th><th style="border-bottom: 2px solid white; padding-bottom: 5px;">Mô tả chi tiết công việc</th><th style="width: 15%; border-bottom: 2px solid white; padding-bottom: 5px;">Tiến độ</th></tr>
                        <tr><td style="padding: 12px 0; border-bottom: 1px solid #555;">1</td><td style="padding: 12px 0; border-bottom: 1px solid #555;"><strong>Thanh Toán SEPay QR</strong></td><td style="padding: 12px 0; border-bottom: 1px solid #555;">Code hệ thống Webhook liên kết trực tiếp ngân hàng API SEPay quét mã QR nhận diện nội dung đóng tiền chính xác.</td><td style="padding: 12px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 12px 0; border-bottom: 1px solid #555;">2</td><td style="padding: 12px 0; border-bottom: 1px solid #555;"><strong>Hỗ trợ Voucher</strong></td><td style="padding: 12px 0; border-bottom: 1px solid #555;">Thiết kế luồng Áp dụng Voucher mua sắm tích hợp trực tiếp lúc đặt hàng Checkout Giỏ Hàng.</td><td style="padding: 12px 0; border-bottom: 1px solid #555; color: #4CAF50;">Khớp 100%</td></tr>
                        <tr><td style="padding: 12px 0;">3</td><td style="padding: 12px 0;"><strong>Quản lý Reorder</strong></td><td style="padding: 12px 0;">Setup hoàn chỉnh Address Management liên kết Giỏ hàng Undo và Tái Đặt hàng (Reorder) 1 click từ lịch sử.</td><td style="padding: 12px 0; color: #4CAF50;">Khớp 100%</td></tr>
                    </table>
                </div>
            </section>

            <!-- Slide 33: Demo ứng dụng -->
            <section>
                <h2>DEMO ỨNG DỤNG THỰC TẾ</h2>
                <div style="font-size: 0.6em; text-align: left; width: 85%; margin: 0 auto; line-height: 1.6;">
                    <p><strong>1. Quy trình của User (Khách hàng):</strong></p>
                    <p style="background: rgba(255,255,255,0.05); padding: 10px; border-radius: 5px;">
                        Đăng nhập &rarr; Trang chủ &rarr; Tìm kiếm "Phở bò" &rarr; Xem chi tiết &rarr; Đọc đánh giá &rarr; Thêm vào giỏ &rarr; Mở giỏ hàng &rarr; Checkout &rarr; Chọn địa chỉ &rarr; Thanh toán COD/QR SEPay &rarr; Xem lịch sử đơn &rarr; Chat với Admin &rarr; Viết đánh giá (5 sao + ảnh).
                    </p>
                    
                    <p style="margin-top: 15px;"><strong>2. Quy trình Admin (Quản trị):</strong></p>
                    <p style="background: rgba(255,255,255,0.05); padding: 10px; border-radius: 5px;">
                        Đăng nhập Admin &rarr; Xem đơn hàng mới &rarr; Xác nhận đơn (Đang nấu &rarr; Đang giao) &rarr; Xem Dashboard doanh thu &rarr; AI nhận xét &rarr; Vào AI Dashboard (Phân tích cảm xúc &rarr; Dự đoán xu hướng) &rarr; Xuất báo cáo PDF &rarr; Gửi email &rarr; Gửi thông báo khuyến mãi cho tất cả khách &rarr; Trả lời chat.
                    </p>
                    
                    <div style="text-align: center; margin-top: 25px; padding: 15px; border: 1px dashed var(--accent); border-radius: 10px; color: var(--accent);">
                        <em>[Chạy demo trực tiếp trên thiết bị / emulator]</em>
                    </div>
                </div>
            </section>

            <!-- Slide 34: Tổng kết & Hướng phát triển -->
            <section>
                <h2>TỔNG KẾT & HƯỚNG MỞ</h2>
                <div style="display: flex; gap: 20px; font-size: 0.55em; text-align: left; width: 95%; margin: 0 auto;">
                    <div style="flex: 1;">
                        <p style="color: var(--primary); font-size: 1.1em; border-bottom: 1px solid #555; padding-bottom: 5px;"><strong>Đã hoàn thành</strong></p>
                        <ul style="line-height: 1.4; padding-left: 20px;">
                            <li>Hệ thống đặt món ăn hoàn chỉnh từ User &rarr; Admin.</li>
                            <li>Tích hợp AI Sentiment Analysis & Trend Prediction.</li>
                            <li>Thanh toán QR Banking qua Sepay & Hỗ trợ Voucher.</li>
                            <li>Chat realtime User &harr; Admin, Push Notification.</li>
                            <li>Dashboard doanh thu với Gemini AI, Chart Phân tích.</li>
                            <li>Xác thực địa chỉ cấu trúc Tỉnh/Huyện/Xã chuẩn.</li>
                            <li>Xuất báo cáo PDF/CSV & gửi Email tự động.</li>
                            <li>Quản lý danh mục nâng cao Drag-Drop.</li>
                        </ul>
                    </div>
                    <div style="flex: 1;">
                        <p style="color: var(--accent); font-size: 1.1em; border-bottom: 1px solid #555; padding-bottom: 5px;"><strong>Hướng phát triển</strong></p>
                        <table style="width: 100%; border-collapse: collapse; line-height: 1.3;">
                            <tr><th style="width: 30%; border-bottom: 1px dotted #555; padding: 5px 0;">Hạng mục</th><th style="border-bottom: 1px dotted #555; padding: 5px 0;">Chi tiết</th></tr>
                            <tr><td style="padding: 5px 0; border-bottom: 1px solid #444;"><strong>AI nâng cao</strong></td><td style="padding: 5px 0; border-bottom: 1px solid #444;">Random Forest / TF Lite cho Prediction chính xác hơn.</td></tr>
                            <tr><td style="padding: 5px 0; border-bottom: 1px solid #444;"><strong>Thanh toán</strong></td><td style="padding: 5px 0; border-bottom: 1px solid #444;">Thêm Momo, ZaloPay, OnePay.</td></tr>
                            <tr><td style="padding: 5px 0; border-bottom: 1px solid #444;"><strong>Realtime Alert</strong></td><td style="padding: 5px 0; border-bottom: 1px solid #444;">Cảnh báo tức thì lên hệ thống quán khi có review tiêu cực.</td></tr>
                            <tr><td style="padding: 5px 0; border-bottom: 1px solid #444;"><strong>Bản đồ</strong></td><td style="padding: 5px 0; border-bottom: 1px solid #444;">Tích hợp Google Maps API tracking live đơn hàng.</td></tr>
                            <tr><td style="padding: 5px 0;"><strong>Đa ngôn ngữ</strong></td><td style="padding: 5px 0;">Hỗ trợ English/Vietnamese Native.</td></tr>
                        </table>
                    </div>
                </div>
                <div style="font-size: 0.7em; text-align: center; margin-top: 25px; color: var(--primary); font-weight: bold; text-transform: uppercase; letter-spacing: 2px;">
                    Cám ơn Thầy/Cô và các bạn đã chú ý lắng nghe!
                </div>
            </section>
"""

final_html = html[:idx_27] + new_slides + html[end_idx:]

with open(path, 'w', encoding='utf-8') as f:
    f.write(final_html)

print("Update complete")