# Push Notification Pipeline (Supabase + Android)

## 1. Muc tieu tai lieu
Tai lieu nay tong hop:
- Ket qua ban da tim hieu trong buoi nay.
- Hien trang tinh nang thong bao trong app.
- Pipeline trien khai push notification thuc su cho Android.
- Checklist test demo khi chi co 1 nguoi phat trien.

---

## 2. Hien trang app (as-is)

### 2.1 Dang co gi
- App da co man hinh danh sach thong bao (in-app inbox).
- Du lieu thong bao duoc luu va doc tu DB (Supabase table `notifications`).
- Badge so thong bao chua doc da co tren Home.
- Khi Admin doi trang thai don (`processing`, `served`, `cancelled`), app tao ban ghi thong bao trong DB.

### 2.2 Chua co gi
- Chua co push notification he thong (khong co FCM service, channel, runtime permission flow).
- Chua co luong gui thong bao realtime tu backend sang thiet bi.

### 2.3 Ket luan hien trang
Thong bao hien tai la **inbox notification** (mo app moi thay), chua phai **push notification** (he dieu hanh hien ngay ca khi dong app).

---

## 3. Kien truc de co push notification that

## Thanh phan
1. Android App
- Dang ky token voi FCM.
- Gui token len Supabase.
- Nhan push va hien thi thong bao he thong.

2. Supabase
- Luu token theo user/device.
- Luu lich su thong bao trong `notifications`.
- Edge Function (hoac BE service) de gui push qua FCM.

3. FCM (Firebase Cloud Messaging)
- Dong vai tro push broker cho Android.
- Nhan request tu backend va day den dung device token.

## Nguyen tac
Khi co su kien (vi du doi trang thai don), backend lam **2 nhanh song song**:
1. Ghi DB de luu lich su thong bao.
2. Goi FCM de day thong bao realtime.

---

## 4. Pipeline xu ly end-to-end

## Giai doan A - Dang ky device
1. User dang nhap app.
2. App xin quyen thong bao (Android 13+).
3. App lay FCM token.
4. App goi API/Edge Function de upsert token vao bang `device_tokens`.

## Giai doan B - Phat sinh su kien nghiep vu
1. Admin cap nhat trang thai don hang.
2. Backend ghi 1 ban ghi vao `notifications`.
3. Backend lay danh sach token cua user nhan thong bao.
4. Backend goi FCM send API den cac token hop le.

## Giai doan C - Nhan va hien thi tren may
1. FCM day message den thiet bi.
2. Neu app foreground: app xu ly callback va co the tu show local notification.
3. Neu app background/kill: he dieu hanh hien thong bao.
4. User bam thong bao -> mo man hinh mong muon (deep link/intent extras).

## Giai doan D - Dong bo trang thai da doc
1. User mo man hinh Notifications.
2. App doc danh sach tu `notifications`.
3. User bam vao item hoac Doc tat ca -> cap nhat `is_read`.
4. Badge Home cap nhat theo unread count.

---

## 5. Data model de xay dung

## 5.1 Bang `device_tokens`
Muc dich: quan ly token theo user va theo thiet bi.

Goi y cot:
- `id` UUID PK
- `user_id` UUID FK -> users
- `fcm_token` TEXT UNIQUE
- `platform` TEXT (android)
- `device_name` TEXT
- `app_version` TEXT
- `is_active` BOOLEAN DEFAULT true
- `last_seen_at` TIMESTAMPTZ
- `created_at` TIMESTAMPTZ DEFAULT now()
- `updated_at` TIMESTAMPTZ DEFAULT now()

Index goi y:
- index `user_id`
- unique index `fcm_token`

## 5.2 Bang `notifications`
Da co trong he thong runtime cua ban. Nen dam bao co cac cot:
- `id`, `user_id`, `order_id`, `order_code`, `title`, `message`, `is_read`, `created_at`

## 5.3 Bang `notification_deliveries` (khuyen nghi)
Muc dich: theo doi trang thai gui push, tranh gui trung, debug de van hanh.

Cot goi y:
- `id`, `notification_id`, `user_id`, `fcm_token`
- `provider` (fcm)
- `status` (queued/sent/failed/invalid_token)
- `provider_message_id`
- `error_message`
- `created_at`

---

## 6. Ke hoach trien khai ky thuat

## Buoc 1 - Firebase setup
1. Tao project Firebase.
2. Add Android app package name (trung `applicationId`).
3. Tai `google-services.json` dat vao `app/`.
4. Them Firebase plugin + dependency FCM vao Gradle.

