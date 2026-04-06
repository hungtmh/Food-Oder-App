import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { SePayPgClient } from "npm:sepay-pg-node";
import { corsHeaders } from "../_shared/cors.ts";

type RequestBody = {
  user_id: string;
  order_code: string;
  subtotal: number;
  payment_mode?: "BANK_QR" | "SEPAY_CHECKOUT" | "COD";
  payment_method?: "cod" | "bank_transfer" | "momo" | "zalopay";
  receiver_name?: string;
  phone?: string;
  address?: string;
  note?: string;
  order_type?: "delivery" | "dine_in";
  voucher_code?: string;
  success_url?: string;
  error_url?: string;
  cancel_url?: string;
};

type VoucherRow = {
  id: string;
  code: string;
  discount_type: "percent" | "fixed_amount";
  discount_value: number;
  max_discount_amount: number | null;
  min_order_value: number;
  is_active: boolean;
  is_public: boolean;
  start_date: string | null;
  end_date: string | null;
  usage_limit: number | null;
  used_count: number | null;
};

function badRequest(message: string) {
  return new Response(JSON.stringify({ error: message }), {
    status: 400,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

function internalError(message: string) {
  return new Response(JSON.stringify({ error: message }), {
    status: 500,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

function normalizeCode(code: string) {
  return code.trim().toUpperCase();
}

function getRequiredEnv(name: string): string {
  const value = Deno.env.get(name)?.trim();
  if (!value) {
    throw new Error(`Missing required secret: ${name}`);
  }
  return value;
}

function buildPaymentContent(orderCode: string): string {
  // Sepay/Bank transfer content should be simple, uppercase and stable.
  return `FOODAPP ${orderCode}`
    .replace(/[^A-Z0-9\s-]/gi, "")
    .trim()
    .toUpperCase();
}

function calculateDiscount(voucher: VoucherRow, subtotal: number): number {
  if (voucher.discount_type === "percent") {
    const raw = (subtotal * voucher.discount_value) / 100;
    if (voucher.max_discount_amount != null) {
      return Math.min(raw, voucher.max_discount_amount);
    }
    return raw;
  }
  return voucher.discount_value;
}

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method Not Allowed" }), {
      status: 405,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }

  let supabaseUrl: string;
  let serviceRoleKey: string;
  let bankCode: string;
  let accountNo: string;
  let accountName: string;

  try {
    // Supabase runtime secrets are managed on dashboard (web):
    // Project Settings > Edge Functions > Secrets.
    supabaseUrl = getRequiredEnv("SUPABASE_URL");
    serviceRoleKey = getRequiredEnv("SUPABASE_SERVICE_ROLE_KEY");
    bankCode = getRequiredEnv("SEPAY_BANK_CODE");
    accountNo = getRequiredEnv("SEPAY_ACCOUNT_NO");
    accountName = getRequiredEnv("SEPAY_ACCOUNT_NAME");
  } catch (err) {
    const message = err instanceof Error ? err.message : "Missing required secrets";
    return internalError(message);
  }

  const sb = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false },
  });

  let body: RequestBody;
  try {
    body = await req.json();
  } catch {
    return badRequest("Invalid JSON body");
  }

  if (!body.user_id || !body.order_code || typeof body.subtotal !== "number") {
    return badRequest("user_id, order_code, subtotal are required");
  }

  if (body.subtotal <= 0) {
    return badRequest("subtotal must be greater than 0");
  }

  let voucherCodeToApply: string | null = null;
  let totalAmount = body.subtotal;

  if (body.voucher_code && body.voucher_code.trim().length > 0) {
    const voucherCode = normalizeCode(body.voucher_code);

    const { data: voucher, error: voucherError } = await sb.from("vouchers").select("id,code,discount_type,discount_value,max_discount_amount,min_order_value,is_active,is_public,start_date,end_date,usage_limit,used_count").eq("code", voucherCode).eq("is_active", true).single<VoucherRow>();

    if (voucherError || !voucher) {
      return badRequest("Voucher is invalid or inactive");
    }

    const now = new Date();
    const startDate = voucher.start_date ? new Date(voucher.start_date) : null;
    const endDate = voucher.end_date ? new Date(voucher.end_date) : null;

    if (startDate && now < startDate) {
      return badRequest("Voucher is not active yet");
    }

    if (endDate && now > endDate) {
      return badRequest("Voucher has expired");
    }

    if (body.subtotal < voucher.min_order_value) {
      return badRequest(`Order must be at least ${voucher.min_order_value}`);
    }

    if (voucher.usage_limit != null) {
      const used = voucher.used_count ?? 0;
      if (used >= voucher.usage_limit) {
        return badRequest("Voucher usage limit reached");
      }
    }

    voucherCodeToApply = voucher.code;
    totalAmount = Math.max(0, body.subtotal - calculateDiscount(voucher, body.subtotal));
  }

  const receiverName = (body.receiver_name ?? "").trim();
  const phone = (body.phone ?? "").trim();
  const address = (body.address ?? "").trim();
  const paymentMode = body.payment_mode ?? "BANK_QR";
  const paymentMethod = body.payment_method ?? (paymentMode === "COD" ? "cod" : "bank_transfer");

  const paymentContent = paymentMode === "COD" ? null : buildPaymentContent(body.order_code);
  let qrUrl: string | null = null;

  let checkoutUrl: string | null = null;
  let checkoutFormFields: Record<string, string> | null = null;

  if (paymentMode === "BANK_QR" || paymentMode === "SEPAY_CHECKOUT") {
    qrUrl = `https://img.vietqr.io/image/${bankCode}-${accountNo}-compact2.png?amount=${Math.round(totalAmount)}&addInfo=${encodeURIComponent(paymentContent ?? "")}&accountName=${encodeURIComponent(accountName)}`;
  }

  if (paymentMode === "SEPAY_CHECKOUT") {
    if (!body.success_url || !body.error_url || !body.cancel_url) {
      return badRequest("success_url, error_url, cancel_url are required for SEPAY_CHECKOUT");
    }

    let sepayEnv: string;
    let merchantId: string;
    let secretKey: string;

    try {
      sepayEnv = getRequiredEnv("SEPAY_ENV");
      merchantId = getRequiredEnv("SEPAY_MERCHANT_ID");
      secretKey = getRequiredEnv("SEPAY_MERCHANT_SECRET_KEY");
    } catch (err) {
      const message = err instanceof Error ? err.message : "Missing SePay checkout secrets";
      return internalError(message);
    }

    const client = new SePayPgClient({
      env: sepayEnv,
      merchant_id: merchantId,
      secret_key: secretKey,
    });

    checkoutUrl = client.checkout.initCheckoutUrl();
    checkoutFormFields = client.checkout.initOneTimePaymentFields({
      payment_method: "BANK_TRANSFER",
      order_invoice_number: body.order_code,
      order_amount: Math.round(totalAmount),
      currency: "VND",
      order_description: body.note?.trim() || `Thanh toan don hang ${body.order_code}`,
      success_url: body.success_url,
      error_url: body.error_url,
      cancel_url: body.cancel_url,
    });
  }

  const { data: insertedOrder, error: orderError } = await sb.rpc("create_order_with_voucher_tx", {
    p_user_id: body.user_id,
    p_order_code: body.order_code,
    p_receiver_name: receiverName,
    p_phone: phone,
    p_address: address,
    p_payment_method: paymentMethod,
    p_note: body.note ?? (paymentMode === "COD" ? "COD" : "QR Sepay"),
    p_subtotal: body.subtotal,
    p_order_type: body.order_type ?? "delivery",
    p_payment_status: "pending",
    p_payment_reference: paymentContent,
    p_payment_qr_url: paymentMode === "BANK_QR" ? qrUrl : null,
    p_voucher_code: voucherCodeToApply,
  });

  if (orderError) {
    return badRequest(orderError.message);
  }

  if (!insertedOrder || typeof insertedOrder !== "object") {
    return internalError("Cannot create order");
  }

  const orderData = insertedOrder as Record<string, unknown>;
  const parsedTotal = Number(orderData.total_amount ?? body.subtotal);
  totalAmount = Number.isFinite(parsedTotal) ? parsedTotal : body.subtotal;

  if (paymentMode === "BANK_QR") {
    qrUrl = `https://img.vietqr.io/image/${bankCode}-${accountNo}-compact2.png?amount=${Math.round(totalAmount)}&addInfo=${encodeURIComponent(paymentContent ?? "")}&accountName=${encodeURIComponent(accountName)}`;
  }

  return new Response(
    JSON.stringify({
      success: true,
      order: orderData,
      payment: {
        provider: "sepay",
        mode: paymentMode,
        qr_url: paymentMode === "BANK_QR" ? qrUrl : null,
        checkout_url: checkoutUrl,
        checkout_form_fields: checkoutFormFields,
        transfer_content: paymentContent,
        amount: Math.round(totalAmount),
      },
    }),
    {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    },
  );
});
