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

## Huong dan gia lap thanh toan bang webhook

Muc tieu: gui mot request webhook giong nhu SePay de cap nhat `orders.payment_status = paid`.

### 1) Chuan bi du lieu don hang de test

Function tim don hang theo `payment_reference` va yeu cau so tien thanh toan khop CHINH XAC voi `orders.total_amount`.

Ban can co 1 don hang co cac truong:
- `payment_reference`: theo mau `FOODAPP ORD-XXXX` (vi du: `FOODAPP ORD-TEST123`)
- `total_amount`: vi du `120000`
- `payment_status`: nen la `pending` truoc khi test

Neu chua co don, tao nhanh 1 ban ghi test (tu SQL editor):

```sql
insert into orders (payment_reference, total_amount, payment_status, status)
values ('FOODAPP ORD-TEST123', 120000, 'pending', 'pending');
```

### 2) Chuan bi webhook secret

Tren Supabase Dashboard > Edge Functions > `sepay-webhook` > Secrets:
- dam bao da set `SEPAY_WEBHOOK_SECRET`

Khi gui request, bat buoc kem header:
- `Authorization: Apikey <SEPAY_WEBHOOK_SECRET>`

### 3) Tao payload gia lap

Payload toi thieu de pass validate:
- co reference map duoc ve `FOODAPP ORD-...`
- co amount
- status thanh cong (hoac bo qua field status)

Vi du payload thanh cong:

```json
{
	"transaction_content": "FOODAPP ORD-TEST123",
	"amount": 120000,
	"status": "success"
}
```

Vi du payload co noi dung chuyen khoan nhieu text (function van tach duoc `ORD-...`):

```json
{
	"content": "CK 120000 VND thanh toan FOODAPP ORD-TEST123 cam on",
	"transferAmount": "120000",
	"paymentStatus": "paid"
}
```

### 4) Gui webhook de test

#### Cach A - PowerShell (Windows)

```powershell
$secret = "YOUR_SEPAY_WEBHOOK_SECRET"
$url = "https://qqmqucmebzvcwcgjnbph.supabase.co/functions/v1/sepay-webhook"

$body = @{
	transaction_content = "FOODAPP ORD-TEST123"
	amount = 120000
	status = "success"
} | ConvertTo-Json

Invoke-RestMethod `
	-Method Post `
	-Uri $url `
	-Headers @{ Authorization = "Apikey $secret" } `
	-ContentType "application/json" `
	-Body $body
```

#### Cach B - curl

```bash
curl -X POST "https://qqmqucmebzvcwcgjnbph.supabase.co/functions/v1/sepay-webhook" \
	-H "Authorization: Apikey YOUR_SEPAY_WEBHOOK_SECRET" \
	-H "Content-Type: application/json" \
	-d '{
		"transaction_content": "FOODAPP ORD-TEST123",
		"amount": 120000,
		"status": "success"
	}'
```

#### Cach C - Postman

1. Method: `POST`
2. URL: `https://qqmqucmebzvcwcgjnbph.supabase.co/functions/v1/sepay-webhook`
3. Headers:
	 - `Authorization: Apikey this-is-a-key`
	 - `Content-Type: application/json`
4. Body (raw JSON): dung 1 trong cac payload mau o tren, phải nhập đúng mã ORD hiện ra ở dưới QR code.

### 5) Ket qua mong doi

Neu thanh cong:

```json
{
	"ok": true,
	"order_id": "...",
	"payment_reference": "FOODAPP ORD-TEST123",
	"paid_amount": 120000
}
```

DB se duoc update:
- `orders.payment_status = paid`
- `orders.paid_at = now()`
- `orders.status = processing`

### 6) Test cac case loi thuong gap

- Sai secret -> `401 Invalid webhook secret`
- Khong tim thay don hang -> `404 Order not found for payment reference`
- Khong co amount -> `400 Missing paid amount in webhook payload`
- Amount khong khop `total_amount` -> `400 Paid amount does not exactly match order total`
- Status khong thanh cong (`failed`, `cancelled`, ...) -> `200 { ok: true, ignored: true, reason: "non-success status" }`

### 7) Luu y idempotent

Neu don da o trang thai `paid`, gui webhook lai se tra:

```json
{
	"ok": true,
	"already_paid": true,
	"order_id": "..."
}
```

Dieu nay giup callback bi gui lap khong lam sai du lieu.
