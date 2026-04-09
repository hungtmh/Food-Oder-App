# send-push Edge Function

## Purpose
This function sends push notifications to Android devices using FCM and logs delivery status.

## Required secrets
Set these secrets before deploying:

- FCM_SERVICE_ACCOUNT_JSON (recommended)
  or
- FCM_SERVER_KEY (legacy fallback)

Note:
- Do NOT create secrets with `SUPABASE_` prefix in Dashboard.
- `SUPABASE_URL` and `SUPABASE_SERVICE_ROLE_KEY` are provided automatically by Supabase Edge Runtime.

Example:

```bash
supabase secrets set FCM_SERVER_KEY="YOUR_FCM_LEGACY_SERVER_KEY"

# Recommended for new Firebase projects (HTTP v1)
supabase secrets set FCM_SERVICE_ACCOUNT_JSON='{"type":"service_account",...}'
```

## Deploy

```bash
supabase functions deploy send-push
```

## Request payload

```json
{
  "user_id": "USER_UUID",
  "title": "Cap nhat don hang #A123",
  "body": "Don hang cua ban dang duoc che bien",
  "data": {
    "order_id": "ORDER_UUID",
    "order_code": "A123"
  },
  "notification_id": "OPTIONAL_NOTIFICATION_UUID"
}
```

## Notes
- The function reads active tokens from `device_tokens`.
- It writes send results into `notification_deliveries`.
- Invalid tokens are deactivated automatically.
- Supports both FCM legacy key and HTTP v1 service account.
