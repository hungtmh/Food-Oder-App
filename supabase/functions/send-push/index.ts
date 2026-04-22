/// <reference path="./globals.d.ts" />
// deno-lint-ignore-file no-explicit-any
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

type PushRequest = {
  user_id: string;
  title: string;
  body: string;
  data?: Record<string, string>;
  notification_id?: string;
};

type FcmSendResult = {
  ok: boolean;
  messageId: string | null;
  error: string | null;
  invalidToken: boolean;
};

type FcmServiceAccount = {
  project_id: string;
  client_email: string;
  private_key: string;
  token_uri?: string;
};

let cachedAccessToken: { token: string; exp: number } | null = null;

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    if (req.method !== "POST") {
      return json({ error: "Method not allowed" }, 405);
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    const fcmServerKey = Deno.env.get("FCM_SERVER_KEY");
    const fcmServiceAccountJson = Deno.env.get("FCM_SERVICE_ACCOUNT_JSON");

    if (!supabaseUrl || !serviceRoleKey) {
      return json({ error: "Missing Supabase runtime envs. Deploy on Supabase Edge Runtime." }, 500);
    }

    if (!fcmServerKey && !fcmServiceAccountJson) {
      return json({
        error:
          "Missing FCM credential. Set either FCM_SERVER_KEY (legacy) or FCM_SERVICE_ACCOUNT_JSON (recommended).",
      }, 500);
    }

    const rawPayload = await req.json();
    
    // Support Postgres Webhook payload format
    let payload: PushRequest;
    if (rawPayload.type === "INSERT" && rawPayload.table === "notifications" && rawPayload.record) {
      payload = {
        user_id: rawPayload.record.user_id,
        title: rawPayload.record.title,
        body: rawPayload.record.message,
        notification_id: rawPayload.record.id
      };
    } else {
      payload = rawPayload as PushRequest;
    }

    if (!payload?.user_id || !payload?.title || !payload?.body) {
      return json({ error: "user_id, title, body are required" }, 400);
    }

    const supabase = createClient(supabaseUrl, serviceRoleKey, {
      auth: { persistSession: false },
    });

    let notificationId = payload.notification_id ?? null;
    let inboxInsertError: string | null = null;
    let inboxInsertWarning: string | null = null;

    // If caller does not provide notification_id, write a row to in-app inbox table.
    if (!notificationId) {
      const notificationPayload: Record<string, unknown> = {
        user_id: payload.user_id,
        title: payload.title,
        message: payload.body,
        is_read: false,
      };

      if (payload.data?.order_id) {
        if (isUuid(payload.data.order_id)) {
          notificationPayload.order_id = payload.data.order_id;
        } else {
          inboxInsertWarning = "order_id in payload is not UUID, skipped order_id field for inbox insert";
        }
      }
      if (payload.data?.order_code) {
        notificationPayload.order_code = payload.data.order_code;
      }

      const { data: createdNotifications, error: createNotificationError } = await supabase
        .from("notifications")
        .insert(notificationPayload)
        .select("id")
        .limit(1);

      if (createNotificationError) {
        inboxInsertError = createNotificationError.message;
      } else {
        notificationId = createdNotifications?.[0]?.id ?? null;
      }
    }

    let tokensResult;
    if (payload.user_id === "all") {
      tokensResult = await supabase
        .from("device_tokens")
        .select("id, fcm_token")
        .eq("is_active", true);
    } else {
      tokensResult = await supabase
        .from("device_tokens")
        .select("id, fcm_token")
        .eq("user_id", payload.user_id)
        .eq("is_active", true);
    }
    
    const { data: tokens, error: tokenError } = tokensResult;

    if (tokenError) {
      return json({ error: tokenError.message }, 500);
    }

    if (!tokens || tokens.length === 0) {
      return json({ success: true, sent: 0, failed: 0, reason: "No active tokens" });
    }

    let sent = 0;
    let failed = 0;

    let serviceAccount: FcmServiceAccount | null = null;
    if (fcmServiceAccountJson) {
      try {
        serviceAccount = JSON.parse(fcmServiceAccountJson) as FcmServiceAccount;
      } catch {
        return json({ error: "FCM_SERVICE_ACCOUNT_JSON is not valid JSON" }, 500);
      }
    }

    for (const tokenRow of tokens) {
      const fcmResult = await sendToFcm(
        tokenRow.fcm_token,
        payload.title,
        payload.body,
        payload.data ?? {},
        fcmServerKey,
        serviceAccount,
      );

      const status = fcmResult.ok ? "sent" : "failed";
      const errorMessage = fcmResult.error ?? "";

      if (fcmResult.ok) {
        sent += 1;
      } else {
        failed += 1;
        if (fcmResult.invalidToken) {
          await supabase
            .from("device_tokens")
            .update({ is_active: false })
            .eq("id", tokenRow.id);
        }
      }

      await supabase.from("notification_deliveries").insert({
        notification_id: notificationId,
        user_id: payload.user_id,
        fcm_token: tokenRow.fcm_token,
        provider: "fcm",
        status,
        provider_message_id: fcmResult.messageId,
        error_message: errorMessage || null,
      });
    }

    return json({
      success: true,
      sent,
      failed,
      notification_id: notificationId,
      inbox_insert_error: inboxInsertError,
      inbox_insert_warning: inboxInsertWarning,
    });
  } catch (e: any) {
    return json({ error: e?.message ?? "Unhandled error" }, 500);
  }
});

