# 💳 05. SCREEN SPEC: CHECKOUT & BIOMETRICS (THANH TOÁN & XÁC THỰC SINH TRẮC HỌC)

> **Mã đặc tả:** `DESIGN_SPEC_05_CHECKOUT_BIOMETRIC`  
> **Package mã nguồn:** `feature:checkout` & `core:network`  
> **Thành phần chính:** `CheckoutScreen`, `VoucherSelectionBottomSheet`, `BiometricPromptManager`  
> **Cập nhật:** 25/09/2026  

---

## 1. MỤC TIÊU MÀN HÌNH & USER STORY

- **Mục tiêu:** Cung cấp quy trình hoàn tất đơn hàng chỉ trong **2 bước chạm**, hỗ trợ chọn phương thức thanh toán đa dạng (Ví MoMo, ZaloPay, Thẻ ngân hàng, Tiền mặt COD) và bảo vệ giao dịch bằng **Xác thực sinh trắc học vân tay / Face Unlock** theo chuẩn AndroidX Biometric API.
- **User Story:**
  > *"Khi tôi bấm thanh toán, tôi muốn kiểm tra lại địa chỉ giao hàng và số điện thoại, áp dụng mã giảm giá tốt nhất chỉ với một chạm, và xác nhận thanh toán bằng vân tay siêu nhanh, an toàn."*

---

## 2. BLUEPRINT BỐ CỤC GIAO DIỆN (ASCII WIREFRAME)

```
┌──────────────────────────────────────────────────────────┐
│ [⬅️] Xác Nhận Đơn Hàng                                  │ <-- 1. Top Bar
├──────────────────────────────────────────────────────────┤
│ 📍 Địa chỉ giao hàng                         [Thay đổi >]│ <-- 2. Address Card
│   Nguyễn Hữu Tùng | (+84) 912 345 678                    │
│   Tòa nhà Bitexco, 2 Hải Triều, P. Bến Nghé, Quận 1      │
│   Ghi chú: Giao sảnh lễ tân tầng 1                       │
├──────────────────────────────────────────────────────────┤
│ ⏰ Thời gian giao hàng: Giao ngay (20 - 30 phút)         │ <-- 3. Delivery Time
├──────────────────────────────────────────────────────────┤
│ 🍽️ Tóm tắt món (3 món từ Cơm Tấm Phúc Lộc Thọ)           │ <-- 4. Order Summary Preview
│   • 2x Cơm Tấm Sườn Bì Chả                     110.000đ  │
│   • 1x Canh Khổ Qua Nhồi Thịt                   15.000đ  │
├──────────────────────────────────────────────────────────┤
│ 🏷️ Mã Khuyến Mãi & Giảm Giá                 [Chọn mã >]  │ <-- 5. Voucher Strip
│   [ BITEFAST50 - Giảm 20.000đ ]                          │
├──────────────────────────────────────────────────────────┤
│ 💳 Phương thức thanh toán                    [Thay đổi >]│ <-- 6. Payment Selection
│   🔘 Ví điện tử MoMo                      [Logo MoMo]    │
│   ⚪ Thẻ tín dụng / Ghi nợ (•••• 8899)    [Logo Visa]    │
│   ⚪ Tiền mặt khi nhận hàng (COD)                        │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ Tổng tiền thanh toán:                       123.000đ │ │ <-- 7. Bill Summary
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ [ 🔒 ĐẶT ĐƠN BẰNG VÂN TAY • 123.000đ                   ] │ <-- 8. Sticky Biometric CTA
└──────────────────────────────────────────────────────────┘
```

---

## 3. QUY TRÌNH XÁC THỰC SINH TRẮC HỌC (BIOMETRIC AUTHENTICATION FLOW)

```
[Bấm nút "Đặt đơn"] ──> Kiểm tra phần cứng ──> [Hiện Android Biometric Prompt]
                                                           │
              ┌────────────────────────────────────────────┴─────────────────────────────┐
              ▼ (Thành công)                                                            ▼ (Hủy / Thất bại)
     Rung Heavy Click nhịp đôi                                                Hiện thông báo lỗi đỏ
  Gọi API CreateOrder (Idempotency-Key)                                       Cho phép nhập mã PIN / Mật khẩu
              │
              ▼
  Điều hướng sang [06_SCREEN_TRACKING_REALTIME]
```

### 3.1 Tiêu chuẩn AndroidX Biometric
- Sử dụng `BiometricPrompt` với `BIOMETRIC_STRONG` hoặc `DEVICE_CREDENTIAL`.
- Tiêu đề Prompt: *"Xác nhận đặt đơn hàng BiteFast"*.
- Phụ đề: *"Xác thực vân tay để thanh toán số tiền 123.000đ"*.
- Cơ chế chống trùng đơn (**Idempotency-Key**): Mỗi lần bấm đặt đơn sinh một UUID v4 ngẫu nhiên gửi trong Header `Idempotency-Key` tới máy chủ backend, chống tình trạng người dùng bấm liên tục gây trừ tiền hai lần.

---

## 4. BOTTOMSHEET CHỌN VOUCHER ƯU ĐÃI

Khi bấm vào mục *"Mã Khuyến Mãi"*:
- Mở `ModalBottomSheet` hiển thị danh sách voucher hợp lệ và voucher chưa đủ điều kiện.
- Thẻ Voucher: Nền `PrimaryContainer` (`#FFEDE6`), đường viền răng cưa (Ticket Perforated Edge), hiển thị điều kiện tối thiểu và hạn sử dụng.
- Nút *"Áp dụng"*: Tự động tính toán lại tổng tiền trong giỏ hàng tức thì, hiển thị thông báo tiết kiệm: *"Bạn đã tiết kiệm được 20.000đ!"*.
