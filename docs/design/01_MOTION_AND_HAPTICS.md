# ⚡ 01. MOTION & HAPTICS (CHUYỂN ĐỘNG & XÚC GIÁC TƯƠNG TÁC)

> **Mã đặc tả:** `DESIGN_SPEC_01_MOTION_HAPTICS`  
> **Áp dụng cho:** Toàn bộ hiệu ứng thị giác và rung phản hồi trong BiteFast  
> **Mục tiêu:** Tạo trải nghiệm lướt mượt mà 60-120fps, không gây giật lag, tăng cảm giác tin cậy và phản hồi vật lý tự nhiên.

---

## 1. NGUYÊN LÝ CHUYỂN ĐỘNG VẬT LÝ (PHYSICS-BASED MOTION)

Trong BiteFast, chuyển động không phải là trang trí phụ mà là **công cụ điều hướng sự chú ý của mắt người dùng**:
1. **Không dùng chuyển động tuyến tính cứng nhắc (Linear):** Mọi chuyển động biến đổi kích thước, di chuyển vị trí đều dùng mô hình lò xo vật lý (`Spring`) hoặc đường cong hãm đà tự nhiên (`FastOutSlowInEasing`).
2. **Thời lượng chuẩn xác:**
   - Phản hồi nhấn nút (Micro-taps): **100ms - 150ms**.
   - Mở BottomSheet / Dialog: **250ms - 300ms**.
   - Chuyển trang màn hình: **300ms - 400ms**.
   - Chu kỳ Shimmer Skeleton: **1200ms lặp vô hạn**.

---

## 2. ĐẶC TẢ CHI TIẾT SKELETON SHIMMER LOADING

Tuyệt đối cấm sử dụng ProgressBar hình tròn quay giữa màn hình trắng. Thay vào đó, toàn bộ danh sách, thẻ quán ăn, chi tiết món đều sử dụng **Khung xương phát sáng (Skeleton Shimmer)**:

```
┌────────────────────────────────────────────────────────┐
│  [======== SHIMMER GRADIENT (1200ms Loop) =======>]   │
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▒▒▒▒▒▒▒▒▒░░░░░░░░▒▒▒▒▒▓▓▓▓▓▓  │
└────────────────────────────────────────────────────────┘
```

### 2.1 Bảng mã màu Shimmer đa tầng
| Nền Theme | Màu tĩnh cơ sở (Base) | Màu vệt sáng quét qua (Highlight) | Góc quét |
| :--- | :--- | :--- | :---: |
| **Light Theme** | `#E5E7EB` (Cool Gray 200) | `#F9FAFB` (Cool Gray 50) | `20 độ nghiêng` |
| **Dark Theme**  | `#262626` (Surface Muted) | `#3F3F46` (Neutral Highlight) | `20 độ nghiêng` |

### 2.2 Công thức cài đặt Compose
```kotlin
val transition = rememberInfiniteTransition(label = "shimmer_transition")
val translateAnim by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1200, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    ),
    label = "shimmer_offset"
)
```
Mã nguồn thành phần tái sử dụng: [ShimmerEffect.kt](file:///d:/Personal_Project/BiteFast/core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/ShimmerEffect.kt).

---

## 3. HIỆU ỨNG NHẤN VÀ ĐỘ NẢY ĐÀN HỒI (SPRING BOUNCE ON TAP)

Tất cả các nút hành động chính (Primary CTA, Card quán ăn khi chạm, Nút tăng số lượng món ăn) áp dụng hiệu ứng **nhấn thu nhỏ có kiểm soát**:

| Trạng thái tương tác | Tỷ lệ Scale | Thông số Spring Spec |
| :--- | :---: | :--- |
| **Idle (Bình thường)** | `1.0f` | - |
| **Pressed (Đang chạm giữ)** | `0.96f` | `spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium)` |
| **Release (Nhả ngón tay)** | Nảy nhẹ `1.02f` rồi về `1.0f` | `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)` |

### Đặc tả hiệu ứng Stepper Số lượng (`QuantitySelector`):
- Khi bấm nút `+` hoặc `-`:
  - Nút bấm scale nhẹ `0.9f` -> `1.0f`.
  - Con số số lượng món ăn ở giữa nhảy nhẹ lên trên 2dp và phóng to `1.2f` trước khi trở về `1.0f` (Scale & Translate Pop animation) trong `150ms`.

---

## 4. MA TRẬN PHẢN HỒI RUNG XÚC GIÁC (HAPTIC FEEDBACK MATRIX)

Phản hồi rung đóng vai trò như cảm giác cơ học của nút bấm thực tế:

| Hành vi người dùng | Kiểu rung đề xuất | Cường độ & Cảm giác | Mục đích trải nghiệm |
| :--- | :--- | :--- | :--- |
| **Bấm nút (+/-) tăng giảm số lượng** | `HapticFeedbackType.TextHandleMove` (hoặc `VibrationEffect.EFFECT_TICK`) | Rất nhẹ, sắc gọn (Tick) | Giúp ngón tay cảm nhận đã đếm thêm 1 món mà không cần nhìn chăm chú |
| **Bấm "Thêm vào giỏ hàng"** | `HapticFeedbackType.LongPress` (hoặc `EFFECT_CLICK`) | Vừa phải, chắc chắn | Xác nhận hành động thêm món thành công |
| **Kéo làm mới (Pull-to-Refresh) chạm ngưỡng** | `HapticFeedbackType.LongPress` | Nhẹ nhàng khi vượt ngưỡng tải | Báo hiệu cho người dùng có thể nhả tay để tải lại |
| **Chuyển Tab danh mục món ăn** | `HapticFeedbackType.TextHandleMove` | Siêu nhẹ | Cảm giác lướt qua các nấc bánh xe |
| **Xác thực vân tay / Đặt đơn thành công** | `EFFECT_HEAVY_CLICK` (Nhịp đôi) | Mạnh mẽ, vang dội | Cảm giác hoàn thành một giao dịch quan trọng |
| **Cảnh báo xung đột quán ăn (Conflict Dialog)** | Rung cảnh báo đôi (Double buzz 100ms - 50ms - 100ms) | Rõ rệt, ngắt quãng | Cảnh báo việc giỏ hàng hiện tại sẽ bị xóa để đổi sang quán mới |

---

## 5. HIỆU ỨNG CHUYỂN CẢNH ĐẶC BIỆT (SPECIAL SCENE TRANSITIONS)

### 5.1 Parallax Hero Image (Chi tiết nhà hàng)
- Khi cuộn màn hình chi tiết quán lên trên:
  - Ảnh đại diện nhà hàng thu nhỏ dần tỷ lệ và áp dụng lớp làm mờ đen mờ (Dark Scrim) từ `0%` lên `85%`.
  - Tên nhà hàng từ tiêu đề lớn ở ảnh trượt mượt mà lên thành tiêu đề nhỏ trên TopAppBar (Shared Layout Transition).

### 5.2 Animated Offline Banner (Thanh thông báo mất kết nối)
- **Khi rớt mạng:** Thanh thông báo màu cam cảnh báo trượt xuống từ mép trên cùng (`slideInVertically(initialOffsetY = { -it })`), biểu tượng WiFi gạch chéo nhấp nháy nhẹ.
- **Khi có mạng trở lại:** Thanh thông báo chuyển sang màu xanh lá Mint (`#10B981`) với nội dung *"Đã kết nối trở lại"*, duy trì 2000ms rồi trượt ẩn lên trên (`slideOutVertically`).
