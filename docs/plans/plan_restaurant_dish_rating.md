# ⭐ Kế Hoạch Đánh Giá Số Sao Riêng Cho Từng Món Ăn (Dish Rating Feature)

> **Mã kế hoạch:** `plan_restaurant_dish_rating.md`  
> **Ngày cập nhật:** 01/10/2026 | **Phiên bản:** 2.0 (Kết hợp Phương án 1 + 2)  
> **Tuân thủ quy chuẩn:** `AGENTS.md` (Clean Architecture, Plan-First)

---

## 🎯 1. MỤC TIÊU & PHẠM VI (OBJECTIVE & SCOPE)

### 1.1 Mục tiêu
Xây dựng tính năng **Đánh giá số sao riêng biệt cho từng món ăn** ngay tại trang Chi tiết món ăn / thực đơn:
1. **Hiển thị số sao của từng món ăn**: Cả trên danh sách thực đơn (`MenuItemRow`) và trong BottomSheet chi tiết món (`CustomizationSheetContent`).
2. **Tương tác đánh giá trực tiếp**: Khi người dùng nhấn vào phần số sao hoặc nút "Đánh giá ngay" của món ăn đó, một **DishRatingBottomSheet** trượt lên với:
   - Thông tin cụ thể của món ăn (Ảnh + Tên món).
   - Thanh 5 ngôi sao tương tác lớn (1 ➔ 5 sao) kèm nhãn cảm xúc ("Tệ", "Bình thường", "Ngon", "Rất ngon", "Tuyệt hảo!").
   - Các tags đánh giá nhanh dành riêng cho món ăn ("Đậm đà chuẩn vị", "Thịt tươi mềm", "Nước sốt ngon", "Khẩu phần nhiều", "Trình bày đẹp").
   - Khung nhận xét tùy chọn.
   - Nút "Gửi đánh giá" với phản hồi tức thì và cập nhật số sao/lượt đánh giá của món ăn đó ngay trên giao diện.

### 1.2 Phạm vi tác động
- **`:core:model`**:
  - `Model.kt`: Bổ sung trường `rating: Double = 4.8` và `reviewCount: Int = 50` cho `MenuItem` (có giá trị mặc định để tương thích 100% với code hiện có).
- **`:feature:detail`**:
  - `DetailViewModel.kt`: Bổ sung state, event và effect quản lý việc đánh giá món ăn và lưu trữ điểm số đã cập nhật (`menuItemRatings`).
  - `DetailScreen.kt`: Thêm component `InteractiveStarRatingBar`, hiển thị rating trên `MenuItemRow` và `CustomizationSheetContent`, xây dựng `DishRatingBottomSheet`.

---

## 🗺️ 2. ĐỒ THỊ ẢNH HƯỞNG MODULE (MODULE IMPACT GRAPH)

```
   [:feature:detail] ──► [:core:model]
          │
          ▼
   [:core:common] (UiEvent, UiEffect, UiState, BaseViewModel)
```
- Phân tầng đúng chiều: Domain/Model độc lập, Feature Presentation phụ thuộc Model và Common.

---

## 📋 3. PHÂN RÃ CÔNG VIỆC CHI TIẾT (TASK BREAKDOWN)

### Task 1 (Size: S): Mở Rộng Model `MenuItem`
- **Mô tả:** Thêm `rating: Double = 4.8` và `reviewCount: Int = 50` vào `MenuItem` trong `core/model/src/main/kotlin/com/bitefast/core/model/Model.kt`.
- **Tiêu chí nghiệm thu:** Build thành công, toàn bộ các use cases và repository cũ không bị ảnh hưởng.
- **Files:** `core/model/src/main/kotlin/com/bitefast/core/model/Model.kt`.

### Task 2 (Size: M): Xử Lý Logic Đánh Giá Món Ăn Trong `DetailViewModel`
- **Mô tả:**
  - Thêm vào `DetailUiState`: `showDishRatingSheet: Boolean`, `ratingMenuItem: MenuItem?`, `selectedStars: Int`, `ratingTags: Set<String>`, `ratingComment: String`, `menuItemRatings: Map<String, Pair<Double, Int>>`.
  - Thêm các `DetailUiEvent`: `OpenDishRating(item)`, `CloseDishRating`, `SelectStars(stars)`, `ToggleRatingTag(tag)`, `UpdateRatingComment(text)`, `SubmitDishRating`.
  - Khi submit: Tính toán điểm trung bình mới `(cũ * count + mới) / (count + 1)`, tăng `reviewCount + 1`, lưu vào state `menuItemRatings` và gửi `ShowSnackbar("Cảm ơn bạn đã đánh giá món [Tên món]!")`.
- **Files:** `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailViewModel.kt`.

### Task 3 (Size: M): Thiết Kế Giao Diện Đánh Giá Món Ăn Trong `DetailScreen`
- **Mô tả:**
  - `InteractiveStarRatingBar`: 5 ngôi sao lớn, chạm vào sao nào thì số sao đó và sao trước nó sáng vàng kèm hiệu ứng nảy nhẹ.
  - Cập nhật `MenuItemRow`: Hiển thị badge `⭐ 4.8 (50)`.
  - Cập nhật `CustomizationSheetContent`: Hiển thị dòng đánh giá có thể bấm `⭐ 4.8 (50 đánh giá) • Đánh giá ngay`.
  - `DishRatingBottomSheet`: Sheet đánh giá nổi bật chuyên cho món ăn đang chọn.
- **Files:** `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailScreen.kt`.

---

## 🔒 4. KIỂM THỬ & NGHIỆM THU
- Chạy `./gradlew assembleDebug` đảm bảo biên dịch 100% xanh.
