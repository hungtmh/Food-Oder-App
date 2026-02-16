package com.example.food_order_app.network;

import com.example.food_order_app.config.SupabaseConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton quản lý Retrofit client cho Supabase
 */
public class RetrofitClient {
    private static Retrofit authRetrofit = null;
    private static Retrofit dbRetrofit = null;

    /**
     * Tạo OkHttpClient với interceptor thêm apikey header
     */
    private static OkHttpClient createClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder()
                                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                .header("Content-Type", "application/json");
                        // Add Authorization header if not already present
                        if (original.header("Authorization") == null) {
                            builder.header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
                        }
                        Request request = builder.method(original.method(), original.body()).build();
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Lấy Retrofit instance cho Auth API
     */
    public static synchronized Retrofit getAuthClient() {
        if (authRetrofit == null) {
            authRetrofit = new Retrofit.Builder()
                    .baseUrl(SupabaseConfig.AUTH_URL)
                    .client(createClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit;
    }

    /**
     * Lấy Retrofit instance cho Database REST API
     */
    public static synchronized Retrofit getDbClient() {
        if (dbRetrofit == null) {
            dbRetrofit = new Retrofit.Builder()
                    .baseUrl(SupabaseConfig.REST_URL)
                    .client(createClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return dbRetrofit;
    }

    /**
     * Lấy SupabaseAuthService
     */
    public static SupabaseAuthService getAuthService() {
        return getAuthClient().create(SupabaseAuthService.class);
    }

    /**
     * Lấy SupabaseDbService
     */
    public static SupabaseDbService getDbService() {
        return getDbClient().create(SupabaseDbService.class);
    }
}
