# ♿ 10. SYSTEM STATES & ACCESSIBILITY (TRẠNG THÁI HỆ THỐNG & TRUY CẬP WCAG)

> **Mã đặc tả:** `DESIGN_SPEC_10_SYSTEM_STATES_A11Y`  
> **Package mã nguồn:** `core:designsystem`  
> **Thành phần chính:** [StateViews.kt](file:///d:/Personal_Project/BiteFast/core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/StateViews.kt), `OfflineBanner`, `TalkBackSemantics`  
> **Cập nhật:** 25/09/2026  

---

## 1. NGUYÊN TẮC HỆ THỐNG TRẠNG THÁI (SYSTEM STATES STANDARD)

Trong BiteFast, mọi màn hình bắt buộc phải xử lý trọn vẹn **4 trạng thái cơ bản**:

```
        ┌────────────────────────────────────────────────────────┐
        │                 SCREEN STATE ARCHITECTURE              │
        └───────────────────────────┬────────────────────────────┘
            ┌───────────────────────┼────────────────────────┐
            ▼                       ▼                        ▼
    ┌───────────────┐       ┌───────────────┐        ┌───────────────┐
    │ LOADING STATE │       │  EMPTY STATE  │        │  ERROR STATE  │
    │ (Shimmer 100%)│       │(Illustr + CTA)│        │(Action Retry) │
    └───────────────┘       └───────────────┘        └───────────────┘
```

### 1.1 Chuẩn hóa Empty State (Khi không có dữ liệu)
- **Cấu trúc bắt buộc:**
  1. Hình minh họa theo chủ đề (Illustration đường nét tối giản, màu ấm).
  2. Tiêu đề rõ ràng: Ví dụ *"Giỏ hàng của bạn đang trống"* (`HeadlineMedium`, 20sp SemiBold).
  3. Giải thích ngắn gọn: *"Hãy lướt menu để chọn những món ăn ngon lành nhé!"* (`TextSecondary`, 14sp Regular).
  4. Nút hành động kêu gọi (Actionable CTA): Chiều cao tối thiểu 48dp, ví dụ *"Khám phá món ngon ngay"*.

### 1.2 Chuẩn hóa Error State (Khi có sự cố kỹ thuật)
- **Nguyên tắc:** Không bao giờ hiển thị mã lỗi kỹ thuật khó hiểu (như `HTTP 500` hay `NullPointerException`) cho người dùng cuối.
- **Cấu trúc:**
  1. Icon biểu tượng lỗi trực quan (Mất mạng, Lỗi máy chủ).
  2. Thông điệp thân thiện: *"Không thể kết nối đến máy chủ"*.
  3. Nút *"Thử lại"* (Retry): Gọi lại hàm nạp dữ liệu của ViewModel với hiệu ứng xoay nhẹ.

---

## 2. QUY CHUẨN TIẾP CẬN TOÀN DIỆN (WCAG 2.1 AA COMPLIANCE)

### 2.1 Vùng Tương Tác Cảm Ứng Tối Thiểu (Minimum Touch Target Size)
- **Quy tắc 48x48dp:** Mọi thành phần người dùng có thể nhấp chuột hoặc chạm (Nút bấm, IconButton, Checkbox, Radio, Chip) đều phải có kích thước vùng cảm ứng tối thiểu **48x48dp**.
- Nếu kích thước hình ảnh hiển thị nhỏ hơn (ví dụ icon 20x20dp), bắt buộc sử dụng `Modifier.size(48.dp)` hoặc `Modifier.minimumInteractiveComponentSize()`.

### 2.2 Tiêu Chuẩn Trình Đọc Màn Hình (TalkBack Screen Reader)
- Tuyệt đối không để `contentDescription = null` trên các phần tử có tương tác.
- Định nghĩa ngữ cảnh đầy đủ:
  ```kotlin
  Modifier.semantics {
      contentDescription = "Tăng số lượng món Cơm Tấm Sườn, hiện tại là $quantity phần"
      role = Role.Button
  }
  ```
- Nhóm thông tin liên quan thành 1 khối đọc (Semantic Merging): Thẻ nhà hàng gom nhóm thông tin tên quán, số sao, khoảng cách thành một khối thống nhất để TalkBack không đọc ngắt quãng từng từ rời rạc.

### 2.3 Khả Năng Phóng To Cỡ Chữ Hệ Thống (Dynamic Font Scaling)
- Sử dụng đơn vị `sp` cho toàn bộ văn bản và kiểm tra giao diện hiển thị chuẩn xác ở mức cài đặt cỡ chữ hệ thống lớn nhất (**Font Scale 1.3x** hoặc **2.0x** của Android).
- Không cố định cứng chiều cao `Modifier.height(...)` cho các khối chứa văn bản để tránh tình trạng chữ bị cắt cụt (Text Clipping).
