-- MIGRATION: Admin voucher management support
-- Run in Supabase SQL editor for environments that do not yet allow write on vouchers.

ALTER TABLE public.vouchers
ADD COLUMN IF NOT EXISTS usage_limit INT,
ADD COLUMN IF NOT EXISTS used_count INT DEFAULT 0,
ADD COLUMN IF NOT EXISTS limit_per_user INT DEFAULT 1,
ADD COLUMN IF NOT EXISTS is_public BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_vouchers_active_public ON public.vouchers(is_active, is_public);

ALTER TABLE public.vouchers ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow all select on vouchers" ON public.vouchers;
CREATE POLICY "Allow all select on vouchers"
ON public.vouchers FOR SELECT
USING (true);

DROP POLICY IF EXISTS "Allow all insert on vouchers" ON public.vouchers;
CREATE POLICY "Allow all insert on vouchers"
ON public.vouchers FOR INSERT
WITH CHECK (true);

DROP POLICY IF EXISTS "Allow all update on vouchers" ON public.vouchers;
CREATE POLICY "Allow all update on vouchers"
ON public.vouchers FOR UPDATE
USING (true)
WITH CHECK (true);

CREATE TABLE IF NOT EXISTS public.user_voucher_usage (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
	voucher_id UUID NOT NULL REFERENCES public.vouchers(id) ON DELETE CASCADE,
	order_id UUID REFERENCES public.orders(id) ON DELETE SET NULL,
	used_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	CONSTRAINT user_voucher_usage_unique UNIQUE (user_id, voucher_id)
);

CREATE INDEX IF NOT EXISTS idx_user_voucher_usage_user_id ON public.user_voucher_usage(user_id);
CREATE INDEX IF NOT EXISTS idx_user_voucher_usage_voucher_id ON public.user_voucher_usage(voucher_id);

ALTER TABLE public.user_voucher_usage ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Allow all select on user_voucher_usage" ON public.user_voucher_usage;
CREATE POLICY "Allow all select on user_voucher_usage"
ON public.user_voucher_usage FOR SELECT
USING (true);

CREATE OR REPLACE FUNCTION public.create_order_with_voucher_tx(
	p_user_id UUID,
	p_order_code TEXT,
	p_receiver_name TEXT,
	p_phone TEXT,
	p_address TEXT,
	p_payment_method TEXT,
	p_note TEXT,
	p_subtotal NUMERIC,
	p_order_type TEXT,
	p_payment_status TEXT,
	p_payment_reference TEXT,
	p_payment_qr_url TEXT,
	p_voucher_code TEXT DEFAULT NULL
)
RETURNS JSONB
LANGUAGE plpgsql
AS $$
DECLARE
	v_voucher public.vouchers%ROWTYPE;
	v_discount NUMERIC := 0;
	v_total NUMERIC := p_subtotal;
	v_applied_code TEXT := NULL;
	v_order public.orders%ROWTYPE;
	v_now TIMESTAMPTZ := NOW();
BEGIN
	IF p_subtotal <= 0 THEN
		RAISE EXCEPTION 'Subtotal must be greater than 0';
	END IF;

	IF p_voucher_code IS NOT NULL AND btrim(p_voucher_code) <> '' THEN
		SELECT * INTO v_voucher
		FROM public.vouchers
		WHERE code = upper(btrim(p_voucher_code))
			AND is_active = TRUE
		FOR UPDATE;

		IF NOT FOUND THEN
			RAISE EXCEPTION 'Voucher is invalid or inactive';
		END IF;

		IF v_voucher.start_date IS NOT NULL AND v_now < v_voucher.start_date THEN
			RAISE EXCEPTION 'Voucher is not active yet';
		END IF;

		IF v_voucher.end_date IS NOT NULL AND v_now > v_voucher.end_date THEN
			RAISE EXCEPTION 'Voucher has expired';
		END IF;

		IF p_subtotal < COALESCE(v_voucher.min_order_value, 0) THEN
			RAISE EXCEPTION 'Order must be at least %', COALESCE(v_voucher.min_order_value, 0);
		END IF;

		IF v_voucher.usage_limit IS NOT NULL AND COALESCE(v_voucher.used_count, 0) >= v_voucher.usage_limit THEN
			RAISE EXCEPTION 'Voucher usage limit reached';
		END IF;

		IF EXISTS (
			SELECT 1 FROM public.user_voucher_usage uvu
			WHERE uvu.user_id = p_user_id
				AND uvu.voucher_id = v_voucher.id
		) THEN
			RAISE EXCEPTION 'Voucher already used by this account';
		END IF;

		IF v_voucher.discount_type = 'percent' THEN
			v_discount := p_subtotal * (v_voucher.discount_value / 100.0);
			IF v_voucher.max_discount_amount IS NOT NULL THEN
				v_discount := LEAST(v_discount, v_voucher.max_discount_amount);
			END IF;
		ELSE
			v_discount := v_voucher.discount_value;
		END IF;

		v_discount := GREATEST(v_discount, 0);
		v_total := GREATEST(p_subtotal - v_discount, 0);
		v_applied_code := v_voucher.code;
	END IF;

	INSERT INTO public.orders (
		user_id,
		order_code,
		receiver_name,
		phone,
		address,
		payment_method,
		note,
		subtotal,
		discount_amount,
		total_amount,
		status,
		order_type,
		voucher_id,
		applied_voucher_code,
		payment_status,
		payment_reference,
		payment_qr_url
	) VALUES (
		p_user_id,
		p_order_code,
		COALESCE(p_receiver_name, ''),
		COALESCE(p_phone, ''),
		COALESCE(p_address, ''),
		p_payment_method,
		COALESCE(p_note, ''),
		p_subtotal,
		v_discount,
		v_total,
		'pending',
		COALESCE(p_order_type, 'delivery'),
		v_voucher.id,
		v_applied_code,
		COALESCE(p_payment_status, 'pending'),
		p_payment_reference,
		p_payment_qr_url
	) RETURNING * INTO v_order;

	IF v_voucher.id IS NOT NULL THEN
		INSERT INTO public.user_voucher_usage (user_id, voucher_id, order_id)
		VALUES (p_user_id, v_voucher.id, v_order.id);

		UPDATE public.vouchers
		SET used_count = COALESCE(used_count, 0) + 1,
				updated_at = NOW()
		WHERE id = v_voucher.id;
	END IF;

	RETURN to_jsonb(v_order);
END;
$$;
