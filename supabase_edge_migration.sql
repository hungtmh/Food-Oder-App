-- ============================================
-- MIGRATION: VOUCHER + SEPAY EDGE FUNCTION SUPPORT
-- Chay file nay de cap nhat DB hien tai an toan (idempotent)
-- ============================================

-- 1) Voucher columns (tuong thich schema cu)
ALTER TABLE public.vouchers
ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS usage_limit INT,
ADD COLUMN IF NOT EXISTS used_count INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS limit_per_user INT DEFAULT 1,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_vouchers_code ON public.vouchers(code);
CREATE INDEX IF NOT EXISTS idx_vouchers_active_public ON public.vouchers(is_active, is_public);

-- Trigger updated_at cho vouchers
DROP TRIGGER IF EXISTS update_vouchers_updated_at ON public.vouchers;
CREATE TRIGGER update_vouchers_updated_at
BEFORE UPDATE ON public.vouchers
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- RLS vouchers (public list)
ALTER TABLE public.vouchers ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow all select on vouchers" ON public.vouchers;
CREATE POLICY "Allow all select on vouchers" ON public.vouchers FOR SELECT USING (true);

-- 2) Orders columns for QR payment and voucher trace
ALTER TABLE public.orders
ADD COLUMN IF NOT EXISTS voucher_id UUID REFERENCES public.vouchers(id) ON DELETE SET NULL,
ADD COLUMN IF NOT EXISTS applied_voucher_code TEXT,
ADD COLUMN IF NOT EXISTS payment_status TEXT NOT NULL DEFAULT 'pending' CHECK (payment_status IN ('pending', 'paid', 'failed', 'expired')),
ADD COLUMN IF NOT EXISTS payment_reference TEXT,
ADD COLUMN IF NOT EXISTS payment_qr_url TEXT,
ADD COLUMN IF NOT EXISTS paid_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON public.orders(payment_status);
CREATE INDEX IF NOT EXISTS idx_orders_payment_reference ON public.orders(payment_reference);

-- 3) RPC helper cho Edge Function
CREATE OR REPLACE FUNCTION public.increment_voucher_usage(p_voucher_id UUID)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE public.vouchers
    SET used_count = COALESCE(used_count, 0) + 1,
        updated_at = NOW()
    WHERE id = p_voucher_id;
END;
$$;

-- 4) Voucher samples (public/private)
INSERT INTO public.vouchers (code, title, discount_type, discount_value, max_discount_amount, min_order_value, end_date, is_public, is_active)
VALUES
    ('GIAM50K', 'Giam truc tiep 50K cho don tu 200K', 'fixed_amount', 50000, 50000, 200000, NOW() + INTERVAL '30 days', TRUE, TRUE),
    ('GIAM20', 'Giam 20% toi da 30K', 'percent', 20, 30000, 100000, NOW() + INTERVAL '30 days', TRUE, TRUE),
    ('FREESHIP', 'Ho tro phi van chuyen toi da 15K', 'fixed_amount', 15000, 15000, 0, NOW() + INTERVAL '30 days', TRUE, TRUE),
    ('SECRET', 'Ma private giam 100K', 'fixed_amount', 100000, 100000, 0, NOW() + INTERVAL '30 days', FALSE, TRUE)
ON CONFLICT (code) DO NOTHING;
