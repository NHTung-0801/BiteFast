# 🎨 BITEFAST UI/UX DESIGN SYSTEM & SCREEN SPECIFICATIONS

> **Hệ thống Thiết kế Giao diện Chuẩn Mực Enterprise cho BiteFast Food Delivery**  
> **Ngôn ngữ nền tảng:** Jetpack Compose (Material 3)  
> **Tiêu chuẩn tương thích:** Android 8.0+ (API 26+)  
> **Tiêu chuẩn tiếp cận:** WCAG 2.1 AA Compliance  
> **Tài liệu tham chiếu cốt lõi:** [docs/plans/plan_ui_design_specs_architecture.md](../plans/plan_ui_design_specs_architecture.md)

---

## 🌟 TRIẾT LÝ THIẾT KẾ: "CULINARY VELOCITY"

Giao diện của BiteFast được xây dựng dựa trên 4 trụ cột cốt lõi:

1. **Food-First Visual Hierarchy (Ưu tiên thị giác món ăn):**  
   Mọi bố cục tập trung làm nổi bật sự ngon mắt của món ăn thông qua hình ảnh sắc nét tỷ lệ vàng (16:9), bo góc mềm mại, độ tương phản tự nhiên, kích thích vị giác người dùng ngay từ cái nhìn đầu tiên.
2. **Sub-second Perception (Trải nghiệm thị giác siêu tốc):**  
   Loại bỏ hoàn toàn các vòng xoay loading tròn truyền thống gây cảm giác chờ đợi nhàm chán. 100% các trạng thái tải dùng hiệu ứng **Skeleton Shimmer Gradient đa tầng**, kết hợp cập nhật giao diện lạc quan (**Optimistic UI**) khi thêm/sửa giỏ hàng.
3. **One-Handed Ergonomics (Công thái học thao tác một tay):**  
   Khu vực thao tác chính (Bottom Sheets, Nút "Thêm vào giỏ", Thanh tiến trình giao hàng, Stepper số lượng) được gom về 40% nửa dưới màn hình để dễ dàng chạm bằng một ngón cái.
4. **Haptic & Motion Synchronicity (Đồng bộ xúc giác và chuyển động):**  
   Mỗi tương tác vật lý (nhấn nút, vuốt làm mới, tăng giảm số lượng món ăn, thanh toán thành công) đều đi kèm phản hồi rung vi mô (**Haptic Micro-feedback**) đồng điệu với đường cong chuyển động vật lý (**Physics Spring Curves**).

---

## 📂 DANH MỤC TÀI LIỆU ĐẶC TẢ CHI TIẾT (`docs/design/`)

| File Đặc Tả | Phạm Vi Trách Nhiệm | Trạng Thái |
| :--- | :--- | :---: |
| [00_DESIGN_TOKENS_SYSTEM.md](00_DESIGN_TOKENS_SYSTEM.md) | Bảng mã màu Light/Dark, Typography, 4dp Grid, Corner Radii, Elevation Shadows | ✅ Sẵn sàng |
| [01_MOTION_AND_HAPTICS.md](01_MOTION_AND_HAPTICS.md) | Animation Curves, Shimmer Skeleton, Haptic Feedback Profiles, Gestures | ✅ Sẵn sàng |
| [02_SCREEN_DISCOVERY_HOME.md](02_SCREEN_DISCOVERY_HOME.md) | Màn hình Khám phá Trang chủ, SearchBar, Category Chips, Nhà hàng nổi bật | ✅ Sẵn sàng |
| `03_SCREEN_RESTAURANT_DETAIL.md` | Chi tiết Quán ăn, Hero Image Parallax, Topping Modal BottomSheet, Danh mục món | ⏳ Kế tiếp |
| `04_SCREEN_CART_AND_CONFLICT.md` | Giỏ hàng, Stepper 48dp, Dialog xung đột nhà hàng khi đặt món khác quán | ⏳ Kế tiếp |
| `05_SCREEN_CHECKOUT_BIOMETRIC.md` | Xác nhận đơn hàng, Áp Voucher khuyến mãi, Xác thực vân tay sinh trắc học | ⏳ Kế tiếp |
| `06_SCREEN_TRACKING_REALTIME.md` | Theo dõi đơn hàng thời gian thực, Maps Compose, Marker Shipper di chuyển mượt mà | ⏳ Kế tiếp |
| `07_SCREEN_AUTH_AND_GUEST_GATE.md` | Đăng nhập/Đăng ký OTP, Guest Mode & Login Gate BottomSheet bảo vệ giỏ hàng | ⏳ Kế tiếp |
| `08_SCREEN_ORDER_HISTORY_REORDER.md` | Lịch sử đơn hàng, Tab Đang đến/Hoàn tất, Nút "Đặt lại đơn" 1 chạm | ⏳ Kế tiếp |
| `09_SCREEN_RATING_AND_FEEDBACK.md` | Đánh giá 5 sao tương tác, Chọn tag cảm nhận nhanh, Đánh giá ẩn danh | ⏳ Kế tiếp |
| `10_SYSTEM_STATES_AND_A11Y.md` | Bộ chuẩn EmptyState, ErrorState, OfflineBanner kết nối mạng, TalkBack ScreenReader | ⏳ Kế tiếp |

---

## 📐 BẢN ĐỒ ÁNH XẠ CODE: TÀI LIỆU ↔ MÃ NGUỒN `core:designsystem`

Toàn bộ thông số trong thư mục tài liệu này có tính ràng buộc kỹ thuật trực tiếp với module `core:designsystem`:

```
docs/design/00_DESIGN_TOKENS_SYSTEM.md
   ├── Color Tokens      ──> core/designsystem/.../theme/Color.kt & Theme.kt
   ├── Typography Tokens ──> core/designsystem/.../theme/Type.kt
   ├── Dimension Tokens  ──> core/designsystem/.../theme/Dimensions.kt
   └── Shape Tokens      ──> core/designsystem/.../theme/Shape.kt

docs/design/01_MOTION_AND_HAPTICS.md
   ├── Shimmer Effect    ──> core/designsystem/.../component/ShimmerEffect.kt
   └── Haptic Engine     ──> core/designsystem/.../util/HapticFeedbackManager.kt

docs/design/02..10_SCREEN_*.md
   ├── Components        ──> core/designsystem/.../component/ (RestaurantCard, QuantitySelector, StateViews)
   └── Feature Screens   ──> feature:<module>/src/main/kotlin/...
```

---

## ♿ QUY CHUẨN TIẾP CẬN (ACCESSIBILITY - WCAG 2.1 AA)

- **Độ tương phản tối thiểu:**
  - Văn bản thông thường (Body/Label): Tối thiểu **4.5:1** so với màu nền.
  - Văn bản tiêu đề lớn (Headline/Display > 18sp Bold): Tối thiểu **3.0:1**.
  - Các thành phần đồ họa / Icon điều hướng: Tối thiểu **3.0:1**.
- **Kích thước vùng chạm (Touch Target Size):**  
  Tất cả các icon clickable, nút bấm, chip lựa chọn bắt buộc có kích thước tương tác tối thiểu **48x48dp**, dù kích thước hiển thị hình ảnh có thể là 24x24dp.
- **Hỗ trợ trình đọc màn hình (TalkBack):**  
  Mọi `Image`, `IconButton` bắt buộc có thuộc tính `contentDescription` ngữ nghĩa rõ ràng bằng tiếng Việt chuẩn.
