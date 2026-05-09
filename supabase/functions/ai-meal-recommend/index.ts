import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

declare const EdgeRuntime: {
  waitUntil(promise: Promise<unknown>): void;
};

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
};

const DEEPSEEK_API_KEY = Deno.env.get("DEEPSEEK_API_KEY");
const DEEPSEEK_BASE = "https://api.deepseek.com/v1";

interface FoodItem {
  id: string;
  name: string;
  description: string;
  price: number;
  discount_percent: number;
  category_id: string;
  avg_rating: number;
  total_reviews: number;
}

interface AiRequest {
  budget: number;
  peopleCount: number;
  taste: string;
  description: string;
  userId: string;
}

interface AiRecommendedFood {
  food_id: string;
  food_name: string;
  price: number;
  reason: string;
}

interface AiResult {
  rationale: string;
  foods: AiRecommendedFood[];
}

function extractJson(text: string): any {
  const cleaned = text
    .replace(/```json/gi, "")
    .replace(/```/g, "")
    .trim();
  try {
    return JSON.parse(cleaned);
  } catch {
    const match = cleaned.match(/\{[\s\S]*\}/);
    if (!match) throw new Error("AI did not return valid JSON");
    return JSON.parse(match[0]);
  }
}

function buildFoodsList(foods: FoodItem[]): string {
  return foods.map((f) => {
    const discounted = f.price * (100 - f.discount_percent) / 100;
    return `- ID: ${f.id} | ${f.name} | ${discounted.toFixed(0)} VND | Rating: ${f.avg_rating}/5 (${f.total_reviews} reviews) | ${f.description?.slice(0, 80) || ""}`;
  }).join("\n");
}

