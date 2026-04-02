import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { SePayPgClient } from "npm:sepay-pg-node";
import { corsHeaders } from "../_shared/cors.ts";

type RequestBody = {
  user_id: string;
  order_code: string;
  subtotal: number;
  payment_mode?: "BANK_QR" | "SEPAY_CHECKOUT";
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
  return `FOODAPP ${orderCode}`.replace(/[^A-Z0-9\s-]/gi, "").trim().toUpperCase();
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

  let voucherId: string | null = null;
  let appliedVoucherCode: string | null = null;
  let discountAmount = 0;

  if (body.voucher_code && body.voucher_code.trim().length > 0) {
    const voucherCode = normalizeCode(body.voucher_code);

    const { data: voucher, error: voucherError } = await sb
      .from("vouchers")
      .select("id,code,discount_type,discount_value,max_discount_amount,min_order_value,is_active,is_public,start_date,end_date,usage_limit,used_count")
      .eq("code", voucherCode)
      .eq("is_active", true)
      .single<VoucherRow>();

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

    discountAmount = calculateDiscount(voucher, body.subtotal);
    voucherId = voucher.id;
    appliedVoucherCode = voucher.code;
  }

  const totalAmount = Math.max(0, body.subtotal - discountAmount);
  const receiverName = (body.receiver_name ?? "").trim();
  const phone = (body.phone ?? "").trim();
  const address = (body.address ?? "").trim();
  const paymentMode = body.payment_mode ?? "BANK_QR";

  const paymentContent = buildPaymentContent(body.order_code);

  const qrUrl = `https://img.vietqr.io/image/${bankCode}-${accountNo}-compact2.png?amount=${Math.round(totalAmount)}&addInfo=${encodeURIComponent(paymentContent)}&accountName=${encodeURIComponent(accountName)}`;

  let checkoutUrl: string | null = null;
  let checkoutFormFields: Record<string, string> | null = null;

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

  const { data: insertedOrder, error: orderError } = await sb
    .from("orders")
    .insert({
      user_id: body.user_id,
      order_code: body.order_code,
      receiver_name: receiverName,
      phone,
      address,
      payment_method: "bank_transfer",
      note: body.note ?? "QR Sepay",
      subtotal: body.subtotal,
      discount_amount: discountAmount,
      total_amount: totalAmount,
      status: "pending",
      voucher_id: voucherId,
      applied_voucher_code: appliedVoucherCode,
      payment_status: "pending",
      payment_reference: paymentContent,
      payment_qr_url: paymentMode === "BANK_QR" ? qrUrl : null,
    })
    .select("id,order_code,total_amount,payment_status,payment_qr_url,payment_reference")
    .single();

  if (orderError) {
    return internalError(orderError.message);
  }

  if (voucherId) {
    const { error: incError } = await sb.rpc("increment_voucher_usage", {
      p_voucher_id: voucherId,
    });

    if (incError) {
      return internalError(incError.message);
    }
  }

  return new Response(
    JSON.stringify({
      success: true,
      order: insertedOrder,
      payment: {
        provider: "sepay",
        mode: paymentMode,
        qr_url: qrUrl,
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
