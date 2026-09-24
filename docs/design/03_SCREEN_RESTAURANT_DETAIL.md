# 🍲 03. SCREEN SPEC: RESTAURANT DETAIL & TOPPING MODAL (CHI TIẾT NHÀ HÀNG & CHỌN TOPPING)

> **Mã đặc tả:** `DESIGN_SPEC_03_RESTAURANT_DETAIL`  
> **Package mã nguồn:** `feature:restaurant` & `core:designsystem`  
> **Thành phần chính:** `RestaurantDetailScreen`, `ToppingModalBottomSheet`, `ScrollableCategoryTabs`  
> **Cập nhật:** 25/09/2026  

---

## 1. MỤC TIÊU MÀN HÌNH & USER STORY

- **Mục tiêu:** Cung cấp thông tin trực quan đầy đủ về nhà hàng (thực đơn phân loại khoa học, đánh giá, phí vận chuyển) và cho phép người dùng tùy biến món ăn (Size, Topping, Ghi chú độ cay/ít đường) qua **Modal BottomSheet 28dp** với khả năng tính toán giá tức thì.
- **User Story:**
  > *"Khi tôi bấm vào một nhà hàng, tôi muốn xem ảnh bìa quán thật hấp dẫn, dễ dàng lướt qua các nhóm món ăn (Cơm, Canh, Nước), chọn topping theo sở thích và thấy tổng tiền cập nhật ngay lập tức trước khi thêm vào giỏ."*

---

## 2. BLUEPRINT BỐ CỤC GIAO DIỆN (ASCII WIREFRAME)

```
┌──────────────────────────────────────────────────────────┐
│ [⬅️ Quay lại]        [❤️ Yêu thích]        [🔗 Chia sẻ]   │ <-- 1. Floating Top Bar
├──────────────────────────────────────────────────────────┤
│                                                          │
│           [ ẢNH HERO PARALLAX TỶ LỆ 16:9 ]               │ <-- 2. Hero Cover
│                                                          │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ Cơm Tấm Phúc Lộc Thọ - Chi Nhánh Lê Văn Việt        │ │ <-- 3. Info Surface Card
│ │ ⭐ 4.8 (500+ đánh giá) • ⚡ 20-30 phút • 1.2 km      │ │
│ │ 🏷️ Giảm 20.000đ cho đơn từ 100.000đ                   │ │
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ [ Món Bán Chạy ]  [ Cơm Tấm ]  [ Món Canh ]  [ Đồ Uống ] │ <-- 4. Sticky Category Tabs
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ Cơm Tấm Sườn Bì Chả                    [ ẢNH MÓN ]   │ │ <-- 5. Dish Item Card
│ │ Sườn nướng mật ong mềm thơm, chả trứng... 80x80dp    │ │
│ │ 55.000đ                              [ + Thêm (48dp)]│ │
│ └──────────────────────────────────────────────────────┘ │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ Cơm Tấm Ba Rọi Nướng                                 │ │
│ │ 52.000đ                              [ + Thêm (48dp)]│ │
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ [ 🛍️ Giỏ hàng • 1 món • 55.000đ            Xem giỏ > ] │ <-- 6. Sticky Floating Cart
└──────────────────────────────────────────────────────────┘
```

---

## 3. MODAL BOTTOMSHEET CHỌN TOPPING & TÙY BIẾN MÓN

Khi người dùng bấm nút `+ Thêm` trên một món ăn có nhiều tùy chọn (Size, Topping, Đá/Đường):

```
┌──────────────────────────────────────────────────────────┐
│                          ──────                          │ <-- Drag Handle (32x4dp)
│ ┌────┐  Cơm Tấm Sườn Nướng                              │
│ │ẢNH │  Đơn giá gốc: 45.000đ                           [✖]
│ └────┘                                                   │
├──────────────────────────────────────────────────────────┤
│ Chọn Kích Cỡ (Bắt buộc chọn 1)                           │
│ 🔘 Size Vừa (Mặc định)                           + 0đ   │ <-- Radio Group
│ ⚪ Size Lớn (Nhiều cơm + thêm sườn)             + 15.000đ│
├──────────────────────────────────────────────────────────┤
│ Chọn Topping Thêm (Tùy chọn)                             │
│ ☑️ Trứng ốp la lòng đào                         + 7.000đ │ <-- Checkbox Group
│ ☑️ Chả trứng hấp                                + 8.000đ │
│ ⬜ Canh khổ qua nhồi thịt                      + 15.000đ │
├──────────────────────────────────────────────────────────┤
│ Ghi Chú Cho Nhà Hàng                                     │
│ [ Ít mỡ hành, ớt để riêng... (Tối đa 150 ký tự)        ] │ <-- TextField 14sp
├──────────────────────────────────────────────────────────┤
│ [ - ]  2  [ + ]     |   Thêm vào giỏ hàng • 150.000đ     │ <-- Bottom Sticky Action
└──────────────────────────────────────────────────────────┘
```

### 3.1 Quy chuẩn kỹ thuật của BottomSheet
- **Bo góc trên:** `ShapeExtraLarge` (28dp).
- **Trạng thái trượt:** `ModalBottomSheet` với `skipPartiallyExpanded = false`.
- **Thanh điều khiển kéo (Drag Handle):** Rộng 32dp, cao 4dp, màu xám nhạt `#D1D5DB`, nằm giữa mép trên.
- **Tính toán tiền thời gian thực (Real-time Price Formula):**
  $$\text{Tổng tiền} = (\text{Giá gốc} + \sum \text{Giá size} + \sum \text{Giá topping}) \times \text{Số lượng}$$
  Mỗi lần tích checkbox hoặc đổi size, con số ở nút bấm chính cập nhật hiệu ứng nảy số (Text scale animation 1.1x -> 1.0x).

---

## 4. HIỆU ỨNG PARALLAX & CUỘN TỰ NHIÊN

1. **Collapsing Top App Bar:**
   - Khi ở đỉnh: Ảnh bìa hiển thị 100%, nút Back và nút Yêu thích có nền hình tròn màu đen mờ 50% (`CircleShape`) để luôn nhìn rõ.
   - Khi cuộn lên quá 200dp: Top bar thu gọn lại còn 56dp, ảnh mờ dần, nền Top bar chuyển thành `Surface` (Trắng hoặc Đen Charcoal), tên quán xuất hiện ở giữa Top bar (`HeadlineSmall`, 16sp SemiBold).
2. **Category Sticky Tabs (Ghim danh mục khi cuộn):**
   - Thanh Tab danh mục tự động ghim (Sticky Header) ngay dưới TopAppBar khi người dùng cuộn qua thông tin nhà hàng.
   - Khi người dùng cuộn xem món, Tab tương ứng tự động chuyển trạng thái active có chỉ báo vạch cam (`Primary`, 3dp) chạy theo vị trí cuộn.

---

## 5. CÁC TRẠNG THÁI GIAO DIỆN (STATES)

- **Đang tải (Loading):** Khung Shimmer lớn 200dp cho ảnh bìa quán + Khung Shimmer 80dp cho Card thông tin + 4 dải Shimmer món ăn dạng thẻ nằm ngang.
- **Quán đã đóng cửa (Closed State):**
  - Ảnh quán phủ một lớp màu xám đen (`Color.Black.copy(alpha = 0.5f)`).
  - Xuất hiện Banner đỏ cam: *"Nhà hàng hiện đang đóng cửa (Mở lại lúc 07:00 sáng mai)"*.
  - Nút `+ Thêm` món ăn bị vô hiệu hóa (`enabled = false`), đổi màu xám mờ.
