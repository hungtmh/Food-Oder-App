-- ============================================
-- REVIEW MIGRATION AND TRIGGER SETUP
-- ============================================

-- ============================================
-- 1. CẬP NHẬT DỮ LIỆU CŨ - Tính tổng số review và điểm sao
-- ============================================
-- Script này tính toán lại tất cả avg_rating và total_reviews từ các review hiện tại
-- Chỉ cần chạy một lần để cập nhật dữ liệu cũ bị miss

UPDATE public.foods f
SET 
    avg_rating = CASE 
        WHEN review_stats.total > 0 
        THEN ROUND(CAST(review_stats.total_rating AS DECIMAL(10,2)) / review_stats.total, 1)
        ELSE 0
    END,
    total_reviews = COALESCE(review_stats.total, 0),
    updated_at = NOW()
FROM (
    SELECT 
        r.food_id,
        COUNT(r.id) as total,
        SUM(r.rating) as total_rating
    FROM public.reviews r
    GROUP BY r.food_id
) review_stats
WHERE f.id = review_stats.food_id;

-- Xác nhận cập nhật
SELECT 'Dữ liệu cũ đã được cập nhật' as status;

-- ============================================
-- 2. TRIGGER - CẬP NHẬT AUTOMATICALLY KHI THÊM REVIEW MỚI
-- ============================================

-- Tạo function để cập nhật avg_rating và total_reviews
CREATE OR REPLACE FUNCTION update_food_review_stats_on_insert()
RETURNS TRIGGER AS $$
DECLARE
    total_count INT;
    avg_score DECIMAL(2, 1);
BEGIN
    -- Tính toán lại từ tất cả reviews của food này
    SELECT 
        COUNT(*),
        COALESCE(ROUND(AVG(rating)::NUMERIC, 1), 0)
    INTO total_count, avg_score
    FROM public.reviews
    WHERE food_id = NEW.food_id;

    -- Cập nhật bảng foods
    UPDATE public.foods
    SET 
        total_reviews = total_count,
        avg_rating = avg_score,
        updated_at = NOW()
    WHERE id = NEW.food_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger khi INSERT review mới
DROP TRIGGER IF EXISTS trigger_update_food_stats_after_insert_review ON public.reviews;
CREATE TRIGGER trigger_update_food_stats_after_insert_review
AFTER INSERT ON public.reviews
FOR EACH ROW
EXECUTE FUNCTION update_food_review_stats_on_insert();

-- ============================================
-- 3. TRIGGER - CẬP NHẬT KHI SỬA REVIEW (nếu có)
-- ============================================

CREATE OR REPLACE FUNCTION update_food_review_stats_on_update()
RETURNS TRIGGER AS $$
DECLARE
    total_count INT;
    avg_score DECIMAL(2, 1);
BEGIN
    -- Tính toán lại từ tất cả reviews của food này
    SELECT 
        COUNT(*),
        COALESCE(ROUND(AVG(rating)::NUMERIC, 1), 0)
    INTO total_count, avg_score
    FROM public.reviews
    WHERE food_id = NEW.food_id;

    -- Cập nhật bảng foods
    UPDATE public.foods
    SET 
        total_reviews = total_count,
        avg_rating = avg_score,
        updated_at = NOW()
    WHERE id = NEW.food_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger khi UPDATE review
DROP TRIGGER IF EXISTS trigger_update_food_stats_after_update_review ON public.reviews;
CREATE TRIGGER trigger_update_food_stats_after_update_review
AFTER UPDATE ON public.reviews
FOR EACH ROW
EXECUTE FUNCTION update_food_review_stats_on_update();

-- ============================================
-- 4. TRIGGER - CẬP NHẬT KHI XÓA REVIEW
-- ============================================

CREATE OR REPLACE FUNCTION update_food_review_stats_on_delete()
RETURNS TRIGGER AS $$
DECLARE
    total_count INT;
    avg_score DECIMAL(2, 1);
BEGIN
    -- Tính toán lại từ tất cả reviews còn lại của food này (không tính review vừa xóa)
    SELECT 
        COUNT(*),
        COALESCE(ROUND(AVG(rating)::NUMERIC, 1), 0)
    INTO total_count, avg_score
    FROM public.reviews
    WHERE food_id = OLD.food_id;

    -- Cập nhật bảng foods
    UPDATE public.foods
    SET 
        total_reviews = total_count,
        avg_rating = avg_score,
        updated_at = NOW()
    WHERE id = OLD.food_id;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Tạo trigger khi DELETE review
DROP TRIGGER IF EXISTS trigger_update_food_stats_after_delete_review ON public.reviews;
CREATE TRIGGER trigger_update_food_stats_after_delete_review
AFTER DELETE ON public.reviews
FOR EACH ROW
EXECUTE FUNCTION update_food_review_stats_on_delete();

-- ============================================
-- 5. KIỂM TRA CÁC TRIGGER ĐÃ TẠO
-- ============================================

-- Xem các trigger liên quan đến reviews
SELECT trigger_name, event_manipulation, event_object_table
FROM information_schema.triggers
WHERE event_object_table = 'reviews' OR trigger_name LIKE '%review_stats%'
ORDER BY trigger_name;
