-- ============================================
-- MIGRATION: Thêm cột image_url vào bảng reviews
-- Chạy script này nếu bảng reviews đã tồn tại
-- ============================================

-- Kiểm tra và thêm cột image_url nếu chưa có
ALTER TABLE public.reviews
ADD COLUMN IF NOT EXISTS image_url TEXT DEFAULT '';

-- Xác nhận cột đã được thêm
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'reviews' AND column_name = 'image_url';
