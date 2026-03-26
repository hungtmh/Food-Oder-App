# create-sepay-payment

Edge Function tao don thanh toan chuyen khoan QR (Sepay/VietQR) co kiem tra voucher.

## 1) Secrets can thiet

```bash
supabase secrets set \
  SEPAY_BANK_CODE=MB \
  SEPAY_ACCOUNT_NO=0123456789 \
  SEPAY_ACCOUNT_NAME="FOOD ORDER APP" \\
  SEPAY_ENV=sandbox \\
  SEPAY_MERCHANT_ID="YOUR_MERCHANT_ID" \\
  SEPAY_MERCHANT_SECRET_KEY="YOUR_MERCHANT_SECRET_KEY"
```

Bat buoc co san trong Supabase runtime:
- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`

## 2) Deploy function

```bash
supabase functions deploy create-sepay-payment
```

## 3) SQL can chay truoc

Chay file `supabase_schema.sql` sau khi da cap nhat:
- co cot `payment_status`, `payment_reference`, `payment_qr_url`, `paid_at` trong `orders`
- co ham `increment_voucher_usage`
- co cot `is_public` trong `vouchers`

## 4) Goi tu app Android (Retrofit)

POST `${SUPABASE_URL}/functions/v1/create-sepay-payment`

Body mau:

```json
{
  "user_id": "uuid-user",
  "order_code": "ORD-ABCD1234",
  "subtotal": 250000,
  "payment_mode": "BANK_QR",
  "receiver_name": "Nguyen Van A",
  "phone": "0900000000",
  "address": "123 ABC",
  "note": "Giao truoc 11h",
  "voucher_code": "GIAM20"
}
```

Neu dung checkout redirect cua SePay (theo huong dan sepay-pg-node):

```json
{
  "user_id": "uuid-user",
  "order_code": "ORD-ABCD1234",
  "subtotal": 250000,
  "payment_mode": "SEPAY_CHECKOUT",
  "success_url": "https://example.com/order/ORD-ABCD1234?payment=success",
  "error_url": "https://example.com/order/ORD-ABCD1234?payment=error",
  "cancel_url": "https://example.com/order/ORD-ABCD1234?payment=cancel"
}
```

Response mau:

```json
{
  "success": true,
  "order": {
    "id": "...",
    "order_code": "ORD-ABCD1234",
    "total_amount": 220000,
    "payment_status": "pending",
    "payment_qr_url": "https://img.vietqr.io/image/...",
    "payment_reference": "FOODAPP ORD-ABCD1234"
  },
  "payment": {
    "provider": "sepay",
    "qr_url": "https://img.vietqr.io/image/...",
    "transfer_content": "FOODAPP ORD-ABCD1234",
    "amount": 220000
  }
}
```