async function processRecommendation(
  supabaseAdmin: any,
  taskId: string,
  userId: string,
  budget: number,
  peopleCount: number,
  taste: string,
  description: string,
  foods: FoodItem[]
) {
  try {
    console.log("Processing task:", taskId);

    await supabaseAdmin.from("ai_recommendation_tasks").update({
      status: "processing",
      updated_at: new Date().toISOString(),
    }).eq("id", taskId);

    const foodsList = buildFoodsList(foods);

    const prompt = `Ban la tro ly goi y mon an cho mot ung dung dat do an. Nhiem vu cua ban la de xuat mot combo mon an dua tren yeu cau cua nguoi dung.

YEU CAU:
- Ngan sach: ${budget || "Khong gioi han"} VND
- So nguoi an: ${peopleCount || 1} nguoi
- Khau vi: ${taste || "Khong co yeu cau dac biet"}
- Mo ta them: ${description || "Khong co"}

DANH SACH MON AN CO SAN:
${foodsList}

Hay phan tich va chon ra TOI DA 5 mon an phu hop nhat tao thanh combo. Tong gia khong vuot qua ngan sach (neu co gioi han). Co the chon it hon 5 mon neu phu hop.

TRA LOI CHI BANG MOT JSON OBJECT, khong them bat ky van ban nao khac, theo dinh dang:
{
  "rationale": "Giai thich ngan gon bang tieng Viet tai sao chon combo nay",
  "foods": [
    {
      "food_id": "UUID cua mon",
      "food_name": "Ten mon",
      "price": gia_sau_khi_giam,
      "reason": "Ly do chon mon nay (1 cau ngan)"
    }
  ]
}

LUU Y QUAN TRONG:
1. food_id PHAI la ID chinh xac tu danh sach tren
2. Chon TOI DA 5 mon, da dang, tranh trung lap cung loai
3. Uu tien mon co rating cao, nhieu review
4. Neu ngan sach co gioi han, tong gia combo phai <= ngan sach
5. Giai thich rationale ngan gon, huu ich bang tieng Viet`;

    console.log("Calling DeepSeek API...");

    const deepseekRes = await fetch(`${DEEPSEEK_BASE}/chat/completions`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${DEEPSEEK_API_KEY}`,
      },
      body: JSON.stringify({
        model: "deepseek-chat",
        messages: [
          { role: "system", content: "Ban la tro ly goi y mon an chuyen nghiep. Chi tra loi bang JSON." },
          { role: "user", content: prompt },
        ],
        temperature: 0.7,
        max_tokens: 2048,
      }),
    });

    if (!deepseekRes.ok) {
      const errText = await deepseekRes.text();
      console.error("DeepSeek API error:", deepseekRes.status, errText);

      await supabaseAdmin.from("ai_recommendation_tasks").update({
        status: "failed",
        error_message: `DeepSeek error ${deepseekRes.status}: ${errText.slice(0, 300)}`,
        updated_at: new Date().toISOString(),
      }).eq("id", taskId);
      return;
    }

    const deepseekData = await deepseekRes.json();
    const aiText = deepseekData.choices?.[0]?.message?.content;

    if (!aiText) {
      await supabaseAdmin.from("ai_recommendation_tasks").update({
        status: "failed",
        error_message: "DeepSeek returned empty response",
        updated_at: new Date().toISOString(),
      }).eq("id", taskId);
      return;
    }

    console.log("DeepSeek response:", aiText);

    let parsed: AiResult;
    try {
      parsed = extractJson(aiText) as AiResult;
      if (!parsed.rationale || !Array.isArray(parsed.foods) || parsed.foods.length === 0) {
        throw new Error("Invalid response structure");
      }
    } catch (parseErr) {
      await supabaseAdmin.from("ai_recommendation_tasks").update({
        status: "failed",
        error_message: `Parse error: ${parseErr.message}`,
        updated_at: new Date().toISOString(),
      }).eq("id", taskId);
      return;
    }

    const validFoods = parsed.foods
      .filter((f) => f.food_id && f.food_name)
      .slice(0, 5);

    await supabaseAdmin.from("ai_recommendation_tasks").update({
      status: "completed",
      result: { rationale: parsed.rationale, foods: validFoods },
      updated_at: new Date().toISOString(),
    }).eq("id", taskId);

    console.log("Task completed:", taskId, "| Foods:", validFoods.length);
  } catch (err) {
    console.error("Processing error:", err);
    await supabaseAdmin.from("ai_recommendation_tasks").update({
      status: "failed",
      error_message: `Unexpected error: ${err.message}`,
      updated_at: new Date().toISOString(),
    }).eq("id", taskId);
  }
}

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    if (!DEEPSEEK_API_KEY) {
      return new Response(
        JSON.stringify({ error: "DEEPSEEK_API_KEY not configured" }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const body: AiRequest = await req.json();
    const { budget, peopleCount, taste, description, userId } = body;

    if (!userId) {
      return new Response(
        JSON.stringify({ error: "userId is required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabaseAdmin = createClient(
      Deno.env.get("SUPABASE_URL") ?? "",
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? ""
    );

    const { data: foods, error: foodsError } = await supabaseAdmin
      .from("foods")
      .select("id,name,description,price,discount_percent,category_id,avg_rating,total_reviews")
      .eq("is_available", true)
      .order("total_reviews", { ascending: false })
      .limit(50);

    if (foodsError) {
      return new Response(
        JSON.stringify({ error: "Failed to fetch foods", detail: foodsError.message }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const taskInsert = await supabaseAdmin
      .from("ai_recommendation_tasks")
      .insert({
        user_id: userId,
        status: "pending",
        budget: budget || 0,
        people_count: peopleCount || 1,
        taste: taste || "",
        description: description || "",
      })
      .select("id")
      .single();

    if (taskInsert.error) {
      return new Response(
        JSON.stringify({ error: "Failed to create task", detail: taskInsert.error.message }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const taskId = taskInsert.data.id;
    console.log("Task created:", taskId);

    EdgeRuntime.waitUntil(
      processRecommendation(supabaseAdmin, taskId, userId, budget, peopleCount, taste, description, foods as FoodItem[])
    );

    return new Response(
      JSON.stringify({ taskId, status: "pending" }),
      { headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  } catch (err) {
    console.error("Request error:", err);
    return new Response(
      JSON.stringify({ error: "Internal server error", detail: err.message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
