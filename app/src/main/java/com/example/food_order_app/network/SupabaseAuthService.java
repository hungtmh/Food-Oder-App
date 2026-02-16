package com.example.food_order_app.network;

import com.example.food_order_app.model.AuthRequest;
import com.example.food_order_app.model.AuthResponse;
import com.example.food_order_app.model.ChangePasswordRequest;
import com.example.food_order_app.model.ForgotPasswordRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Service API cho Supabase Auth
 */
public interface SupabaseAuthService {

    /**
     * Đăng ký tài khoản mới
     */
    @POST("signup")
    Call<AuthResponse> signUp(@Body AuthRequest request);

    /**
     * Đăng nhập
     */
    @POST("token?grant_type=password")
    Call<AuthResponse> signIn(@Body AuthRequest request);

    /**
     * Quên mật khẩu - gửi email reset
     */
    @POST("recover")
    Call<Void> forgotPassword(@Body ForgotPasswordRequest request);

    /**
     * Đổi mật khẩu (cần access token)
     */
    @PUT("user")
    Call<AuthResponse> changePassword(
            @Header("Authorization") String bearerToken,
            @Body ChangePasswordRequest request
    );

    /**
     * Đăng xuất
     */
    @POST("logout")
    Call<Void> signOut(@Header("Authorization") String bearerToken);
}
