# 🎨 00. DESIGN TOKENS SYSTEM (HỆ THỐNG NGUYÊN TỬ THIẾT KẾ)

> **Mã đặc tả:** `DESIGN_SPEC_00_TOKENS`  
> **Áp dụng cho:** Toàn bộ ứng dụng BiteFast (`core:designsystem`, `feature:*`, `app`)  
> **Tiêu chuẩn:** Material Design 3 (M3) & WCAG 2.1 AA  
> **Cập nhật:** 25/09/2026  

---

## 1. HỆ THỐNG MÀU SẮC ĐỒNG BỘ (COLOR PALETTE)

Hệ màu của BiteFast được tuyển chọn kỹ lưỡng để mang lại cảm giác ấm áp, tươi mới, ngon miệng (Culinary Appetite) nhưng không gây chói mắt khi sử dụng vào ban đêm.

### 1.1 Bảng màu Thương hiệu & Điểm nhấn (Brand & Accent)

| Tên Token | Light Theme | Dark Theme | Mô tả vai trò & Vị trí sử dụng |
| :--- | :--- | :--- | :--- |
| **`Primary`** | `#FF5722` (Tangerine Coral) | `#FF7043` (Luminous Amber) | Màu nhận diện chủ đạo: Nút CTA chính ("Đặt hàng", "Thanh toán"), Icon active |
| **`OnPrimary`** | `#FFFFFF` | `#FFFFFF` | Chữ hoặc biểu tượng nằm trên nền màu `Primary` |
| **`PrimaryContainer`** | `#FFEDE6` | `#3D1B11` | Nền nhẹ của thẻ voucher, badge giảm giá, thông báo ưu đãi |
| **`OnPrimaryContainer`**| `#801D00` | `#FFCCBC` | Chữ và icon nằm trên nền `PrimaryContainer` |
| **`Secondary`** | `#FFA000` (Honey Amber) | `#FFB300` (Bright Amber) | Điểm nhấn phụ: Đánh giá sao ⭐, điểm tích lũy thành viên, tag món "Best Seller" |
| **`OnSecondary`** | `#FFFFFF` | `#261900` | Chữ nằm trên nền `Secondary` |

### 1.2 Bảng màu Nền & Bề mặt (Background & Surfaces)

| Tên Token | Light Theme | Dark Theme | Mô tả vai trò & Vị trí sử dụng |
| :--- | :--- | :--- | :--- |
| **`Background`** | `#FDFBF7` (Soft Porcelain) | `#121212` (Deep Obsidian) | Nền tổng thể của toàn màn hình (thay thế màu trắng gắt để mắt dịu hơn) |
| **`Surface`** | `#FFFFFF` (Pure White) | `#1E1E1E` (Charcoal Surface)| Bề mặt thẻ nhà hàng, thẻ món ăn, card thông tin đơn hàng |
| **`SurfaceVariant`**| `#F4EFE6` (Warm Muted) | `#2A2A2A` (Muted Dark) | Bề mặt thanh tìm kiếm, divider ngăn cách, nền chip chưa chọn |
| **`SurfaceLevel1`** | `#FFFFFF` | `#232323` | Card nổi 1 cấp (Elevation 1-2dp) |
| **`SurfaceLevel2`** | `#FFFFFF` | `#2C2C2C` | BottomSheet nổi 2 cấp (Elevation 4-8dp) |

### 1.3 Bảng màu Văn bản (Typography Contrast)

Tuân thủ nghiêm ngặt chuẩn WCAG 2.1 AA (Tỷ lệ tương phản tối thiểu 4.5:1 với văn bản nội dung):

| Tên Token | Light Hex | Light Ratio | Dark Hex | Dark Ratio | Vai trò sử dụng |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **`TextPrimary`** | `#1A1A1A` | **15.2:1** | `#F5F5F5` | **16.1:1** | Tên món, tên quán, giá tiền, tiêu đề chính |
| **`TextSecondary`**| `#666666` | **5.4:1** | `#A0A0A0` | **6.5:1** | Mô tả món, danh mục phụ, thời gian giao hàng |
| **`TextTertiary`** | `#8E8E93` | **3.2:1** | `#757575` | **3.8:1** | Placeholder text, nhãn trạng thái đã hoàn tất |
| **`TextDisabled`** | `#BCBCBC` | N/A | `#4E4E4E` | N/A | Các thành phần hoặc nút bấm bị vô hiệu hóa |

### 1.4 Bảng màu Trạng thái Nghiệp vụ (Semantic / Status)

| Trạng thái | Hex Light | Hex Dark | Ý nghĩa nghiệp vụ |
| :--- | :--- | :--- | :--- |
| **`Success`** | `#10B981` (Fresh Mint) | `#34D399` | Quán "Đang mở cửa", Đơn hàng "Giao thành công", Freeship |
| **`Error`** | `#EF4444` (Vivid Crimson) | `#F87171` | Lỗi mạng, Hết món, Cảnh báo xung đột quán ăn trong giỏ hàng |
| **`Warning`** | `#F59E0B` (Vibrant Amber) | `#FBBF24` | Quán "Sắp đóng cửa trong 15 phút", Món ăn "Còn lại dưới 3 phần" |
| **`Info`** | `#3B82F6` (Electric Blue) | `#60A5FA` | Gợi ý mẹo tiết kiệm, Vị trí hiện tại của tài xế shipper |

