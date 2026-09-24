# 🛒 04. SCREEN SPEC: CART & RESTAURANT CONFLICT (GIỎ HÀNG & XUNG ĐỘT QUÁN ĂN)

> **Mã đặc tả:** `DESIGN_SPEC_04_CART_CONFLICT`  
> **Package mã nguồn:** `feature:cart` & `core:database`  
> **Composable chính:** [CartScreen.kt](file:///d:/Personal_Project/BiteFast/feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartScreen.kt)  
> **ViewModel:** [CartViewModel.kt](file:///d:/Personal_Project/BiteFast/feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartViewModel.kt)  
> **Cập nhật:** 25/09/2026  

---

## 1. MỤC TIÊU MÀN HÌNH & USER STORY

- **Mục tiêu:** Cung cấp giao diện kiểm tra chi tiết các món đã chọn, điều chỉnh số lượng thuận tiện (vùng chạm 48dp), xem chi tiết giá tiền minh bạch (Tiền món, Phí giao hàng, Phí dịch vụ, Giảm giá voucher) và giải quyết triệt để bài toán **Xung đột quán ăn** (Multi-restaurant conflict) một cách lịch sự, an toàn.
- **User Story:**
  > *"Trước khi thanh toán, tôi muốn xem lại giỏ hàng của mình, tăng giảm hoặc xóa bớt món ăn nhanh chóng, nhập mã giảm giá và biết rõ từng khoản tiền mà không có phí ẩn."*

---

## 2. BLUEPRINT BỐ CỤC GIAO DIỆN (ASCII WIREFRAME)

```
┌──────────────────────────────────────────────────────────┐
│ [⬅️] Giỏ Hàng                                      [🗑️ Xóa]│ <-- 1. Top Bar
├──────────────────────────────────────────────────────────┤
│ 🏪 Cơm Tấm Phúc Lộc Thọ - Lê Văn Việt        [+ Thêm món]│ <-- 2. Restaurant Tag
│ 📍 Giao tới: 123 Nguyễn Huệ, P. Bến Nghé, Quận 1         │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ [ẢNH] Cơm Tấm Sườn Bì Chả                            │ │ <-- 3. Cart Item Row
│ │       Topping: Trứng ốp la, Chả hấp                  │ │
│ │       55.000đ               [ - ]  2  [ + ] (48dp)   │ │
│ └──────────────────────────────────────────────────────┘ │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ [ẢNH] Canh Khổ Qua Nhồi Thịt                         │ │
│ │       15.000đ               [ - ]  1  [ + ] (48dp)   │ │
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ 🏷️ BITEFAST50 - Giảm 20.000đ            [Thay đổi >] │ │ <-- 4. Voucher Strip
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ Chi tiết thanh toán                                  │ │ <-- 5. Bill Breakdown
│ │ Tiền món (3 món):                           125.000đ │ │
│ │ Phí giao hàng (1.2 km):                      16.000đ │ │
│ │ Phí dịch vụ nền tảng:                         2.000đ │ │
│ │ Khuyến mãi voucher:                         -20.000đ │ │
│ │ ───────────────────────────────────────────────────  │ │
│ │ Tổng cộng:                                  123.000đ │ │
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ [ Thanh Toán Ngay • 123.000đ                         > ] │ <-- 6. Sticky Checkout CTA
└──────────────────────────────────────────────────────────┘
```

---

## 3. HỘP THOẠI XỬ LÝ XUNG ĐỘT QUÁN ĂN (RESTAURANT CONFLICT DIALOG)

Khi người dùng đang có món của **Quán A** trong giỏ, nhưng lại bấm thêm món từ **Quán B**:

```
┌──────────────────────────────────────────────────────────┐
│                      ⚠️ TẠO GIỎ MỚI?                     │
│                                                          │
│  Giỏ hàng của bạn hiện đang có 2 món từ:                 │
│  "Cơm Tấm Phúc Lộc Thọ"                                  │
│                                                          │
│  Bạn có muốn làm mới giỏ hàng để bắt đầu đặt món từ:     │
│  "Phở Thìn Lò Đúc" không?                                │
│                                                          │
│   ┌────────────────────────┐  ┌───────────────────────┐  │
│   │        Giữ giỏ cũ      │  │      Tạo giỏ mới      │  │
│   │       (OutlinedButton) │  │       (Red / Primary) │  │
│   └────────────────────────┘  └───────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

### 3.1 Quy chuẩn kỹ thuật của Conflict Dialog
- **Hình khối:** `ShapeLarge` (24dp), độ nổi `Elevation Level 4` (16dp).
- **Phản hồi rung:** Rung cảnh báo kép (`Double Buzz` 100ms - 50ms pause - 100ms) khi Dialog xuất hiện.
- **Hành động "Giữ giỏ cũ":** Đóng dialog, giữ nguyên giỏ hàng cũ, hiển thị Toast/Snackbar: *"Đã giữ lại giỏ hàng hiện tại"*.
- **Hành động "Tạo giỏ mới":** Gọi use case xóa sạch giỏ cũ trong database Room (`@Transaction clearAndInsert`), sau đó tự động thêm món mới của Quán B vào giỏ, điều hướng tiếp mà không làm đứt đoạn hành trình.

---

## 4. CHI TIẾT THÀNH PHẦN TƯƠNG TÁC (MICRO-INTERACTIONS)

### 4.1 Stepper Tăng Giảm Số Lượng (QuantitySelector 48dp)
- Chiều cao tổng thể: 36dp (nhưng vùng tương tác vô hình bao quanh đạt chuẩn **48x48dp**).
- Nút bấm `+` và `-`: Bo góc `ShapeSmall` (8dp), nền `SurfaceVariant`.
- Khi số lượng = 1 và bấm nút `-`: Hiển thị icon thùng rác màu đỏ (`DeleteSweep`) thay vì dấu trừ, nhắc nhở món ăn sẽ bị xóa khỏi giỏ nếu bấm tiếp.

### 4.2 Thao tác Vuốt để Xóa (Swipe-to-Dismiss / Swipe-to-Delete)
- Cho phép người dùng vuốt thẻ món ăn từ phải sang trái.
- Khi vuốt: Lộ ra nền đỏ `ErrorRed` (`#EF4444`) cùng icon Thùng rác phóng to dần theo độ dài vuốt.
- Khi vuốt qua 50% chiều rộng thẻ: Tự động kích hoạt rung nhẹ và kích hoạt hoạt ảnh co rút chiều cao thẻ về 0dp trước khi xóa khỏi database.
