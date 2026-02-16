-- ============================================
-- SUPABASE SQL SCHEMA - ỨNG DỤNG ĐẶT MÓN ĂN
-- DATABASE HOÀN CHỈNH (Chạy 1 lần trong SQL Editor)
-- ============================================

-- ============================================
-- 1. BẢNG USERS
-- ============================================
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    address TEXT DEFAULT '',
    avatar_url TEXT DEFAULT '',
    role TEXT NOT NULL DEFAULT 'user' CHECK (role IN ('admin', 'user')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS password TEXT;
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);

-- ============================================
-- 2. BẢNG PASSWORD_RESET_CODES
-- ============================================
CREATE TABLE IF NOT EXISTS public.password_reset_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT NOT NULL,
    code TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 3. BẢNG CATEGORIES
-- ============================================
CREATE TABLE IF NOT EXISTS public.categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    icon_url TEXT DEFAULT '',
    sort_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 4. BẢNG FOODS
-- ============================================
CREATE TABLE IF NOT EXISTS public.foods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID REFERENCES public.categories(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    description TEXT DEFAULT '',
    price DECIMAL(12,0) NOT NULL DEFAULT 0,
    discount_percent INT DEFAULT 0 CHECK (discount_percent >= 0 AND discount_percent <= 100),
    image_url TEXT DEFAULT '',
    is_popular BOOLEAN DEFAULT FALSE,
    is_recommended BOOLEAN DEFAULT FALSE,
    is_available BOOLEAN DEFAULT TRUE,
    avg_rating DECIMAL(2,1) DEFAULT 0,
    total_reviews INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_foods_category ON public.foods(category_id);
CREATE INDEX IF NOT EXISTS idx_foods_popular ON public.foods(is_popular);
CREATE INDEX IF NOT EXISTS idx_foods_recommended ON public.foods(is_recommended);

-- ============================================
-- 5. BẢNG FOOD_IMAGES
-- ============================================
CREATE TABLE IF NOT EXISTS public.food_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    food_id UUID NOT NULL REFERENCES public.foods(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 6. BẢNG REVIEWS
-- ============================================
CREATE TABLE IF NOT EXISTS public.reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    food_id UUID NOT NULL REFERENCES public.foods(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 7. BẢNG CARTS
-- ============================================
CREATE TABLE IF NOT EXISTS public.carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 8. BẢNG CART_ITEMS
-- ============================================
CREATE TABLE IF NOT EXISTS public.cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL REFERENCES public.carts(id) ON DELETE CASCADE,
    food_id UUID NOT NULL REFERENCES public.foods(id) ON DELETE CASCADE,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(cart_id, food_id)
);

-- ============================================
-- 9. BẢNG ADDRESSES
-- ============================================
CREATE TABLE IF NOT EXISTS public.addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    receiver_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    address TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 10. BẢNG ORDERS
-- ============================================
CREATE TABLE IF NOT EXISTS public.orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    order_code TEXT UNIQUE NOT NULL,
    receiver_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    address TEXT NOT NULL,
    payment_method TEXT NOT NULL DEFAULT 'cod' CHECK (payment_method IN ('cod', 'bank_transfer', 'momo', 'zalopay')),
    note TEXT DEFAULT '',
    subtotal DECIMAL(12,0) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,0) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,0) NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'confirmed', 'preparing', 'delivering', 'delivered', 'cancelled')),
    estimated_delivery TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_orders_user ON public.orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON public.orders(status);

-- ============================================
-- 11. BẢNG ORDER_ITEMS
-- ============================================
CREATE TABLE IF NOT EXISTS public.order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    food_id UUID REFERENCES public.foods(id) ON DELETE SET NULL,
    food_name TEXT NOT NULL,
    food_image TEXT DEFAULT '',
    price DECIMAL(12,0) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    subtotal DECIMAL(12,0) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 12. BẢNG SEARCH_HISTORY
