package com.example.food_order_app.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Utility class để gửi email qua Gmail SMTP
 *
 * HƯỚNG DẪN CÀI ĐẶT:
 * 1. Đăng nhập Gmail tại: https://myaccount.google.com
 * 2. Bật xác minh 2 bước: Security -> 2-Step Verification -> Bật
 * 3. Tạo App Password: Security -> 2-Step Verification -> App passwords
 *    - Chọn tên app: "Food Order App"
 *    - Google sẽ tạo mật khẩu 16 ký tự (ví dụ: "abcd efgh ijkl mnop")
 * 4. Copy mật khẩu đó vào SENDER_PASSWORD bên dưới (bỏ dấu cách)
 * 5. Đổi SENDER_EMAIL thành email Gmail của bạn
 */
public class EmailSender {

    private static final String TAG = "EmailSender";

    // ⚠️ THAY ĐỔI 2 GIÁ TRỊ NÀY ⚠️
    private static final String SENDER_EMAIL = "hungtmh20002@gmail.com";     // Gmail của bạn
    private static final String SENDER_PASSWORD = "xcbl pfka xoyg pzxh";     // App Password (16 ký tự)

    public interface EmailCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Gửi mã reset password về email
     */
    public static void sendResetCode(String recipientEmail, String code, EmailCallback callback) {
        new SendEmailTask(recipientEmail, code, callback).execute();
    }

    private static class SendEmailTask extends AsyncTask<Void, Void, String> {
        private final String recipientEmail;
        private final String code;
        private final EmailCallback callback;

        SendEmailTask(String recipientEmail, String code, EmailCallback callback) {
            this.recipientEmail = recipientEmail;
            this.code = code;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Cấu hình SMTP Gmail
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

                // Tạo session với authentication
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                // Tạo email
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL, "Food Order App"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Mã xác nhận đặt lại mật khẩu - Food Order App");

                // Nội dung email HTML
                String htmlContent = "<!DOCTYPE html>"
                        + "<html>"
                        + "<body style='font-family: Arial, sans-serif; padding: 20px;'>"
                        + "<div style='max-width: 500px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; padding: 30px;'>"
                        + "<h2 style='color: #6B4EAB; text-align: center;'>🍔 Food Order App</h2>"
                        + "<hr style='border: 1px solid #eee;'>"
                        + "<p>Xin chào,</p>"
                        + "<p>Bạn đã yêu cầu đặt lại mật khẩu. Đây là mã xác nhận của bạn:</p>"
                        + "<div style='background: #f5f0ff; border-radius: 10px; padding: 20px; text-align: center; margin: 20px 0;'>"
                        + "<h1 style='color: #6B4EAB; letter-spacing: 8px; font-size: 36px; margin: 0;'>" + code + "</h1>"
                        + "</div>"
                        + "<p style='color: #666;'>⏰ Mã này sẽ hết hạn sau <strong>10 phút</strong>.</p>"
                        + "<p style='color: #666;'>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                        + "<hr style='border: 1px solid #eee;'>"
                        + "<p style='color: #999; font-size: 12px; text-align: center;'>Food Order App © 2026</p>"
                        + "</div>"
                        + "</body>"
                        + "</html>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                // Gửi email
                Transport.send(message);

                Log.d(TAG, "Email gửi thành công đến: " + recipientEmail);
                return null; // null = thành công

            } catch (Exception e) {
                Log.e(TAG, "Lỗi gửi email: " + e.getMessage(), e);
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String error) {
            if (error == null) {
                callback.onSuccess();
            } else {
                callback.onError(error);
            }
        }
    }
}
