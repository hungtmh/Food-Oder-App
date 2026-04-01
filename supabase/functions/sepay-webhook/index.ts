import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { corsHeaders } from "../_shared/cors.ts";

type JsonObject = Record<string, unknown>;

function jsonResponse(status: number, payload: JsonObject) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

function getRequiredEnv(name: string): string {
  const value = Deno.env.get(name)?.trim();
  if (!value) {
    throw new Error(`Missing required secret: ${name}`);
  }
  return value;
}

function normalizeRef(input: string): string {
  return input.replace(/\s+/g, " ").trim().toUpperCase();
}

function canonicalizeOrderCodeToken(input: string): string | null {
  const compact = input.toUpperCase().replace(/[^A-Z0-9]/g, "");
  if (!compact.startsWith("ORD") || compact.length <= 3) {
    return null;
  }
  return `ORD-${compact.slice(3)}`;
}

function extractOrderCodeToken(input: string): string | null {
  const normalized = normalizeRef(input);
  if (!normalized) {
    return null;
  }

  const foodAppMatch = normalized.match(/FOODAPP\s+(ORD[A-Z0-9_-]*)\b/);
  if (foodAppMatch?.[1]) {
    return canonicalizeOrderCodeToken(foodAppMatch[1]);
  }

  const orderCodeMatch = normalized.match(/\b(ORD[A-Z0-9_-]*)\b/);
  if (orderCodeMatch?.[1]) {
    return canonicalizeOrderCodeToken(orderCodeMatch[1]);
  }

  return null;
}

function extractReference(payload: JsonObject): string | null {
  const rawCandidates = [
    payload.transfer_content,
    payload.transferContent,
    payload.payment_content,
    payload.paymentContent,
    payload.transaction_content,
    payload.transactionContent,
    payload.content,
    payload.description,
    payload.order_invoice_number,
    payload.orderInvoiceNumber,
    payload.order_code,
    payload.orderCode,
  ].filter((v) => typeof v === "string") as string[];

  if (rawCandidates.length === 0) {
    return null;
  }

  for (const candidate of rawCandidates) {
    const normalized = normalizeRef(candidate);
    if (!normalized) {
      continue;
    }

    // Accept noisy transfer content and extract order code token (ORD...).
    const orderCode = extractOrderCodeToken(normalized);
    if (orderCode) {
      return `FOODAPP ${orderCode}`;
    }

    // Fallback to exact normalized content if provider sends only full reference.
    return normalized;
  }

  return null;
}

function extractPaidAmount(payload: JsonObject): number | null {
  const candidates = [
    payload.amount,
    payload.transfer_amount,
    payload.transferAmount,
    payload.paid_amount,
    payload.paidAmount,
  ];
  for (const value of candidates) {
    if (typeof value === "number" && Number.isFinite(value)) {
      return value;
    }
    if (typeof value === "string") {
      const parsed = Number(value.replace(/[^\d.-]/g, "").trim());
      if (Number.isFinite(parsed)) {
        return parsed;
      }
    }
  }
  return null;
}

function toMoneyInt(value: unknown): number | null {
  if (typeof value === "number" && Number.isFinite(value)) {
    return Math.round(value);
  }
  if (typeof value === "string") {
    const parsed = Number(value.replace(/[^\d.-]/g, "").trim());
    if (Number.isFinite(parsed)) {
      return Math.round(parsed);
    }
  }
  return null;
}

function isSuccessPayload(payload: JsonObject): boolean {
  const successFields = [
    payload.status,
    payload.payment_status,
    payload.paymentStatus,
    payload.transaction_status,
    payload.transactionStatus,
  ]
    .filter((v) => typeof v === "string")
    .map((v) => (v as string).trim().toLowerCase());

  const booleanSuccessFields = [payload.success, payload.is_success, payload.isSuccess, payload.paid]
    .filter((v) => typeof v === "boolean") as boolean[];

  if (booleanSuccessFields.includes(true)) {
    return true;
  }

  if (successFields.some((v) => ["failed", "cancelled", "canceled", "error", "expired", "refunded"].includes(v))) {
    return false;
  }

  if (successFields.some((v) => ["success", "paid", "completed", "done", "succeeded", "00"].includes(v))) {
    return true;
  }

  if (successFields.length === 0) {
    // Some SePay transfer callbacks only include content/amount and no status field.
    return true;
  }

  return false;
}

