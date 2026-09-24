# 🔐 07. SCREEN SPEC: AUTH & GUEST GATE (XÁC THỰC & CỔNG BẢO VỆ KHÁCH)

> **Mã đặc tả:** `DESIGN_SPEC_07_AUTH_GUEST_GATE`  
> **Package mã nguồn:** `feature:auth` & `core:datastore`  
> **Thành phần chính:** `LoginScreen`, `OtpVerificationScreen`, `LoginGateBottomSheet`  
> **Cập nhật:** 25/09/2026  

---

## 1. MỤC TIÊU MÀN HÌNH & USER STORY

- **Mục tiêu:** Cho phép người dùng trải nghiệm lướt xem thực đơn thoải mái không bắt ép đăng nhập (**Guest-First Experience**). Chỉ khi người dùng thực hiện hành vi thanh toán đơn hàng hoặc lưu địa chỉ, hệ thống mới hiển thị **Cổng bảo vệ đăng nhập (Login Gate)** mà vẫn **bảo toàn nguyên vẹn 100% giỏ hàng** sau khi đăng nhập thành công.
- **User Story:**
  > *"Tôi muốn có thể xem thử quán ăn và chọn món ngay mà không bị chặn bởi màn hình đăng nhập phiền phức. Khi tôi quyết định mua hàng, tôi chỉ cần nhập số điện thoại và mã OTP gửi qua SMS là xong ngay lập tức."*

---

## 2. BLUEPRINT BỐ CỤC GIAO DIỆN (ASCII WIREFRAME)

### 2.1 Cổng Đăng Nhập Dạng Modal (`LoginGateBottomSheet`)
```
┌──────────────────────────────────────────────────────────┐
│                          ──────                          │ <-- Drag Handle
│  🔐 Đăng nhập để tiếp tục thanh toán                     │
│  Giỏ hàng của bạn (3 món) sẽ được lưu lại an toàn.       │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ 🇻🇳 (+84) | Nhập số điện thoại của bạn               │ │ <-- Phone TextField (48dp)
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ [ Tiếp tục với mã OTP SMS                             ]  │ <-- Primary CTA
├──────────────────────────────────────────────────────────┤
│ ─────────────── Hoặc đăng nhập với ───────────────       │
│ [ 🌐 Đăng nhập bằng Google ]                             │ <-- Outlined Button
│ [ 🍏 Đăng nhập bằng Apple  ]                             │
├──────────────────────────────────────────────────────────┤
│ Nhấp "Tiếp tục", bạn đồng ý với Điều khoản dịch vụ BiteFast│
└──────────────────────────────────────────────────────────┘
```

### 2.2 Màn Hình Nhập Mã OTP 6 Số (`OtpVerificationScreen`)
```
┌──────────────────────────────────────────────────────────┐
│ [⬅️] Xác Thực Mã OTP                                    │
│  Mã xác thực gồm 6 số đã được gửi tới (+84) 912 345 678  │
├──────────────────────────────────────────────────────────┤
│                                                          │
│     [ 4 ]   [ 8 ]   [ 2 ]   [ 1 ]   [ 9 ]   [ 0 ]        │ <-- 6 Pin Cells (56x56dp)
│                                                          │
│  Gửi lại mã sau (00:45)                                  │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │    1                 2                 3             │ │
│ │    4                 5                 6             │ │ <-- Numeric Pin Keypad
│ │    7                 8                 9             │ │
│ │                      0                 ⌫             │ │
│ └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

---

## 3. CƠ CHẾ BẢO TOÀN GIỎ HÀNG KHÔNG MẤT DỮ LIỆU (ZERO-DATA-LOSS CART MIGRATION)

```
[Khách vãng lai thêm món (Guest Cart: Local Database)]
                          │
                          ▼
[Bấm "Thanh toán" -> LoginGateBottomSheet xuất hiện]
                          │
                          ▼
[Đăng nhập OTP thành công -> Nhận AccessToken & UserId]
                          │
                          ▼
[Gọi CartRepository.migrateGuestCartToUser(userId)]
                          │
                          ▼
[Giữ nguyên màn hình Checkout -> Không bắt người dùng chọn lại món từ đầu]
```
