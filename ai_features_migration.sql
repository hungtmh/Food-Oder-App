-- ============================================
-- AI FEATURES MIGRATION - Nhóm 15
-- Thêm các trường cho phân tích cảm xúc và dự đoán xu hướng
-- ============================================

-- Thêm cột sentiment vào bảng reviews
ALTER TABLE public.reviews 
ADD COLUMN IF NOT EXISTS sentiment TEXT CHECK (sentiment IN ('positive', 'negative', 'neutral')),
ADD COLUMN IF NOT EXISTS sentiment_score DECIMAL(3,2) DEFAULT 0;

-- Thêm cột sentiment vào bảng feedbacks
ALTER TABLE public.feedbacks 
ADD COLUMN IF NOT EXISTS sentiment TEXT CHECK (sentiment IN ('positive', 'negative', 'neutral')),
ADD COLUMN IF NOT EXISTS sentiment_score DECIMAL(3,2) DEFAULT 0;

-- Tạo bảng food_trends để lưu dự đoán xu hướng
CREATE TABLE IF NOT EXISTS public.food_trends (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    food_id UUID NOT NULL REFERENCES public.foods(id) ON DELETE CASCADE,
    trend_type TEXT NOT NULL CHECK (trend_type IN ('hot_seller', 'declining', 'at_risk', 'stable')),
    confidence_score DECIMAL(3,2) DEFAULT 0,
    prediction_period TEXT DEFAULT '30_days',
    sales_trend DECIMAL(5,2) DEFAULT 0, -- % tăng/giảm
    sentiment_trend DECIMAL(3,2) DEFAULT 0,
    notes TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_food_trends_food ON public.food_trends(food_id);
CREATE INDEX IF NOT EXISTS idx_food_trends_type ON public.food_trends(trend_type);

-- Trigger cho food_trends
DROP TRIGGER IF EXISTS update_food_trends_updated_at ON public.food_trends;
CREATE TRIGGER update_food_trends_updated_at 
BEFORE UPDATE ON public.food_trends 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- RLS cho food_trends
ALTER TABLE public.food_trends ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow all select on food_trends" ON public.food_trends;
CREATE POLICY "Allow all select on food_trends" ON public.food_trends FOR SELECT USING (true);
DROP POLICY IF EXISTS "Allow all insert on food_trends" ON public.food_trends;
CREATE POLICY "Allow all insert on food_trends" ON public.food_trends FOR INSERT WITH CHECK (true);
DROP POLICY IF EXISTS "Allow all update on food_trends" ON public.food_trends;
CREATE POLICY "Allow all update on food_trends" ON public.food_trends FOR UPDATE USING (true);
DROP POLICY IF EXISTS "Allow all delete on food_trends" ON public.food_trends;
CREATE POLICY "Allow all delete on food_trends" ON public.food_trends FOR DELETE USING (true);

-- Tạo view để thống kê cảm xúc theo món ăn
CREATE OR REPLACE VIEW public.food_sentiment_stats AS
SELECT 
    f.id as food_id,
    f.name as food_name,
    f.image_url,
    COUNT(r.id) as total_reviews,
    COUNT(CASE WHEN r.sentiment = 'positive' THEN 1 END) as positive_count,
    COUNT(CASE WHEN r.sentiment = 'negative' THEN 1 END) as negative_count,
    COUNT(CASE WHEN r.sentiment = 'neutral' THEN 1 END) as neutral_count,
    ROUND(COUNT(CASE WHEN r.sentiment = 'positive' THEN 1 END)::DECIMAL * 100 / NULLIF(COUNT(r.id), 0), 2) as positive_percent,
    ROUND(COUNT(CASE WHEN r.sentiment = 'negative' THEN 1 END)::DECIMAL * 100 / NULLIF(COUNT(r.id), 0), 2) as negative_percent,
    ROUND(COUNT(CASE WHEN r.sentiment = 'neutral' THEN 1 END)::DECIMAL * 100 / NULLIF(COUNT(r.id), 0), 2) as neutral_percent,
    ROUND(AVG(r.sentiment_score), 2) as avg_sentiment_score,
    ROUND(AVG(r.rating), 2) as avg_rating
FROM public.foods f
LEFT JOIN public.reviews r ON f.id = r.food_id
GROUP BY f.id, f.name, f.image_url;

-- Tạo view để thống kê cảm xúc tổng quan
CREATE OR REPLACE VIEW public.overall_sentiment_stats AS
SELECT 
    COUNT(id) as total_reviews,
    COUNT(CASE WHEN sentiment = 'positive' THEN 1 END) as positive_count,
    COUNT(CASE WHEN sentiment = 'negative' THEN 1 END) as negative_count,
    COUNT(CASE WHEN sentiment = 'neutral' THEN 1 END) as neutral_count,
    ROUND(COUNT(CASE WHEN sentiment = 'positive' THEN 1 END)::DECIMAL * 100 / NULLIF(COUNT(id), 0), 2) as positive_percent,
    ROUND(COUNT(CASE WHEN sentiment = 'negative' THEN 1 END)::DECIMAL * 100 / NULLIF(COUNT(id), 0), 2) as negative_percent,
    ROUND(COUNT(CASE WHEN sentiment = 'neutral' THEN 1 END)::DECIMAL * 100 / NULLIF(COUNT(id), 0), 2) as neutral_percent,
    ROUND(AVG(sentiment_score), 2) as avg_sentiment_score
FROM public.reviews
WHERE sentiment IS NOT NULL;
