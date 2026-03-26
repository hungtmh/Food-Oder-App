# sepay-webhook

Nhan callback tu SePay va cap nhat don hang da thanh toan.

Che do xac thuc hien tai: STRICT
- Neu payload co field trang thai thi bat buoc phai la trang thai thanh cong (success/paid/completed/done/...)
- Neu payload khong co field trang thai, function van cho phep xu ly (mot so callback transfer chi gui noi dung + so tien)
- Bat buoc payload co so tien thanh toan
- So tien thanh toan phai khop CHINH XAC voi `orders.total_amount`

## URL webhook

`https://qqmqucmebzvcwcgjnbph.supabase.co/functions/v1/sepay-webhook`

## Secrets can set tren Supabase Dashboard

- `SEPAY_WEBHOOK_SECRET`: khoa bi mat de xac thuc request webhook.
- `SUPABASE_URL`: co san trong runtime.
- `SUPABASE_SERVICE_ROLE_KEY`: co san trong runtime.

## Header bat buoc tu SePay

- `Authorization: Apikey <SEPAY_WEBHOOK_SECRET>`

Tuong thich nguoc (de test cu):
- `x-sepay-webhook-secret: <SEPAY_WEBHOOK_SECRET>`

## Payload toi thieu

Function co the doc linh hoat cac truong:
- `transfer_content` / `payment_content` / `transaction_content` / `content` / `description`
- hoac `order_invoice_number` / `order_code`

Co ho tro ca snake_case va camelCase (vd: `transferAmount`, `paymentStatus`).
Neu `content` co them text du, function se co gang tach token `ORD...` de map ve `payment_reference` theo dinh dang `FOODAPP ORD...`.

Trang thai thanh cong co the doc tu:
- `status` / `payment_status` / `transaction_status`

So tien thanh toan co the doc tu:
- `amount` / `transfer_amount` / `transferAmount` / `paid_amount` / `paidAmount`

Neu payload chi gui `ORD-XXXX`, function tu map thanh `FOODAPP ORD-XXXX` de khop voi `payment_reference` khi tao don.

## Deploy

```bash
supabase functions deploy sepay-webhook
```
