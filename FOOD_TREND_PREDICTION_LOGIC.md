# Food Trend Prediction Logic (Current Implementation)

## 1. Scope
Tài liệu này mô tả chính xác logic hiện tại của màn dự đoán xu hướng món ăn trong app Android.
Nguồn logic chính nằm ở FoodTrendPredictionActivity.

## 2. Entry Points
- Mở màn hình: gọi loadTrends() để đọc dữ liệu trend đã có và hiển thị.
- Nhấn nút Phân tích: gọi generatePredictions() để chạy lại dự đoán cho toàn bộ món ăn đang available.

## 3. Data Sources Used
- Bảng foods: lấy danh sách món qua API getAllFoods("eq.true", "name.asc").
- Bảng food_sentiment_stats: lấy sentiment theo từng món qua API getFoodSentimentStatsByFood("eq.{foodId}").
- Bảng food_trends: xóa bản ghi cũ theo food_id rồi tạo bản ghi mới.

Các model liên quan trực tiếp:
- Food: dùng food.id, food.isPopular.
- FoodSentimentStats: dùng totalReviews, avgSentimentScore, positivePercent, negativePercent.
- FoodTrend: đích lưu kết quả.

## 4. High-Level Flow
1. Người dùng bấm Phân tích.
2. UI bị khóa tạm thời:
- disable nút Phân tích
- đổi text nút thành Đang phân tích...
- hiện ProgressBar
3. Gọi API lấy toàn bộ foods đang available.
4. Duyệt tuần tự từng món bằng đệ quy analyzeFoodsTrends(foods, index):
- xử lý xong món hiện tại mới sang món kế tiếp
- không chạy song song
5. Với mỗi món, gọi predictFoodTrend(food, onComplete):
- lấy sentiment stats của món đó
- suy ra trend_type + confidence_score + sales_trend + sentiment_trend
- lưu xuống DB bằng saveFoodTrend(...)
6. Khi xử lý hết danh sách:
- enable lại nút Phân tích
- text nút về Phân tích
- ẩn ProgressBar
- Toast số món đã phân tích
- gọi loadTrends() để reload danh sách mới nhất

## 5. Classification Rules (Actual If/Else)
Nếu có sentimentStats và totalReviews > 0:
- sentimentTrend = avgSentimentScore

Luật phân loại theo thứ tự ưu tiên:
1. Nếu positivePercent > 70 và food.isPopular == true
- trendType = hot_seller
- confidenceScore = 0.85
- salesTrend = 25.0

2. Ngược lại, nếu negativePercent > 50
- trendType = at_risk
- confidenceScore = 0.75
- salesTrend = -15.0

3. Ngược lại, nếu negativePercent > 30
- trendType = declining
- confidenceScore = 0.70
- salesTrend = -8.0

4. Ngược lại
- trendType = stable
- confidenceScore = 0.65
- salesTrend = 2.0

Nếu không có sentiment data (null hoặc totalReviews == 0):
- trendType = stable
- confidenceScore = 0.50
- salesTrend = 0
- sentimentTrend = 0.5

## 6. Persistence Logic
Cho mỗi food_id:
1. Gọi deleteFoodTrend("eq.{foodId}") để xóa trend cũ.
2. Tạo payload mới:
- food_id
- trend_type
- confidence_score
- prediction_period = 30_days
- sales_trend
- sentiment_trend
3. Gọi createFoodTrend(payload).
4. Dù create thành công hay fail vẫn gọi onComplete để không kẹt luồng xử lý món tiếp theo.

## 7. Error Handling Behavior
- Lỗi khi lấy danh sách foods:
- reset UI về trạng thái bình thường
- hiện Toast lỗi
- dừng quy trình

- Lỗi khi lấy sentiment của một món:
- bỏ qua món đó
- vẫn tiếp tục món kế tiếp

- Lỗi khi delete/create trend:
- không retry
- vẫn tiếp tục món kế tiếp

## 8. Display and Filter Logic
Sau khi loadTrends():
- gọi API getFoodTrends("*,foods(*)", "confidence_score.desc")
- giữ vào allTrends
- lọc theo currentFilter để hiển thị RecyclerView

Filter hiện có:
- all
- hot_seller
- declining
- at_risk
- stable

## 9. Important Notes About Current Logic
- dateFrom (30 ngày trước) được tính trong predictFoodTrend nhưng hiện chưa dùng vào query nào.
- salesTrend hiện là giá trị hard-code theo rule, chưa tính từ lịch sử đơn hàng thực tế.
- Import Order và OrderItem có trong file nhưng chưa được sử dụng trong thuật toán hiện tại.
- Thuật toán là rule-based heuristic, chưa có mô hình ML thực sự.

## 10. Pseudocode Snapshot
```text
generatePredictions():
  lockUI()
  foods = getAllFoods(is_available=true)
  for food in foods (sequential recursion):
    stats = getFoodSentimentStatsByFood(food.id)
    if stats exists and stats.totalReviews > 0:
      sentimentTrend = stats.avgSentimentScore
      if stats.positivePercent > 70 and food.isPopular:
        trend = hot_seller, confidence=0.85, sales=25
      else if stats.negativePercent > 50:
        trend = at_risk, confidence=0.75, sales=-15
      else if stats.negativePercent > 30:
        trend = declining, confidence=0.70, sales=-8
      else:
        trend = stable, confidence=0.65, sales=2
    else:
      trend = stable, confidence=0.50, sales=0, sentimentTrend=0.5

    delete old trend by food_id
    insert new trend

  unlockUI()
  reload trends list
```