function extractAuthorizationApiKey(req: Request): string | null {
  const authHeader = req.headers.get("Authorization") ?? req.headers.get("authorization");
  if (!authHeader) {
    return null;
  }

  const match = authHeader.match(/^\s*Apikey\s+(.+)\s*$/i);
  if (!match || !match[1]) {
    return null;
  }

  return match[1].trim();
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return jsonResponse(405, { error: "Method Not Allowed" });
  }

  let supabaseUrl: string;
  let serviceRoleKey: string;
  let webhookSecret: string;

  try {
    supabaseUrl = getRequiredEnv("SUPABASE_URL");
    serviceRoleKey = getRequiredEnv("SUPABASE_SERVICE_ROLE_KEY");
    webhookSecret = getRequiredEnv("SEPAY_WEBHOOK_SECRET");
  } catch (err) {
    const message = err instanceof Error ? err.message : "Missing required secrets";
    return jsonResponse(500, { error: message });
  }

  const providedFromAuthorization = extractAuthorizationApiKey(req);
  const providedFromLegacyHeader = req.headers.get("x-sepay-webhook-secret")?.trim() ?? "";
  const providedSecret = providedFromAuthorization ?? providedFromLegacyHeader;

  if (providedSecret !== webhookSecret) {
    return jsonResponse(401, { error: "Invalid webhook secret" });
  }

  let payload: JsonObject;
  try {
    payload = await req.json();
  } catch {
    return jsonResponse(400, { error: "Invalid JSON body" });
  }

  if (!isSuccessPayload(payload)) {
    return jsonResponse(200, { ok: true, ignored: true, reason: "non-success status" });
  }

  const paymentReference = extractReference(payload);
  if (!paymentReference) {
    return jsonResponse(400, { error: "Cannot extract payment reference" });
  }

  const paidAmount = extractPaidAmount(payload);
  if (paidAmount == null) {
    return jsonResponse(400, { error: "Missing paid amount in webhook payload" });
  }

  const sb = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false },
  });

  const { data: order, error: orderErr } = await sb
    .from("orders")
    .select("id,total_amount,payment_status,payment_reference")
    .eq("payment_reference", paymentReference)
    .limit(1)
    .maybeSingle();

  if (orderErr) {
    return jsonResponse(500, { error: orderErr.message });
  }

  if (!order) {
    return jsonResponse(404, { error: "Order not found for payment reference", payment_reference: paymentReference });
  }

  // Idempotent behavior: already paid callback should return success without updating again.
  if (order.payment_status === "paid") {
    return jsonResponse(200, { ok: true, already_paid: true, order_id: order.id });
  }

  const expectedAmount = toMoneyInt(order.total_amount);
  const actualAmount = toMoneyInt(paidAmount);

  if (expectedAmount == null || actualAmount == null) {
    return jsonResponse(500, {
      error: "Cannot normalize amount for verification",
      paid_amount: paidAmount,
      expected_amount: order.total_amount,
    });
  }

  if (actualAmount !== expectedAmount) {
    return jsonResponse(400, {
      error: "Paid amount does not exactly match order total",
      paid_amount: actualAmount,
      expected_amount: expectedAmount,
    });
  }

  const { error: updateErr } = await sb
    .from("orders")
    .update({
      payment_status: "paid",
      paid_at: new Date().toISOString(),
      status: "processing",
    })
    .eq("id", order.id);

  if (updateErr) {
    return jsonResponse(500, { error: updateErr.message });
  }

  return jsonResponse(200, {
    ok: true,
    order_id: order.id,
    payment_reference: paymentReference,
    paid_amount: paidAmount,
  });
});
