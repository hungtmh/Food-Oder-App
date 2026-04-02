package com.example.food_order_app.network;

import com.example.food_order_app.model.*;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Supabase Database REST API service.
 * Authorization + apikey headers are added automatically by RetrofitClient interceptor.
 * For POST operations needing "Prefer: return=representation", pass it explicitly.
 */
public interface SupabaseDbService {

    // ==================== USERS ====================
    @GET("users")
    Call<List<User>> getUserByEmail(
            @Query("email") String emailFilter,
            @Query("select") String select);

    @GET("users")
    Call<List<User>> getUsersByRole(
            @Query("role") String roleFilter,
            @Query("select") String select,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @POST("users")
    Call<List<User>> createUser(@Body User user);

    @Headers("Prefer: return=representation")
    @PATCH("users")
    Call<List<User>> updateUser(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @Headers("Prefer: return=representation")
    @PATCH("users")
    Call<List<User>> updatePasswordByEmail(
            @Query("email") String emailFilter,
            @Body Map<String, Object> updates);

    // ==================== PASSWORD RESET ====================
    @Headers("Prefer: return=representation")
    @POST("password_reset_codes")
    Call<List<PasswordResetCode>> createResetCode(@Body PasswordResetCode resetCode);

    @GET("password_reset_codes")
    Call<List<PasswordResetCode>> getResetCode(
            @Query("email") String emailFilter,
            @Query("code") String codeFilter,
            @Query("used") String usedFilter,
            @Query("select") String select);

    @Headers("Prefer: return=representation")
    @PATCH("password_reset_codes")
    Call<List<PasswordResetCode>> updateResetCode(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    // ==================== CATEGORIES ====================
    @GET("categories")
    Call<List<Category>> getCategories(
            @Query("is_active") String isActive,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @POST("categories")
    Call<List<Category>> createCategory(@Body Category category);

    @Headers("Prefer: return=representation")
    @PATCH("categories")
    Call<List<Category>> updateCategory(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @DELETE("categories")
    Call<Void> deleteCategory(@Query("id") String idFilter);

    // ==================== FOODS ====================
    @GET("foods")
    Call<List<Food>> getPopularFoods(
            @Query("is_popular") String isPopular,
            @Query("is_available") String isAvailable);

    @GET("foods")
    Call<List<Food>> getRecommendedFoods(
            @Query("is_recommended") String isRec,
            @Query("is_available") String isAvailable);

    @GET("foods")
    Call<List<Food>> getAllFoods(
            @Query("is_available") String isAvailable,
            @Query("order") String order);

    @GET("foods")
    Call<List<Food>> getFoodsByCategory(
            @Query("category_id") String categoryFilter,
            @Query("is_available") String isAvailable);

    @GET("foods")
    Call<List<Food>> searchFoods(
            @Query("name") String nameFilter,
            @Query("is_available") String isAvailable);

    @GET("foods")
    Call<List<Food>> getFoodById(
            @Query("id") String idFilter);

    // ==================== REVIEWS ====================
    @GET("reviews")
    Call<List<Review>> getReviews(
            @Query("food_id") String foodIdFilter,
            @Query("select") String select,
            @Query("order") String order);

    @POST("reviews")
    Call<Void> createReview(@Body Map<String, Object> review);

    // ==================== CARTS ====================
    @GET("carts")
    Call<List<Cart>> getCart(
            @Query("user_id") String userIdFilter);

    @Headers("Prefer: return=representation")
    @POST("carts")
    Call<List<Cart>> createCart(@Body Map<String, String> cart);

    // ==================== CART ITEMS ====================
    @GET("cart_items")
    Call<List<CartItem>> getCartItems(
            @Query("cart_id") String cartIdFilter,
            @Query("food_id") String foodIdFilter,
            @Query("select") String select);

    @POST("cart_items")
    Call<Void> addCartItem(@Body Map<String, Object> cartItem);

    @PATCH("cart_items")
    Call<Void> updateCartItem(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @DELETE("cart_items")
    Call<Void> deleteCartItem(
            @Query("id") String idFilter);

    @DELETE("cart_items")
    Call<Void> clearCart(
            @Query("cart_id") String cartIdFilter);

    // ==================== ORDERS ====================
    @Headers("Prefer: return=representation")
    @POST("orders")
    Call<List<Order>> createOrder(@Body Map<String, Object> order);

    @GET("orders")
    Call<List<Order>> getOrders(
            @Query("user_id") String userIdFilter,
            @Query("select") String select,
            @Query("order") String order);

    @GET("orders")
    Call<List<Order>> getOrderById(
            @Query("id") String idFilter,
            @Query("select") String select);

    // ==================== ORDER ITEMS ====================
    @POST("order_items")
    Call<Void> createOrderItem(@Body Map<String, Object> orderItem);

    // ==================== SEARCH HISTORY ====================
    @GET("search_history")
    Call<List<SearchHistory>> getSearchHistory(
            @Query("user_id") String userIdFilter,
            @Query("order") String order,
            @Query("limit") int limit);

    @POST("search_history")
    Call<Void> saveSearch(@Body Map<String, String> search);

    @DELETE("search_history")
    Call<Void> deleteSearchHistory(
            @Query("user_id") String userIdFilter);

    // ==================== FAVORITES ====================
    @GET("favorites")
    Call<List<Favorite>> getFavorites(
            @Query("user_id") String userIdFilter,
            @Query("select") String select,
            @Query("order") String order);

    @GET("favorites")
    Call<List<Favorite>> checkFavorite(
            @Query("user_id") String userIdFilter,
            @Query("food_id") String foodIdFilter);

    @Headers("Prefer: return=representation")
    @POST("favorites")
    Call<List<Favorite>> addFavorite(@Body Map<String, String> favorite);

    @DELETE("favorites")
    Call<Void> removeFavorite(
            @Query("user_id") String userIdFilter,
            @Query("food_id") String foodIdFilter);

    // ==================== ADMIN: FOODS ====================
    @GET("foods")
    Call<List<Food>> getAdminAllFoods(
            @Query("select") String select,
            @Query("order") String order);

    @GET("foods")
    Call<List<Food>> getFoodsByCategoryFilter(
            @Query("category_id") String categoryFilter,
            @Query("select") String select,
            @Query("order") String order);

    @GET("foods")
    Call<List<Food>> adminSearchFoods(
            @Query("name") String nameFilter,
            @Query("select") String select);

    @Headers("Prefer: return=representation")
    @POST("foods")
    Call<List<Food>> createFood(@Body Map<String, Object> food);

    @Headers("Prefer: return=representation")
    @PATCH("foods")
    Call<List<Food>> updateFood(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @DELETE("foods")
    Call<Void> deleteFood(
            @Query("id") String idFilter);

    @DELETE("foods")
    Call<Void> deleteFoodsByCategory(
            @Query("category_id") String categoryFilter);

    @Headers("Prefer: return=representation")
    @PATCH("foods")
    Call<List<Food>> updateFoodsByCategory(
            @Query("category_id") String categoryFilter,
            @Body Map<String, Object> updates);

    // ==================== ADMIN: FEEDBACKS ====================
    @GET("feedbacks")
    Call<List<Feedback>> getAllFeedbacks(
            @Query("select") String select,
            @Query("order") String order);

    @GET("feedbacks")
    Call<List<Feedback>> getFeedbacksByReadStatus(
            @Query("is_read") String isReadFilter,
            @Query("select") String select,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @PATCH("feedbacks")
    Call<List<Feedback>> updateFeedback(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @DELETE("feedbacks")
    Call<Void> deleteFeedback(
            @Query("id") String idFilter);

    @Headers("Prefer: return=representation")
    @POST("feedbacks")
    Call<List<Feedback>> createFeedback(@Body Map<String, Object> feedback);

    // ==================== ADMIN: ORDERS ====================
    @GET("orders")
    Call<List<Order>> getAllOrders(
            @Query("select") String select,
            @Query("order") String order);

    @GET("orders")
    Call<List<Order>> getOrdersByStatus(
            @Query("status") String statusFilter,
            @Query("select") String select,
            @Query("order") String order);

    @GET("orders")
    Call<List<Order>> searchOrders(
            @Query("or") String orFilter,
            @Query("select") String select,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @PATCH("orders")
    Call<List<Order>> updateOrderStatus(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @GET("order_items")
    Call<List<OrderItem>> getOrderItems(
            @Query("order_id") String orderIdFilter,
            @Query("select") String select);

    // ==================== NOTIFICATIONS ====================          
    @Headers("Prefer: return=representation")
    @POST("notifications")
    Call<List<Notification>> createNotification(@Body Map<String, Object> notification);

    @GET("notifications")
    Call<List<Notification>> getUserNotifications(
            @Query("user_id") String userIdFilter,
            @Query("select") String select,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @PATCH("notifications")
    Call<List<Notification>> markNotificationRead(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @Headers("Prefer: return=representation")
    @PATCH("notifications")
    Call<List<Notification>> markAllNotificationsRead(
            @Query("user_id") String userIdFilter,
            @Query("is_read") String isReadFilter,
            @Body Map<String, Object> updates);

    @GET("notifications")
    Call<List<Notification>> getUnreadNotificationCount(
            @Query("user_id") String userIdFilter,
            @Query("is_read") String isReadFilter,
            @Query("select") String select);

    @Headers("Prefer: return=representation")
    @POST("rpc/send_broadcast_notification")
    Call<Map<String, Object>> sendBroadcastNotification(@Body Map<String, Object> payload);

    // ==================== ADMIN: REVENUE & STATISTICS ====================
    @GET("orders")
    Call<List<Order>> getOrdersByDateRange(
            @QueryMap Map<String, String> filters,
            @Query("select") String select);

    // ==================== AI FEATURES: SENTIMENT ANALYSIS ====================
    @GET("food_sentiment_stats")
    Call<List<FoodSentimentStats>> getFoodSentimentStats(
            @Query("order") String order);

    @GET("food_sentiment_stats")
    Call<List<FoodSentimentStats>> getFoodSentimentStatsByFood(
            @Query("food_id") String foodIdFilter);

    @GET("overall_sentiment_stats")
    Call<List<OverallSentimentStats>> getOverallSentimentStats();

    @Headers("Prefer: return=representation")
    @PATCH("reviews")
    Call<List<Review>> updateReviewSentiment(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @GET("reviews")
    Call<List<Review>> getAllReviews(
            @Query("select") String select,
            @Query("order") String order);

    // ==================== AI FEATURES: FOOD TRENDS ====================
    @GET("food_trends")
    Call<List<FoodTrend>> getFoodTrends(
            @Query("select") String select,
            @Query("order") String order);

    @GET("food_trends")
    Call<List<FoodTrend>> getFoodTrendsByType(
            @Query("trend_type") String trendTypeFilter,
            @Query("select") String select,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @POST("food_trends")
    Call<List<FoodTrend>> createFoodTrend(@Body Map<String, Object> foodTrend);

    @Headers("Prefer: return=representation")
    @PATCH("food_trends")
    Call<List<FoodTrend>> updateFoodTrend(
            @Query("food_id") String foodIdFilter,
            @Body Map<String, Object> updates);

    @DELETE("food_trends")
    Call<Void> deleteFoodTrend(
            @Query("food_id") String foodIdFilter);

    // ==================== ADDRESSES ====================
    @GET("addresses")
    Call<List<Address>> getAddresses(
            @Query("user_id") String userIdFilter,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @POST("addresses")
    Call<List<Address>> createAddress(@Body Map<String, Object> address);

    @Headers("Prefer: return=representation")
    @PATCH("addresses")
    Call<List<Address>> updateAddress(
            @Query("id") String idFilter,
            @Body Map<String, Object> updates);

    @Headers("Prefer: return=representation")
    @PATCH("addresses")
    Call<List<Address>> clearDefaultAddresses(
            @Query("user_id") String userIdFilter,
            @Body Map<String, Object> updates);

    @DELETE("addresses")
    Call<Void> deleteAddress(
            @Query("id") String idFilter);

    // ==================== CHAT ====================
    @GET("chat_messages")
    Call<List<ChatMessage>> getChatMessages(
            @Query("room_user_id") String roomUserIdFilter,
            @Query("select") String select,
            @Query("order") String order);

    @GET("chat_messages")
    Call<List<ChatMessage>> getAllChatMessages(
            @Query("select") String select,
            @Query("order") String order);

    @Headers("Prefer: return=representation")
    @POST("chat_messages")
    Call<List<ChatMessage>> createChatMessage(@Body Map<String, Object> chatMessage);

    // ==================== VOUCHERS ====================
    @GET("vouchers")
    Call<List<com.example.food_order_app.model.Voucher>> getVouchers(
            @Query("is_active") String isActiveParam,
            @Query("is_public") String isPublicParam,
            @Query("order") String orderParam);

    @GET("vouchers")
    Call<List<com.example.food_order_app.model.Voucher>> getVoucherByCode(
            @Query("code") String codeFilter,
            @Query("is_active") String isActiveParam);
}
