# 🎨 KẾ HOẠCH XÂY DỰNG TÀI LIỆU ĐẶC TẢ THIẾT KẾ GIAO DIỆN ĐỒNG BỘ (UI/UX DESIGN SYSTEM & SCREEN SPECS)

> **Mã kế hoạch:** `PLAN_20260925_ui_design_specs_architecture`  
> **Phiên bản:** 1.0 — Enterprise Grade  
> **Căn cứ đặc tả:** [bitefast_project_plan.md](../../bitefast_project_plan.md) & [AGENTS.md](../../AGENTS.md)  
> **Ngày lập:** 25/09/2026  
> **Mục tiêu:** Xây dựng hệ thống tài liệu Markdown đặc tả thiết kế UI/UX toàn diện trong thư mục `docs/design/`, đảm bảo 100% tính đồng bộ về ngôn ngữ hình ảnh (Design Tokens), hiệu ứng vi mô (Micro-interactions), bảng màu hiện đại (Modern Color Palette) và khả năng tiếp cận (Accessibility WCAG 2.1 AA) cho tất cả các màn hình trong ứng dụng BiteFast.

---

## 🏛️ CẤU TRÚC THƯ MỤC TÀI LIỆU THIẾT KẾ ĐỀ XUẤT (`docs/design/`)

```
docs/design/
├── README.md                           # Mục lục và nguyên lý thiết kế chung
├── 00_DESIGN_TOKENS_SYSTEM.md          # Màu sắc, Typography, Spacing (4/8dp), Shapes, Elevation
├── 01_MOTION_AND_HAPTICS.md            # Hiệu ứng chuyển động (Shimmer, Bounce, Haptic Feedback)
├── 02_SCREEN_DISCOVERY_HOME.md         # Trang chủ, Tìm kiếm, Danh mục, Thẻ nhà hàng
├── 03_SCREEN_RESTAURANT_DETAIL.md      # Chi tiết quán, Topping BottomSheet, Tab thực đơn
├── 04_SCREEN_CART_AND_CONFLICT.md      # Giỏ hàng, Stepper 48dp, Hộp thoại xung đột quán
├── 05_SCREEN_CHECKOUT_BIOMETRIC.md     # Thanh toán, Áp Voucher, Xác thực sinh trắc vân tay
├── 06_SCREEN_TRACKING_REALTIME.md      # Bản đồ Maps Compose, Shipper Pulse Marker, Timeline
├── 07_SCREEN_AUTH_AND_GUEST_GATE.md    # Đăng nhập, Đăng ký, LoginGateBottomSheet
├── 08_SCREEN_ORDER_HISTORY_REORDER.md  # Lịch sử đơn hàng, Nút "Đặt lại đơn" 1 chạm
├── 09_SCREEN_RATING_AND_FEEDBACK.md    # Chấm sao, Chọn tag nhanh, Đánh giá ẩn danh
└── 10_SYSTEM_STATES_AND_A11Y.md        # EmptyState, ErrorState, OfflineBanner, TalkBack
```

---

## 🎨 HỆ THỐNG DESIGN TOKENS CHUẨN HIỆN ĐẠI (MODERN FOOD DELIVERY AESTHETICS)

### 1. Bảng Màu Đồng Bộ (Color Tokens Palette)
- **Primary Brand Color (Sắc cam chủ đạo kích thích vị giác):**
  - Light Theme: Warm Coral / Vibrant Tangerine (`#FF5722`) ➔ Tạo cảm giác tươi mới, ngon miệng, năng động.
  - Dark Theme: Luminous Warm Amber (`#FF7043`) ➔ Tăng độ tương phản trên nền tối, không gây chói mắt.
- **Secondary Accent Color:** Honey Amber (`#FFA000`) cho Badge đánh giá sao và điểm thưởng.
- **Success & Freshness:** Fresh Mint Green (`#10B981`) cho trạng thái *"Đang mở cửa"*, *"Giao hàng miễn phí"*, *"Đơn thành công"*.
- **Background & Surfaces:**
  - Light: Soft Cream Porcelain (`#FDFBF7`) thay vì màu trắng bệch (`#FFFFFF`) giúp mắt dễ chịu khi duyệt thực đơn lâu.
  - Dark: Deep Obsidian Charcoal (`#121212`) và Surface Card (`#1E1E1E`) theo chuẩn Material 3 Dark Theme.
- **Tương phản văn bản (WCAG 2.1 AA):**
  - Light: On-Surface `#1A1A1A` (Tỷ lệ tương phản ~14.2:1), Muted `#666666` (~5.1:1).
  - Dark: On-Surface `#F5F5F5` (Tỷ lệ tương phản ~15.1:1), Muted `#A0A0A0` (~6.2:1).