-- ============================================
CREATE TABLE IF NOT EXISTS public.search_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    keyword TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- 13. BẢNG FEEDBACKS (Phản hồi khách hàng)
-- ============================================
CREATE TABLE IF NOT EXISTS public.feedbacks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    subject TEXT DEFAULT '',
    content TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- TRIGGERS
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_users_updated_at ON public.users;
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON public.users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_foods_updated_at ON public.foods;
CREATE TRIGGER update_foods_updated_at BEFORE UPDATE ON public.foods FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_carts_updated_at ON public.carts;
CREATE TRIGGER update_carts_updated_at BEFORE UPDATE ON public.carts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_cart_items_updated_at ON public.cart_items;
CREATE TRIGGER update_cart_items_updated_at BEFORE UPDATE ON public.cart_items FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_orders_updated_at ON public.orders;
CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON public.orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_addresses_updated_at ON public.addresses;
CREATE TRIGGER update_addresses_updated_at BEFORE UPDATE ON public.addresses FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_reviews_updated_at ON public.reviews;
CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON public.reviews FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_feedbacks_updated_at ON public.feedbacks;
CREATE TRIGGER update_feedbacks_updated_at BEFORE UPDATE ON public.feedbacks FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- RLS: CHO PHÉP TẤT CẢ (Custom Auth)
-- ============================================
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN SELECT unnest(ARRAY[
        'users','password_reset_codes','categories','foods','food_images',
        'reviews','carts','cart_items','addresses','orders','order_items','search_history','feedbacks'
    ])
    LOOP
        EXECUTE format('ALTER TABLE public.%I ENABLE ROW LEVEL SECURITY', tbl);
        EXECUTE format('DROP POLICY IF EXISTS "Allow all select on %I" ON public.%I', tbl, tbl);
        EXECUTE format('CREATE POLICY "Allow all select on %I" ON public.%I FOR SELECT USING (true)', tbl, tbl);
        EXECUTE format('DROP POLICY IF EXISTS "Allow all insert on %I" ON public.%I', tbl, tbl);
        EXECUTE format('CREATE POLICY "Allow all insert on %I" ON public.%I FOR INSERT WITH CHECK (true)', tbl, tbl);
        EXECUTE format('DROP POLICY IF EXISTS "Allow all update on %I" ON public.%I', tbl, tbl);
        EXECUTE format('CREATE POLICY "Allow all update on %I" ON public.%I FOR UPDATE USING (true)', tbl, tbl);
        EXECUTE format('DROP POLICY IF EXISTS "Allow all delete on %I" ON public.%I', tbl, tbl);
        EXECUTE format('CREATE POLICY "Allow all delete on %I" ON public.%I FOR DELETE USING (true)', tbl, tbl);
    END LOOP;
END $$;

-- Xóa policies cũ
DROP POLICY IF EXISTS "Anyone can read users" ON public.users;
DROP POLICY IF EXISTS "Anyone can insert users" ON public.users;
DROP POLICY IF EXISTS "Anyone can update users" ON public.users;
DROP POLICY IF EXISTS "Anyone can read reset codes" ON public.password_reset_codes;
DROP POLICY IF EXISTS "Anyone can insert reset codes" ON public.password_reset_codes;
DROP POLICY IF EXISTS "Anyone can update reset codes" ON public.password_reset_codes;

-- ============================================
-- DỮ LIỆU MẪU: DANH MỤC
-- ============================================
INSERT INTO public.categories (name, sort_order) VALUES
    ('Tất cả', 0), ('Món chính', 1), ('Đồ uống', 2), ('Tráng miệng', 3), ('Khai vị', 4)
ON CONFLICT DO NOTHING;

-- ============================================
-- DỮ LIỆU MẪU: MÓN ĂN
-- ============================================
DO $$
DECLARE
    cat_main UUID; cat_drink UUID; cat_dessert UUID; cat_appetizer UUID;
