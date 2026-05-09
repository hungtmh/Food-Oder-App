-- ============================================
-- AI RECOMMENDATION TASKS TABLE
-- for "Gợi ý combo món ăn" feature using DeepSeek API
-- ============================================
CREATE TABLE IF NOT EXISTS public.ai_recommendation_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES public.users(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending','processing','completed','failed')),
    budget DOUBLE PRECISION,
    people_count INTEGER,
    taste TEXT DEFAULT '',
    description TEXT DEFAULT '',
    result JSONB,
    error_message TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for user queries
CREATE INDEX IF NOT EXISTS idx_ai_tasks_user_id ON public.ai_recommendation_tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_tasks_status ON public.ai_recommendation_tasks(status);
