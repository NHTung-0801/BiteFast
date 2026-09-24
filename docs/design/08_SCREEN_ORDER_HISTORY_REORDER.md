# 📜 08. SCREEN SPEC: ORDER HISTORY & SMART RE-ORDER (LỊCH SỬ ĐƠN & ĐẶT LẠI 1 CHẠM)

> **Mã đặc tả:** `DESIGN_SPEC_08_ORDER_HISTORY_REORDER`  
> **Package mã nguồn:** `feature:order` & `core:domain`  
> **Thành phần chính:** `OrderHistoryScreen`, `ReorderUseCase`, `OrderCard`  
> **Cập nhật:** 25/09/2026  

---

## 1. MỤC TIÊU MÀN HÌNH & USER STORY

- **Mục tiêu:** Quản lý toàn bộ danh sách đơn hàng đã đặt, phân loại rành mạch giữa đơn đang giao và đơn lịch sử; cung cấp tính năng **Đặt lại đơn hàng 1 chạm (Smart Re-Order)** giúp tái hiện toàn bộ món, topping của đơn cũ vào giỏ chỉ trong 1 giây mà vẫn kiểm tra tính sẵn sàng của menu (món còn bán hay đã hết).
- **User Story:**
  > *"Tôi thường xuyên ăn món cơm tấm quen thuộc vào buổi trưa. Tôi muốn vào lịch sử, bấm đúng 1 nút 'Đặt lại' là toàn bộ món và topping tôi thích được đưa vào giỏ ngay để tôi bấm thanh toán luôn mà không cần chọn lại từng topping."*

---

## 2. BLUEPRINT BỐ CỤC GIAO DIỆN (ASCII WIREFRAME)

```
┌──────────────────────────────────────────────────────────┐
│ Lịch Sử Đơn Hàng                                         │ <-- Top Bar
├──────────────────────────────────────────────────────────┤
│ [ Đang Đến (1) ]                  [ Lịch Sử Đã Giao (14)]│ <-- Two-Tab Segment
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ 🏪 Cơm Tấm Phúc Lộc Thọ - Lê Văn Việt                │ │ <-- Past Order Card
│ │ 📅 Hôm qua, 12:30 • Hoàn tất (✅)                    │ │
│ │ ───────────────────────────────────────────────────  │ │
│ │ • 2x Cơm Tấm Sườn Bì Chả (Trứng lòng đào, Chả hấp)   │ │
│ │ • 1x Canh Khổ Qua Nhồi Thịt                          │ │
│ │                                                      │ │
│ │ Tổng tiền: 123.000đ (Đã thanh toán MoMo)             │ │
│ │ ───────────────────────────────────────────────────  │ │
│ │ [ ⭐ Đánh giá (48dp) ]     [ 🔄 ĐẶT LẠI ĐƠN NÀY (48dp)]│ │ <-- Action Buttons
│ └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

---

## 3. LOGIC SMART RE-ORDER VÀ TRẠNG THÁI KIỂM TRA MÓN ĂN

Khi người dùng nhấn nút `🔄 ĐẶT LẠI ĐƠN NÀY`:
1. **Kiểm tra trạng thái quán ăn:** Quán ăn có đang mở cửa không? Nếu đóng cửa, hiển thị Toast cảnh báo: *"Quán hiện đang đóng cửa"*.
2. **Kiểm tra tồn kho món ăn:** Gọi `ValidateReorderItemsUseCase`.
   - Nếu toàn bộ món và topping còn đủ: Đưa vào giỏ hàng và điều hướng thẳng tới [CartScreen.kt](file:///d:/Personal_Project/BiteFast/feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartScreen.kt).
   - Nếu có 1 món tạm thời hết hàng: Hiển thị Dialog thông báo: *"Món 'Canh Khổ Qua' hiện đã hết. Bạn có muốn đặt lại các món còn lại không?"*.