BEGIN
    SELECT id INTO cat_main FROM public.categories WHERE name = 'Món chính' LIMIT 1;
    SELECT id INTO cat_drink FROM public.categories WHERE name = 'Đồ uống' LIMIT 1;
    SELECT id INTO cat_dessert FROM public.categories WHERE name = 'Tráng miệng' LIMIT 1;
    SELECT id INTO cat_appetizer FROM public.categories WHERE name = 'Khai vị' LIMIT 1;

    IF NOT EXISTS (SELECT 1 FROM public.foods LIMIT 1) THEN
        INSERT INTO public.foods (category_id, name, description, price, discount_percent, image_url, is_popular, is_recommended) VALUES
        (cat_main, 'Bò cuộn phô mai', 'Bò Mỹ cuộn phô mai nướng thơm lừng, phục vụ kèm rau sống', 250000, 15, 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400', TRUE, TRUE),
        (cat_main, 'Rau muống xào tỏi', 'Rau muống xào tỏi giòn tan, chế biến theo phong cách đặc biệt', 200000, 10, 'https://images.unsplash.com/photo-1512058564366-18510be2db19?w=400', TRUE, TRUE),
        (cat_main, 'Sườn xào chua ngọt', 'Sườn non xào chua ngọt đậm đà hương vị', 180000, 10, 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=400', TRUE, FALSE),
        (cat_main, 'Ếch xào măng', 'Ếch đồng xào măng tươi, đậm chất quê hương', 220000, 0, 'https://images.unsplash.com/photo-1476224203421-9ac39bcb3327?w=400', TRUE, TRUE),
        (cat_main, 'Cơm chiên hải sản', 'Cơm chiên với tôm, mực, nghêu tươi ngon', 150000, 5, 'https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400', FALSE, TRUE),
        (cat_main, 'Gà nướng mật ong', 'Đùi gà nướng mật ong vàng óng, da giòn thịt mềm', 280000, 20, 'https://images.unsplash.com/photo-1598103442097-8b74394b95c6?w=400', TRUE, TRUE),
        (cat_main, 'Cá kho tộ', 'Cá lóc kho tộ đậm đà ăn kèm cơm trắng', 160000, 0, 'https://images.unsplash.com/photo-1467003909585-2f8a72700288?w=400', FALSE, FALSE),
        (cat_main, 'Phở bò tái', 'Phở bò tái nước dùng hầm xương 12 tiếng', 85000, 0, 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?w=400', TRUE, TRUE),
        (cat_drink, 'Trà sữa trân châu', 'Trà sữa thơm béo với trân châu đường đen', 45000, 0, 'https://images.unsplash.com/photo-1558857563-b371033873b8?w=400', TRUE, TRUE),
        (cat_drink, 'Sinh tố bơ', 'Sinh tố bơ Đắk Lắk béo ngậy thêm sữa đặc', 55000, 10, 'https://images.unsplash.com/photo-1638176066666-ffb2f013c7dd?w=400', FALSE, TRUE),
        (cat_drink, 'Nước ép cam', 'Nước cam tươi nguyên chất giàu vitamin C', 40000, 0, 'https://images.unsplash.com/photo-1621506289937-a8e4df240d0b?w=400', FALSE, FALSE),
        (cat_drink, 'Cà phê sữa đá', 'Cà phê phin truyền thống đậm đà thơm lừng', 35000, 0, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', TRUE, TRUE),
        (cat_dessert, 'Chè khúc bạch', 'Chè khúc bạch mát lạnh với nhãn, vải, nước cốt dừa', 50000, 5, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400', FALSE, TRUE),
        (cat_dessert, 'Bánh flan', 'Bánh flan caramel mềm mịn tan trong miệng', 30000, 0, 'https://images.unsplash.com/photo-1624353365286-3f8d62daad51?w=400', FALSE, FALSE),
        (cat_dessert, 'Kem dừa', 'Kem dừa tươi nguyên chất trong trái dừa thật', 65000, 10, 'https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=400', TRUE, TRUE),
        (cat_appetizer, 'Gỏi cuốn tôm thịt', 'Gỏi cuốn tươi mát với tôm, thịt heo, rau sống', 60000, 0, 'https://images.unsplash.com/photo-1562967916-eb82221dfb92?w=400', FALSE, TRUE),
        (cat_appetizer, 'Chả giò', 'Chả giò giòn rụm nhân thịt heo, tôm, miến', 70000, 5, 'https://images.unsplash.com/photo-1544025162-d76694265947?w=400', TRUE, FALSE),
        (cat_appetizer, 'Súp cua', 'Súp cua trứng bắc thảo nóng hổi bổ dưỡng', 55000, 0, 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=400', FALSE, TRUE);
    END IF;
END $$;
