-- Push notification support tables
-- Run this script in Supabase SQL editor

-- ============================================
-- 1) DEVICE TOKENS
-- ============================================
CREATE TABLE IF NOT EXISTS public.device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    fcm_token TEXT NOT NULL,
    platform TEXT NOT NULL DEFAULT 'android',
    device_name TEXT DEFAULT '',
    app_version TEXT DEFAULT '',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_device_tokens_fcm_token ON public.device_tokens(fcm_token);
CREATE INDEX IF NOT EXISTS idx_device_tokens_user_id ON public.device_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_device_tokens_active ON public.device_tokens(is_active);

-- ============================================
-- 2) NOTIFICATION DELIVERIES (optional but useful)
-- ============================================
CREATE TABLE IF NOT EXISTS public.notification_deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notification_id UUID REFERENCES public.notifications(id) ON DELETE SET NULL,
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    fcm_token TEXT,
    provider TEXT NOT NULL DEFAULT 'fcm',
    status TEXT NOT NULL DEFAULT 'queued',
    provider_message_id TEXT,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notification_deliveries_user_id ON public.notification_deliveries(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_deliveries_notification_id ON public.notification_deliveries(notification_id);
CREATE INDEX IF NOT EXISTS idx_notification_deliveries_status ON public.notification_deliveries(status);

-- ============================================
-- 3) updated_at trigger for device_tokens
-- ============================================
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_device_tokens_updated_at ON public.device_tokens;
CREATE TRIGGER update_device_tokens_updated_at
BEFORE UPDATE ON public.device_tokens
FOR EACH ROW EXECUTE FUNCTION public.update_updated_at_column();

-- ============================================
-- 4) RLS (custom auth mode -> allow all)
-- ============================================
ALTER TABLE public.device_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notification_deliveries ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS device_tokens_all_access ON public.device_tokens;
CREATE POLICY device_tokens_all_access
ON public.device_tokens
FOR ALL
USING (true)
WITH CHECK (true);

DROP POLICY IF EXISTS notification_deliveries_all_access ON public.notification_deliveries;
CREATE POLICY notification_deliveries_all_access
ON public.notification_deliveries
FOR ALL
USING (true)
WITH CHECK (true);
