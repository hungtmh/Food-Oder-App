-- ============================================
-- CATEGORY FEATURES MIGRATION
-- Manager role support + category icon defaults
-- ============================================

-- 1) Extend users.role check to support manager role
ALTER TABLE public.users
DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE public.users
ADD CONSTRAINT users_role_check
CHECK (role IN ('admin', 'manager', 'user'));

-- 2) Add icon URLs for current 4 categories shown in admin screen
UPDATE public.categories
SET icon_url = CASE name
		WHEN 'Món chính' THEN 'https://images.unsplash.com/photo-1544025162-d76694265947?w=300&h=300&fit=crop'
		WHEN 'Đồ uống' THEN 'https://images.unsplash.com/photo-1544145945-f90425340c7e?w=300&h=300&fit=crop'
		WHEN 'Tráng miệng' THEN 'https://images.unsplash.com/photo-1551024601-bec78aea704b?w=300&h=300&fit=crop'
		WHEN 'Khai vị' THEN 'https://images.unsplash.com/photo-1473093295043-cdd812d0e601?w=300&h=300&fit=crop'
		ELSE icon_url
END
WHERE name IN ('Món chính', 'Đồ uống', 'Tráng miệng', 'Khai vị')
	AND COALESCE(icon_url, '') = '';