### 2. Lưới Khoảng Cách & Hình Khối (Grid & Geometry)
- **Base 4dp Grid System:** 4dp (micro), 8dp (compact), 16dp (standard screen padding), 24dp (section gap), 32dp (hero gap).
- **Corner Radii (Độ bo góc mượt mà):**
  - Small (8dp): Cho các Tag Chip, Badge, nút số lượng nhỏ.
  - Medium (16dp): Cho Card nhà hàng, Card món ăn, Card tổng kết chi phí.
  - Large (24dp): Cho thanh tìm kiếm SearchBar, Hộp thoại Dialog.
  - Extra Large (28dp top radius): Cho toàn bộ các BottomSheet trượt từ đáy màn hình.

### 3. Hiệu Ứng Chuyển Động & Phản Hồi Xúc Giác (Motion & Haptics)
- **Skeleton Shimmer Loading:** Linear Gradient chéo chuyển động lặp vô hạn (Tween 1200ms `LinearEasing`), phủ lên khung xương xám mờ thay thế toàn bộ vòng xoay tròn loading nhàm chán.
- **Haptic Micro-feedback:**
  - `HapticFeedbackType.LongPress`: Khi bấm nút "Thêm vào giỏ", áp mã giảm giá thành công hoặc trượt Pull-to-refresh vượt ngưỡng.
  - `HapticFeedbackType.TextHandleMove`: Khi bấm nút tăng/giảm (+/-) trong QuantitySelector.
- **Smooth State Animations:**
  - Thanh trạng thái kết nối `OfflineBanner`: Trượt xuống màu cam khi mất mạng, chuyển xanh 2 giây khi kết nối lại rồi trượt ẩn.
  - Shipper Marker: Xoay vòng theo góc di chuyển (bearing angle) và nội suy tọa độ trơn tru (Smooth Polyline Interpolation).

---

## 📋 NỘI DUNG CHI TIẾT CỦA CÁC FILE ĐẶC TẢ GIAO DIỆN (SCREEN SPECS)

Mỗi file màn hình trong `docs/design/` sẽ tuân thủ cấu trúc chuẩn mực:
1. **Mục đích màn hình & User Story chính.**
2. **Layout Blueprint (Sơ đồ bố cục UI bằng Mermaid / ASCII Wireframe).**
3. **Chi tiết từng thành phần UI (Components, Spacing, Color tokens áp dụng).**
4. **Các trạng thái giao diện bắt buộc (States):**
   - *Loading State* (Shimmer Skeleton tương ứng với từng phần tử).
   - *Success / Content State* (Giao diện hiển thị đầy đủ).
   - *Empty State* (Trống dữ liệu kèm hình minh họa và CTA gợi ý).
   - *Error State* (Lỗi mạng hoặc server kèm Actionable Retry Button).
5. **Hiệu ứng vi mô & Haptics tương ứng trên từng nút bấm.**
6. **Tiêu chuẩn Accessibility (A11y TalkBack Description, Touch Target ≥ 48dp).**

---

## ⚖️ ĐỀ XUẤT PHƯƠNG ÁN THỰC THI (THEO ĐIỀU 4 CỦA AGENTS.MD)

### 🔹 Phương án 1 (Khuyến nghị - Recommended): Khởi tạo Design Tokens & Màn Hình Mẫu Trước
- **Cách làm:**
  1. Tạo thư mục `docs/design/`.
  2. Viết 2 file nền tảng cốt lõi: `00_DESIGN_TOKENS_SYSTEM.md` (Toàn bộ mã màu, font chữ, khoảng cách, radius) và `01_MOTION_AND_HAPTICS.md`.
  3. Viết file đặc tả chi tiết mẫu đầu tiên: `02_SCREEN_DISCOVERY_HOME.md`.
  4. Trình bạn xem xét, điều chỉnh phong cách màu sắc/hiệu ứng cho vừa ý bạn nhất.
  5. Sau khi bạn chốt phong cách chuẩn, tiến hành tạo đồng loạt các file màn hình còn lại theo đúng chuẩn đã chốt.
- **Ưu điểm:** Đảm bảo bạn kiểm soát được "gu" thẩm mỹ (màu sắc, độ bo góc, ánh sáng shimmer) ngay từ đầu, tránh việc viết 10 file xong mới phải sửa hàng loạt.

### 🔹 Phương án 2: Tạo đồng loạt toàn bộ 10 file đặc tả UI Spec ngay lập tức
- **Cách làm:** AI sẽ tự động sinh toàn bộ 10 file đặc tả từ màn hình 01 đến 10 dựa trên bản đặc tả 10/10 Enterprise hiện tại.
- **Ưu điểm:** Đầy đủ tài liệu ngay trong một lần thực hiện.
- **Nhược điểm:** Dung lượng tài liệu rất lớn, nếu bạn muốn đổi một tông màu chính (ví dụ từ cam san hô sang cam cháy) thì sẽ phải chỉnh sửa lại toàn bộ các file.

👉 **Bạn muốn chúng ta triển khai theo Phương án 1 (Làm chuẩn Tokens + Trang chủ trước để duyệt gu thẩm mỹ) hay Phương án 2 (Tạo toàn bộ 10 file cùng lúc)?**