## Buoc 2 - Android app code
1. Xin permission `POST_NOTIFICATIONS` (Android 13+).
2. Tao Notification Channel (Android 8+).
3. Tao class `FirebaseMessagingService`:
- `onNewToken()` -> gui token len backend.
- `onMessageReceived()` -> parse payload va show local notification khi can.
4. Dam bao click notification mo dung Activity (Notifications/OrderDetail).

## Buoc 3 - Supabase schema + API
1. Tao bang `device_tokens`.
2. Tao endpoint (hoac RPC/Edge Function) de upsert token.
3. Dam bao RLS policy cho phep user cap nhat token cua chinh minh.

## Buoc 4 - Send push service
Lua chon:
- Edge Function trong Supabase (khuyen nghi de demo nhanh).
- Hoac backend rieng (Node/Java).

Nhiem vu function:
1. Nhan input su kien (`user_id`, `title`, `body`, `data`).
2. Lay token active cua user.
3. Goi FCM API.
4. Log ket qua vao `notification_deliveries`.
5. Danh dau token invalid neu FCM tra loi token khong hop le.

## Buoc 5 - Tich hop voi luong don hang hien tai
Tai diem doi trang thai don:
1. Van `createNotification` de luu DB.
2. Sau khi tao thanh cong, goi them function `sendPush`.
3. Dat idempotency key theo `order_id + status + timestamp bucket` (hoac `notification_id`) de tranh gui trung.

---

## 7. Test demo khi chi co 1 nguoi

## Yeu cau moi truong
1. Android Emulator image co Google Play.
2. Thiet bi/emulator co internet.
3. Da dang nhap app de co user_id.

## Quy trinh test 1 nguoi
1. Chay app va lay FCM token (logcat/man debug).
2. Luu token vao `device_tokens`.
3. Gui test push den token do:
- Cach A: Firebase Console -> Send test message.
- Cach B: Goi Supabase Edge Function `send-push`.
4. Kiem tra 3 trang thai:
- Foreground
- Background
- App bi kill
5. Bam notification de mo app va verify route/man hinh.
6. Vao man hinh inbox kiem tra ban ghi da co trong DB.

## Tieu chi pass
- Push hien tren system tray.
- Bam vao push mo dung man hinh.
- DB co lich su thong bao tuong ung.
- Badge unread cap nhat dung.

---

## 8. Risk va cach giam thieu
1. Android 13+ khong cap quyen thong bao
- Giai phap: nhac user cap quyen + fallback inbox.

2. Token bi doi/het han
- Giai phap: xu ly `onNewToken`, upsert token moi, disable token cu.

3. Gui trung thong bao
- Giai phap: idempotency key + bang delivery log.

4. Khong thay thong bao khi app foreground
- Giai phap: tu show local notification trong `onMessageReceived()`.

5. Mat thong bao khi backend loi
- Giai phap: retry queue, log that bai, dashboard monitor.

---

## 9. Lo trinh de xuat (2-4 ngay)

Ngay 1:
- Firebase setup.
- Them Android permission + channel + service.
- Lay va luu token len Supabase.

Ngay 2:
- Tao Edge Function gui push qua FCM.
- Test gui thu cong theo token.

Ngay 3:
- Noi luong business: doi trang thai don -> ghi DB + gui push.
- Them tracking `notification_deliveries`.

Ngay 4:
- Hoan thien UX, retry, anti-duplicate, test regression.
- Viet tai lieu handover cho team.

---

## 10. Checklist implementation

## Android
- [ ] Them Firebase dependencies
- [ ] Them `google-services.json`
- [ ] Xin `POST_NOTIFICATIONS`
- [ ] Tao Notification Channel
- [ ] Tao `FirebaseMessagingService`
- [ ] Xu ly click deep link

## Supabase
- [ ] Tao bang `device_tokens`
- [ ] RLS policy cho token
- [ ] Tao Edge Function `send-push`
- [ ] Quan ly secret FCM service account

## Business flow
- [ ] Sau `createNotification` goi `send-push`
- [ ] Them idempotency
- [ ] Log ket qua delivery

## QA
- [ ] Test foreground/background/killed
- [ ] Test deny permission
- [ ] Test token invalid
- [ ] Test duplicate prevention

---

## 11. Ket luan
Ban da xac dinh dung van de cot loi:
- He thong hien tai la inbox thong bao tu DB.
- Muon thong bao realtime thi can bo sung push pipeline qua FCM (hoac provider trung gian).

Huong trien khai tot nhat cho stack hien tai:
- Supabase + FCM + Edge Function,
- Dong thoi giu `notifications` lam lich su de UX on dinh va de doi soat.