function isUuid(value: string): boolean {
  return /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$/.test(value);
}

async function sendToFcm(
  token: string,
  title: string,
  body: string,
  data: Record<string, string>,
  fcmServerKey: string | undefined,
  serviceAccount: FcmServiceAccount | null,
): Promise<FcmSendResult> {
  if (serviceAccount?.project_id && serviceAccount.client_email && serviceAccount.private_key) {
    return sendToFcmHttpV1(token, title, body, data, serviceAccount);
  }

  if (fcmServerKey) {
    return sendToFcmLegacy(token, title, body, data, fcmServerKey);
  }

  return {
    ok: false,
    messageId: null,
    error: "No usable FCM credential configured",
    invalidToken: false,
  };
}

async function sendToFcmLegacy(
  token: string,
  title: string,
  body: string,
  data: Record<string, string>,
  serverKey: string,
): Promise<FcmSendResult> {
  const fcmBody = {
    to: token,
    notification: { title, body },
    data,
    priority: "high",
  };

  const response = await fetch("https://fcm.googleapis.com/fcm/send", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `key=${serverKey}`,
    },
    body: JSON.stringify(fcmBody),
  });

  const result = await safeJson(response);
  const firstResult = result?.results?.[0] ?? null;
  const error = firstResult?.error ?? null;

  if (response.ok && result?.success === 1) {
    return {
      ok: true,
      messageId: firstResult?.message_id ?? null,
      error: null,
      invalidToken: false,
    };
  }

  return {
    ok: false,
    messageId: null,
    error: error ?? "FCM legacy request failed",
    invalidToken: error === "NotRegistered" || error === "InvalidRegistration",
  };
}

async function sendToFcmHttpV1(
  token: string,
  title: string,
  body: string,
  data: Record<string, string>,
  serviceAccount: FcmServiceAccount,
): Promise<FcmSendResult> {
  const accessToken = await getGoogleAccessToken(serviceAccount);

  const endpoint = `https://fcm.googleapis.com/v1/projects/${serviceAccount.project_id}/messages:send`;
  const requestBody = {
    message: {
      token,
      notification: { title, body },
      data,
    },
  };

  const response = await fetch(endpoint, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify(requestBody),
  });

  const result = await safeJson(response);
  if (response.ok && result?.name) {
    return {
      ok: true,
      messageId: result.name,
      error: null,
      invalidToken: false,
    };
  }

  const errorMessage = result?.error?.message ?? `FCM v1 HTTP ${response.status}`;
  const invalidToken =
    errorMessage.includes("UNREGISTERED") ||
    (errorMessage.includes("INVALID_ARGUMENT") && errorMessage.includes("token"));

  return {
    ok: false,
    messageId: null,
    error: errorMessage,
    invalidToken,
  };
}

async function getGoogleAccessToken(serviceAccount: FcmServiceAccount): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  if (cachedAccessToken && cachedAccessToken.exp > now + 60) {
    return cachedAccessToken.token;
  }

  const tokenUri = serviceAccount.token_uri ?? "https://oauth2.googleapis.com/token";
  const assertion = await createSignedJwt(serviceAccount.client_email, serviceAccount.private_key, tokenUri);

  const form = new URLSearchParams();
  form.set("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
  form.set("assertion", assertion);

  const response = await fetch(tokenUri, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: form.toString(),
  });

  const result = await safeJson(response);
  if (!response.ok || !result?.access_token) {
    throw new Error(`Failed to get Google access token: ${result?.error_description ?? response.status}`);
  }

  cachedAccessToken = {
    token: result.access_token,
    exp: now + Number(result.expires_in ?? 3600),
  };

  return cachedAccessToken.token;
}

async function createSignedJwt(clientEmail: string, privateKeyPem: string, audience: string): Promise<string> {
  const now = Math.floor(Date.now() / 1000);
  const header = { alg: "RS256", typ: "JWT" };
  const payload = {
    iss: clientEmail,
    scope: "https://www.googleapis.com/auth/firebase.messaging",
    aud: audience,
    iat: now,
    exp: now + 3600,
  };

  const encodedHeader = base64UrlEncodeJson(header);
  const encodedPayload = base64UrlEncodeJson(payload);
  const unsignedToken = `${encodedHeader}.${encodedPayload}`;

  const cryptoKey = await importPrivateKey(privateKeyPem);
  const signatureBuffer = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    cryptoKey,
    new TextEncoder().encode(unsignedToken),
  );

  const signature = base64UrlEncodeBytes(new Uint8Array(signatureBuffer));
  return `${unsignedToken}.${signature}`;
}

async function importPrivateKey(privateKeyPem: string): Promise<CryptoKey> {
  const pemContents = privateKeyPem
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\s+/g, "");

  const binary = atob(pemContents);
  const bytes = Uint8Array.from(binary, (c) => c.charCodeAt(0));

  return crypto.subtle.importKey(
    "pkcs8",
    bytes.buffer,
    {
      name: "RSASSA-PKCS1-v1_5",
      hash: "SHA-256",
    },
    false,
    ["sign"],
  );
}

function base64UrlEncodeJson(value: unknown): string {
  const text = JSON.stringify(value);
  return base64UrlEncodeBytes(new TextEncoder().encode(text));
}

function base64UrlEncodeBytes(bytes: Uint8Array): string {
  const binary = String.fromCharCode(...bytes);
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

async function safeJson(response: Response): Promise<any> {
  try {
    return await response.json();
  } catch {
    return null;
  }
}

function json(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });
}
