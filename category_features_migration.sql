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

-- 3) Admin-ready category features
ALTER TABLE public.categories
ADD COLUMN IF NOT EXISTS parent_id uuid NULL REFERENCES public.categories(id),
ADD COLUMN IF NOT EXISTS display_start_time time NULL,
ADD COLUMN IF NOT EXISTS display_end_time time NULL,
ADD COLUMN IF NOT EXISTS is_deleted boolean NOT NULL DEFAULT false,
ADD COLUMN IF NOT EXISTS deleted_at timestamptz NULL,
ADD COLUMN IF NOT EXISTS updated_by uuid NULL REFERENCES public.users(id);

ALTER TABLE public.foods
ADD COLUMN IF NOT EXISTS stock_quantity INT NOT NULL DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON public.categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_is_deleted ON public.categories(is_deleted);
CREATE INDEX IF NOT EXISTS idx_categories_name_lower ON public.categories ((lower(name)));

-- 4) Audit log for category changes
CREATE TABLE IF NOT EXISTS public.category_change_logs (
	id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	category_id uuid NULL REFERENCES public.categories(id),
	action text NOT NULL,
	changed_by uuid NULL REFERENCES public.users(id),
	changed_at timestamptz NOT NULL DEFAULT now(),
	before_data jsonb NULL,
	after_data jsonb NULL
);

CREATE INDEX IF NOT EXISTS idx_category_change_logs_category_id ON public.category_change_logs(category_id);
CREATE INDEX IF NOT EXISTS idx_category_change_logs_changed_at ON public.category_change_logs(changed_at DESC);
