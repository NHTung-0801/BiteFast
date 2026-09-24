# ⭐ 09. SCREEN SPEC: RATING & FEEDBACK (ĐÁNH GIÁ & PHẢN HỒI MÓN ĂN)

> **Mã đặc tả:** `DESIGN_SPEC_09_RATING_FEEDBACK`  
> **Package mã nguồn:** `feature:order` & `core:designsystem`  
> **Thành phần chính:** `RatingFeedbackScreen`, `InteractiveStarBar`, `QuickReviewChips`  
> **Cập nhật:** 25/09/2026  

---

## 1. MỤC TIÊU MÀN HÌNH & USER STORY

- **Mục tiêu:** Thu thập đánh giá chân thực của khách hàng về chất lượng món ăn và tài xế trong **dưới 15 giây** thông qua thanh 5 sao có phản hồi xúc giác sống động, bộ thẻ chọn cảm nhận nhanh (Quick Feedback Chips) và tùy chọn đánh giá ẩn danh.
- **User Story:**
  > *"Sau khi thưởng thức bữa ăn ngon miệng, tôi muốn chấm 5 sao nhanh chóng, chọn vài điểm khen ngợi như 'Đồ ăn nóng hổi', 'Giao siêu nhanh' để ủng hộ quán ăn."*

---

## 2. BLUEPRINT BỐ CỤC GIAO DIỆN (ASCII WIREFRAME)

```
┌──────────────────────────────────────────────────────────┐
│ [✖ Đóng]             ĐÁNH GIÁ ĐƠN HÀNG                   │ <-- Top Bar
├──────────────────────────────────────────────────────────┤
│ 🏪 Cơm Tấm Phúc Lộc Thọ - Lê Văn Việt                    │
│                                                          │
│              Bữa ăn của bạn hôm nay thế nào?             │
│                                                          │
│             ⭐    ⭐    ⭐    ⭐    ⭐                  │ <-- 5 Interactive Stars
│                     ( Tuyệt vời! )                       │ <-- Dynamic Vibe Label
├──────────────────────────────────────────────────────────┤
│ Bạn ấn tượng điều gì nhất? (Chọn nhiều thẻ)              │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ [🔥 Đồ ăn nóng hổi]    [⚡ Giao hàng siêu tốc]       │ │ <-- Quick Review Chips
│ │ [📦 Đóng gói cẩn thận] [🍽️ Nêm nếm chuẩn vị]        │ │
│ │ [✨ Món ăn đúng mô tả] [🥬 Rau tươi sạch sẽ]         │ │
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────────┐ │
│ │ Chia sẻ thêm cảm nhận của bạn... (Tùy chọn)          │ │ <-- Text Area (100dp)
│ └──────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────┤
│ 📷 Thêm hình ảnh món ăn thực tế (Tối đa 3 ảnh) [ + ]     │ <-- Photo Upload Grid
├──────────────────────────────────────────────────────────┤
│ 🔒 Đánh giá ẩn danh (Che tên và avatar của bạn)   [ ON ] │ <-- Switch
├──────────────────────────────────────────────────────────┤
│ [ GỬI ĐÁNH GIÁ (Nhận ngay 100 điểm thưởng)             ] │ <-- Primary CTA
└──────────────────────────────────────────────────────────┘
```

---

## 3. HIỆU ỨNG TƯƠNG TÁC SAO (INTERACTIVE STAR BAR)

- **Kích thước ngôi sao:** 40x40dp, khoảng cách giữa các sao: 12dp.
- **Phản hồi khi chạm hoặc vuốt (Drag to Rate):**
  - Ngôi sao được chọn nảy lên (`scale: 1.35f -> 1.0f`) bằng hiệu ứng `Spring(dampingRatio = 0.5f)`.
  - Màu sắc: Màu vàng mật ong `Secondary` (`#FFA000`).
  - Phản hồi rung: Mỗi khi mức sao tăng/giảm, phát rung nhẹ `HapticFeedbackType.TextHandleMove`.
  - Nhãn cảm xúc tương ứng:
    - 1 sao: *"Rất thất vọng"* (`#EF4444`)
    - 2 sao: *"Chưa hài lòng"* (`#F59E0B`)
    - 3 sao: *"Bình thường"* (`#666666`)
    - 4 sao: *"Ngon miệng"* (`#10B981`)
    - 5 sao: *"Tuyệt vời!"* (`#FFA000`)