---

## 2. HỆ THỐNG TYPOGRAPHY (KIỂU CHỮ & CỠ CHỮ)

- **Phông chữ chủ đạo:** `Plus Jakarta Sans` (hoặc fallback `Inter` / `Roboto`).  
- Tối ưu hóa kích thước chữ và khoảng cách dòng (`LineHeight`) đảm bảo không bị cắt chữ khi người dùng phóng to cỡ chữ hệ thống Android.

```
Display Large    34sp / 40sp line-height / Bold (700)      -- Banners khuyến mãi Hero
Headline Large   24sp / 30sp line-height / SemiBold (600)  -- Tên nhà hàng, Tiêu đề màn hình
Headline Medium  20sp / 26sp line-height / SemiBold (600)  -- Tiêu đề phân mục trang chủ
Title Large      18sp / 24sp line-height / Medium (500)    -- Tên món ăn trong danh sách
Title Medium     16sp / 22sp line-height / Medium (500)    -- Tiêu đề nhóm topping, Giá tiền món
Body Large       16sp / 24sp line-height / Regular (400)   -- Mô tả chi tiết món ăn, Địa chỉ nhận hàng
Body Medium      14sp / 20sp line-height / Regular (400)   -- Ghi chú cho quán, Mô tả ngắn
Label Large      14sp / 18sp line-height / SemiBold (600)  -- Chữ trên nút bấm CTA chính
Label Medium     12sp / 16sp line-height / Medium (500)    -- Tag món ăn, Khoảng cách (km), Đánh giá (4.8)
Label Small      11sp / 14sp line-height / Regular (400)   -- Thời gian cập nhật đơn hàng, Disclaimer
```

---

## 3. LƯỚI KHOẢNG CÁCH (SPACING & PADDING - 4DP / 8DP GRID)

Tuyệt đối **không dùng** các giá trị padding ngẫu nhiên (như 7dp, 13dp, 19dp). Mọi khoảng cách đều tuân thủ bội số của 4dp/8dp:

```
SpaceNone        = 0dp
SpaceExtraSmall  = 4dp   -- Khoảng cách giữa icon và text nhỏ (Icon sao và 4.8)
SpaceSmall       = 8dp   -- Padding nội bộ trong Chip, khoảng cách giữa các badge
SpaceMedium      = 12dp  -- Khoảng cách giữa các item trong danh sách rút gọn
SpaceStandard    = 16dp  -- Margin 2 bên viền màn hình (Standard Screen Padding)
SpaceLarge       = 20dp  -- Khoảng cách giữa ảnh món ăn và nội dung text
SpaceExtraLarge  = 24dp  -- Khoảng cách phân tách giữa các Section trang chủ
SpaceHuge        = 32dp  -- Khoảng trống đầu trang (Hero padding)
SpaceGigantic    = 48dp  -- Chiều cao tối thiểu của nút bấm tương tác (Touch Target)
```

---

## 4. HỆ THỐNG BO GÓC (SHAPES & CORNER RADII)

| Token Shape | Bán kính Bo | Ứng dụng cụ thể trong BiteFast |
| :--- | :---: | :--- |
| **`ShapeSmall`** | `8dp` | Tag phân loại món, Badge giảm giá, Nút Stepper số lượng (+/-) |
| **`ShapeMedium`** | `16dp` | Thẻ nhà hàng (RestaurantCard), Thẻ món ăn (DishCard), Thẻ voucher |
| **`ShapeLarge`** | `24dp` | Thanh tìm kiếm SearchBar, Hộp thoại Dialog xác nhận |
| **`ShapeExtraLarge`** | `28dp` *(Top only)* | **Tất cả BottomSheet** trượt từ dưới lên (Topping, Giỏ hàng, Đăng nhập) |
| **`ShapeFull`** | `999dp` | Nút bấm con nhộng (Pill CTA), Avatar người dùng, Nút quay lại (Back Button) |

---

## 5. HỆ THỐNG ĐỘ NỔI VÀ ĐỔ BÓNG (ELEVATION & SHADOWS)

- **Level 0 (Flat - 0dp):** Nền màn hình, thẻ danh mục dạng phẳng không đổ bóng.
- **Level 1 (Card Default - 2dp, Tonal Elevation 1dp):** Thẻ nhà hàng, thẻ món ăn trong danh sách cuộn.
- **Level 2 (Active/Pressed - 4dp):** Thẻ khi người dùng nhấn giữ (Press feedback state).
- **Level 3 (Sticky Bottom Bar - 8dp + Mờ mờ Ambient Shadow):** Thanh giỏ hàng nổi chân trang (Floating Cart Sticky Bar).
- **Level 4 (Modal Surfaces - 16dp):** Modal BottomSheet cấu hình topping món ăn, Dialog xung đột giỏ hàng.

---

## 6. MẪU KHO CHUẨN KHO HÓA TRONG KOTLIN COMPOSE

Tài liệu này được triển khai đồng bộ bằng các file mã nguồn:
- **Màu sắc:** [Color.kt](file:///d:/Personal_Project/BiteFast/core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/theme/Color.kt)
- **Theme M3:** [Theme.kt](file:///d:/Personal_Project/BiteFast/core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/theme/Theme.kt)
