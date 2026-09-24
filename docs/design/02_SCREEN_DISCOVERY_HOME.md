# 🏠 02. SCREEN SPEC: DISCOVERY HOME (MÀN HÌNH KHÁM PHÁ TRANG CHỦ)

> **Mã đặc tả:** `DESIGN_SPEC_02_DISCOVERY_HOME`  
> **Package mã nguồn:** `feature:discovery` & `app:navigation`  
> **Composable chính:** [DiscoveryScreen.kt](file:///d:/Personal_Project/BiteFast/feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryScreen.kt)  
> **ViewModel:** [DiscoveryViewModel.kt](file:///d:/Personal_Project/BiteFast/feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryViewModel.kt)  
> **Cập nhật:** 25/09/2026  

---

## 1. MỤC TIÊU MÀN HÌNH & USER STORY

- **Mục tiêu:** Là màn hình cửa ngõ chính của BiteFast, giúp người dùng tìm thấy món ăn họ thèm trong vòng **dưới 10 giây** thông qua định vị địa chỉ giao hàng, tìm kiếm thông minh, duyệt danh mục trực quan và danh sách quán ăn gợi ý được cá nhân hóa.
- **User Story:**
  > *"Là một người dùng đang đói bụng, tôi muốn mở app là thấy ngay địa chỉ giao hàng của mình, tìm nhanh quán ăn gần nhất, thấy rõ đánh giá sao, thời gian giao hàng và các ưu đãi hấp dẫn để quyết định chọn quán ăn nhanh chóng."*

---

## 2. BLUEPRINT BỐ CỤC GIAO DIỆN (ASCII WIREFRAME)

```
┌──────────────────────────────────────────────────────────┐
│ [📍 123 Nguyễn Huệ, Q.1 ▼]                 [🔔 (Badge)] │ <-- 1. Location Bar
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ 🔍 Bạn thèm món gì hôm nay?                     [⚙️] │ │ <-- 2. Search & Filter
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ (🍔 Burger) (🍜 Phở) (🍕 Pizza) (🧋 Trà sữa) (🍣 Sushi)  │ <-- 3. Category Chips
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ 🎁 BANNER PROMO: GIẢM 50K ĐƠN ĐẦU TIÊN (16:9 Card)  │ │ <-- 4. Hero Carousel
│ └──────────────────────────────────────────────────────┘ │
│                          ○ ● ○ ○                         │
├──────────────────────────────────────────────────────────┤
│ 🔥 Quán Ngon Gần Bạn                           Xem tất cả│ <-- 5. Section Header
│ ┌──────────────────────────────────────────────────────┐ │
│ │ ┌──────────────────────────────────────────────────┐ │ │
│ │ │ [ ẢNH NHÀ HÀNG TỶ LỆ 16:9 ]       [❤️ Yêu thích]  │ │ │
│ │ │ [⚡ 20-30 phút]                   [🏷️ FREESHIP]   │ │ │
│ │ └──────────────────────────────────────────────────┘ │ │
│ │  Cơm Tấm Phúc Lộc Thọ                                │ │ <-- 6. Restaurant Card
│ │  ⭐ 4.8 (500+ đánh giá) • 1.2 km • Phí ship: 15.000đ │ │
│ └──────────────────────────────────────────────────────┘ │
│ ┌──────────────────────────────────────────────────────┐ │
│ │  Phở Thìn Lò Đúc - Bò Tái Lăn                        │ │
│ │  ⭐ 4.7 (320+ đánh giá) • 2.5 km • Phí ship: 18.000đ │ │
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ [ 🛍️ 2 món trong giỏ • 95.000đ            Xem giỏ hàng > ] │ <-- 7. Sticky Cart Bar
└──────────────────────────────────────────────────────────┘
```

---

## 3. CHI TIẾT TỪNG THÀNH PHẦN UI & TOKENS ÁP DỤNG

### 3.1 Location Header (Thanh địa chỉ giao hàng)
- **Cấu trúc:** Icon `LocationOn` (`#FF5722`), Text địa chỉ cắt ngắn (`TextPrimary`, 14sp SemiBold), Icon mũi tên trỏ xuống (`ExpandMore`), và Icon Chuông thông báo phía đối diện.
- **Kích thước tương tác:** Toàn bộ cụm chọn địa chỉ có chiều cao 48dp, padding ngang 16dp.
- **Hành vi khi chạm:** Bật `AddressSelectionBottomSheet` để đổi địa chỉ nhận hàng.

### 3.2 Sticky Search Bar (Thanh tìm kiếm)
- **Nền:** `SurfaceVariant` (`#F4EFE6` Light / `#2A2A2A` Dark).
- **Bo góc:** `ShapeLarge` (24dp hình viên thuốc).
- **Chiều cao:** 52dp.
- **Icon:** Kính lúp bên trái, Icon bộ lọc nâng cao (`FilterList`) bên phải với touch target 48x48dp.
- **Placeholder:** *"Bạn thèm món gì hôm nay?"* (`TextTertiary`, 14sp Regular).

### 3.3 Category Chips Carousel (Băng chuyền danh mục)
- **Kiểu cuộn:** `LazyRow` với `contentPadding = PaddingValues(horizontal = 16.dp)`, khoảng cách giữa các chip: `8.dp`.
- **Trạng thái Chip:**
  - *Chưa chọn:* Nền `Surface`, viền mờ 1dp `SurfaceVariant`, chữ `TextPrimary` (12sp Medium).
  - *Đang chọn:* Nền `PrimaryContainer` (`#FFEDE6`), chữ và icon màu `Primary` (`#FF5722`), viền `Primary` 1.5dp.
- **Hiệu ứng:** Bấm chọn kích hoạt `HapticFeedbackType.TextHandleMove` (nhẹ).

### 3.4 Hero Promo Banner (Băng chuyền khuyến mãi)
- **Tỷ lệ khung hình:** 16:9, bo góc `ShapeMedium` (16dp).
- **Tự động trượt:** Tự động chuyển slide sau mỗi 4000ms nếu người dùng không chạm tay vào màn hình.
- **Chỉ báo trang (Dots):** 4 chấm tròn (Active dài 16dp dạng con nhộng màu `Primary`, Inactive tròn 6dp màu xám nhạt).

### 3.5 Restaurant Card (Thẻ nhà hàng chuẩn - Component tái sử dụng)
Ánh xạ trực tiếp từ component [RestaurantCard.kt](file:///d:/Personal_Project/BiteFast/core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/RestaurantCard.kt):
- **Bìa ảnh (Cover):** Tỷ lệ 16:9 bo góc trên 16dp, tích hợp lớp gradient tối nhẹ ở chân ảnh để nổi bật badge thời gian giao hàng.
- **Badges trên ảnh:**
  - Góc trái dưới: Thời gian giao hàng `⚡ 20 - 30 phút` (Nền đen trong suốt 60%, chữ trắng 12sp SemiBold).
  - Góc phải trên: Badge ưu đãi `FREESHIP` hoặc `GIẢM 20%` (Nền `Primary`, chữ trắng).
- **Thông tin bên dưới ảnh:**
  - Tên nhà hàng: `TitleLarge` (18sp Bold, `TextPrimary`, tối đa 1 dòng, chấm lửng nếu quá dài).
  - Hàng thông tin phụ: Icon Sao vàng (`#FFA000`) + Số sao `4.8` (Bold) + `(500+)` (`TextSecondary`) • Khoảng cách `1.2 km` • Phí giao hàng `15.000đ`.

### 3.6 Floating Bottom Cart Bar (Thanh giỏ hàng nổi chân trang)
- **Hiển thị có điều kiện:** Chỉ trượt xuất hiện khi giỏ hàng có `totalQuantity > 0`.
- **Hiệu ứng xuất hiện:** `AnimatedVisibility` với `slideInVertically(initialOffsetY = { it }) + fadeIn()`.
- **Màu nền:** `Primary` (`#FF5722`), chữ trắng toàn bộ.
- **Nội dung:** Icon túi đồ ăn + Số lượng món + Tổng tiền tạm tính + Nút mũi tên `>` dẫn sang [CartScreen.kt](file:///d:/Personal_Project/BiteFast/feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartScreen.kt).

---

## 4. MA TRẬN 4 TRẠNG THÁI GIAO DIỆN (SCREEN STATES)

```
        ┌───────────────────────────────────────────────┐
        │                 DISCOVERY STATE               │
        └───────────────────────┬───────────────────────┘
            ┌───────────────────┼───────────────────┐
            ▼                   ▼                   ▼
    ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
    │ LOADING STATE │   │ CONTENT STATE │   │  ERROR STATE  │
    │  (3x Shimmer) │   │ (Pull-Refresh)│   │ (Retry Action)│
    └───────────────┘   └───────────────┘   └───────────────┘
```

### 4.1 Loading State (Shimmer Skeleton)
- Khi `DiscoveryUiState.isLoading == true`:
  - 1 dải Shimmer giả lập thanh tìm kiếm (chiều cao 52dp, bo góc 24dp).
  - 1 hàng 5 hình tròn Shimmer giả lập danh mục (đường kính 48dp).
  - 1 khung Shimmer chữ nhật tỷ lệ 16:9 giả lập Banner (chiều cao 140dp, bo góc 16dp).
  - 3 thẻ Shimmer nhà hàng lớn (Khung ảnh 160dp + 2 dòng chữ xám nhạt).
- **Tuyệt đối không giật layout** (Kích thước shimmer khớp 100% với kích thước thẻ thật khi tải xong).

### 4.2 Content State (Hiển thị đầy đủ)
- Hỗ trợ thao tác vuốt từ trên xuống để làm mới (`PullRefreshIndicator`).
- Khi vuốt vượt ngưỡng kéo: Kích hoạt rung haptic nhẹ, hiển thị vòng cung cam quay mượt.

### 4.3 Empty State (Không tìm thấy kết quả)
- Khi tìm kiếm với từ khóa không có quán phù hợp:
  - Hiển thị hình minh họa đĩa ăn trống rỗng dễ thương.
  - Tiêu đề: *"Không tìm thấy món bạn yêu cầu"*.
  - Gợi ý: *"Hãy thử tìm 'Cơm tấm', 'Trà sữa' hoặc xóa bộ lọc xem sao nhé!"*.
  - Nút CTA: *"Xóa bộ lọc tìm kiếm"* (`ButtonDefaults.outlinedButtonColors`).

### 4.4 Error State (Lỗi kết nối / Máy chủ)
- Khi mất mạng hoặc server lỗi:
  - Biểu tượng đám mây gạch chéo (`CloudOff`) màu cam đất.
  - Tiêu đề: *"Đã xảy ra lỗi kết nối"*.
  - Phụ đề: *"Không thể tải danh sách nhà hàng. Vui lòng kiểm tra lại kết nối Wi-Fi hoặc 4G."*.
  - Nút CTA chính: *"Thử lại"* (Chiều cao 48dp, nền `Primary`).

---

## 5. TIÊU CHUẨN TIẾP CẬN & ACCESSIBILITY (WCAG 2.1 AA)

- `IconButton` Chuông thông báo: `contentDescription = "Thông báo đơn hàng và ưu đãi"`.
- `RestaurantCard`: Toàn bộ thẻ là 1 `clickable` modifier với `Role.Button`, đọc tổng hợp:  
  `contentDescription = "Nhà hàng ${restaurant.name}, đánh giá ${restaurant.rating} sao, khoảng cách ${restaurant.distanceKm} kilômét, thời gian giao dự kiến ${restaurant.deliveryTimeMinutes} phút"`.
- Đảm bảo TalkBack không bị kẹt khi cuộn danh sách vô hạn bằng thuộc tính `semantics { scrollAxisRange = ... }`.
