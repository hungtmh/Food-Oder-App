package com.example.food_order_app.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Retrofit interface cho Supabase Storage API
 */
public interface SupabaseStorageService {

    @PUT("object/{bucket}/{filePath}")
    Call<ResponseBody> uploadFile(
            @Path("bucket") String bucket,
            @Path(value = "filePath", encoded = true) String filePath,
            @Header("Content-Type") String contentType,
            @Header("x-upsert") String upsert,
            @Body RequestBody fileBody
    );
}
