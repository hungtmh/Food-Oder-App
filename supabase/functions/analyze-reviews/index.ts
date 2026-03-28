import "@supabase/functions-js/edge-runtime.d.ts";
import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

// Token lấy từ biến môi trường của Hugging Face
const HF_TOKEN = Deno.env.get("HF_TOKEN");
// Sử dụng AI khổng lồ Qwen 2.5
const MODEL_ID = "Qwen/Qwen2.5-72B-Instruct";

type Sentiment = "positive" | "negative" | "neutral";

function normalizeSentiment(raw: unknown): Sentiment {
  const value = String(raw ?? "")
    .trim()
    .toLowerCase();
  if (["positive", "pos", "tích cực"].includes(value)) return "positive";
  if (["negative", "neg", "tiêu cực"].includes(value)) return "negative";
  if (["neutral", "neu", "trung tính"].includes(value)) return "neutral";
  return "neutral";
}

function normalizeScore(raw: unknown, sentiment: Sentiment): number {
  const parsed = Number(raw);
  if (Number.isFinite(parsed)) return Math.max(0, Math.min(1, parsed));
  return sentiment === "neutral" ? 0.5 : 0.8;
}

// Hàm bóc tách JSON
function extractJsonObject(rawText: string): Record<string, unknown> {
  const cleaned = rawText
    .replace(/```json/gi, "")
    .replace(/```/g, "")
    .trim();
  try {
    return JSON.parse(cleaned);
  } catch {
    const jsonMatch = cleaned.match(/\{[\s\S]*\}/);
    if (!jsonMatch) throw new Error("AI không trả về JSON hợp lệ");
    return JSON.parse(jsonMatch[0]);
  }
}

serve(async (req: Request) => {
  try {
    if (!HF_TOKEN) throw new Error("Thiếu HF_TOKEN trong môi trường Edge Function");

    const { review_id, content } = await req.json();
    if (!review_id) throw new Error("Thiếu review_id");

    console.log(`Đang phân tích review ID: ${review_id}`);

    // Gọi API Hugging Face qua cổng Chat Completions mới nhất
    const response = await fetch("https://router.huggingface.co/v1/chat/completions", {
      headers: {
        Authorization: `Bearer ${HF_TOKEN}`,
        "Content-Type": "application/json",
      },
      method: "POST",
      body: JSON.stringify({
        model: MODEL_ID,
        messages: [
          {
            role: "system",
            content: `Bạn là AI phân tích cảm xúc đánh giá món ăn. Hãy trả về CHỈ MỘT chuỗi JSON hợp lệ. Không markdown, không giải thích. Định dạng bắt buộc: {"sentiment": "positive"|"negative"|"neutral", "score": từ 0.0 đến 1.0}`,
          },
          {
            role: "user",
            content: content || "Không có nội dung",
          },
        ],
        temperature: 0.1,
        max_tokens: 100,
      }),
    });

    if (!response.ok) {
      const errText = await response.text();
      throw new Error(`Lỗi API: ${response.status} - ${errText}`);
    }

    const result = await response.json();
    const rawText = result.choices?.[0]?.message?.content || "{}";
    console.log("Hugging Face (Qwen) phản hồi:", rawText);

    const aiResult = extractJsonObject(rawText);
    const sentiment = normalizeSentiment(aiResult.sentiment);
    const score = normalizeScore(aiResult.score, sentiment);

    // Cập nhật Database
    const supabase = createClient(Deno.env.get("SUPABASE_URL") ?? "", Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "");

    const { error: updateError } = await supabase
      .from("reviews")
      .update({
        sentiment: sentiment,
        sentiment_score: score,
        is_analyzed: true,
      })
      .eq("id", review_id);

    if (updateError) throw updateError;

    console.log(`Cập nhật thành công! Nhãn: ${sentiment}, Điểm: ${score}`);

    return new Response(JSON.stringify({ status: "success", sentiment, score }), {
      headers: { "Content-Type": "application/json" },
    });
  } catch (error) {
    // ĐÂY LÀ ĐOẠN LÚC NÃY BẠN BỊ COPY THIẾU NÀY:
    const errorMessage = error instanceof Error ? error.message : String(error);
    console.error("LỖI CHI TIẾT:", errorMessage);

    const statusCode = errorMessage.includes("503") ? 503 : 500;

    return new Response(JSON.stringify({ error: errorMessage }), {
      status: statusCode,
      headers: { "Content-Type": "application/json" },
    });
  }
});
