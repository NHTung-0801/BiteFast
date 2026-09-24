# 📍 06. SCREEN SPEC: REAL-TIME TRACKING (THEO DÕI ĐƠN HÀNG THỜI GIAN THỰC)

> **Mã đặc tả:** `DESIGN_SPEC_06_TRACKING_REALTIME`  
> **Package mã nguồn:** `feature:tracking` & `core:network` (WebSocket/SSE)  
> **Thành phần chính:** `TrackingScreen`, `GoogleMapTrackingView`, `DriverPulseMarker`, `OrderTimelineBottomSheet`  
> **Cập nhật:** 25/09/2026  

---

## 1. MỤC TIÊU MÀN HÌNH & USER STORY

- **Mục tiêu:** Cung cấp trải nghiệm theo dõi đơn hàng sống động, trực quan trên bản đồ số; cập nhật vị trí shipper từng giây qua WebSocket/SSE mà không giật cục nhờ thuật toán nội suy tọa độ; hiển thị rõ ràng thời gian dự kiến giao hàng (ETA) và timeline các bước chuẩn bị.
- **User Story:**
  > *"Sau khi đặt đơn, tôi muốn xem ngay trên bản đồ tài xế đang ở đâu, quãng đường tài xế đi tới nhà tôi, biết chính xác còn bao nhiêu phút nữa thì đồ ăn tới để tôi sẵn sàng nhận hàng."*

---

## 2. BLUEPRINT BỐ CỤC GIAO DIỆN (ASCII WIREFRAME)

```
┌──────────────────────────────────────────────────────────┐
│ [⬅️ Trang chủ]       ĐƠN HÀNG #BF-9821           [🆘 Hỗ trợ]│ <-- 1. Floating Top Bar
├──────────────────────────────────────────────────────────┤
│                                                          │
│                [ BẢN ĐỒ MAPS COMPOSE ]                   │
│                                                          │
│      🏪 (Nhà hàng)                                       │
│          ╲                                               │
│           ╲────── 🛵 (Shipper Pulse Marker)              │ <-- 2. Polyline & Marker
│                    ╲                                     │
│                     ╲───── 🏠 (Địa chỉ của bạn)          │
│                                                          │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │                  ──────                              │ │ <-- 3. BottomSheet Handle
│ │  ⚡ DỰ KIẾN GIAO: 12:45 (Còn khoảng 12 phút)          │ │ <-- 4. Big ETA Counter
│ │  ████████████████████▒▒▒▒▒▒  (Đang giao hàng)        │ │ <-- 5. Progress Bar
│ ├──────────────────────────────────────────────────────┤ │
│ │ 🛵 Tài xế: Trần Văn Bình (BiteFast Rider)            │ │ <-- 6. Driver Info
│ │    ⭐ 4.9 • Xe Honda Wave • Biển số: 59-X1 999.88    │ │
│ │    [ 📞 Gọi điện (48dp) ]     [ 💬 Nhắn tin (48dp) ] │ │
│ ├──────────────────────────────────────────────────────┤ │
│ │ 📋 Timeline Trạng Thái                               │ │ <-- 7. Order Timeline
│ │  ✅ 12:15 - Đơn hàng đã được xác nhận                │ │
│ │  ✅ 12:25 - Nhà hàng đã chuẩn bị xong món            │ │
│ │  🔵 12:33 - Tài xế đang trên đường giao tới bạn      │ │
│ │  ⚪ --:-- - Đã giao hàng thành công                  │ │
│ └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

---

## 3. THUẬT TOÁN XOAY VÀ NỘI SUY VỊ TRÍ TÀI XẾ (SMOOTH MARKER MOTION)

Khi nhận tọa độ mới $(lat_2, lng_2)$ từ WebSocket:
1. **Tính góc xoay (Bearing Angle):**
   $$\theta = \text{atan2}(\sin(\Delta lng) \cdot \cos(lat_2), \cos(lat_1) \cdot \sin(lat_2) - \sin(lat_1) \cdot \cos(lat_2) \cdot \cos(\Delta lng))$$
   Biểu tượng xe máy xoay mượt mà theo góc $\theta$ với thời lượng 400ms (`animateFloatAsState`).
2. **Nội suy chuyển động (Spherical Interpolation):**
   Tọa độ Marker di chuyển tịnh tiến đều giữa tọa độ cũ và mới qua `animateValueAsState` trong khoảng thời gian giữa 2 gói tin định vị (2000ms), ngăn chặn hiện tượng xe máy bị "nhảy cóc" trên bản đồ.
3. **Hiệu ứng sóng tỏa (Pulse Ring):**
   Vòng tròn bán kính 20dp dưới chân xe máy tỏa rộng ra và mờ dần (Alpha 0.6 -> 0.0) với chu kỳ 1500ms tạo cảm giác tín hiệu GPS đang phát sóng trực tiếp.
