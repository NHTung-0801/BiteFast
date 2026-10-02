# 🚀 KẾ HOẠCH NÂNG CẤP BỐ CỤC TRANG CHỦ, CHUẨN HÓA UTF-8 VÀ SỬA LỖI ĐIỀU HƯỚNG/THANH TOÁN/TRACKING

> **Mã kế hoạch:** `plan_ux_fix_and_home_redesign`  
> **Phiên bản:** 1.0  
> **Trọng tâm:** Khắc phục lỗi font UTF-8, hoàn thiện thanh điều hướng Back/Home, sửa logic Checkout & Tracking, và thiết kế lại Home với các hàng món ăn trượt ngang + danh sách dọc phong phú.

---

## 1. Mục tiêu & Phạm vi (Objective & Scope)

Bản kế hoạch này giải quyết triệt để 4 vấn đề được người dùng phản ánh:
1. **Kiểu chữ & Hiển thị tiếng Việt UTF-8:** Loại bỏ hoàn toàn các ký tự `?` bị lỗi mã hóa, chuẩn hóa cấu hình biên dịch Java/Kotlin UTF-8 trên môi trường Windows.
2. **Điều hướng (Navigation & Back Icons):** Đảm bảo người dùng luôn có nút Back / Trở về Trang chủ trên các màn hình sâu và màn hình Đơn hàng, không bao giờ bị kẹt lại.
3. **Logic Thanh toán & Theo dõi (Checkout & Tracking):**
   - Sửa lỗi nút Đặt hàng bị vô hiệu hóa im lặng do địa chỉ nhận hàng ban đầu rỗng. Cung cấp địa chỉ mặc định sẵn và thông báo kiểm tra hợp lệ rõ ràng.
   - Sửa lỗi màn hình Tracking bị đen: Xử lý an toàn luồng dữ liệu đơn hàng, khởi tạo tọa độ và canvas mô phỏng shipper mượt mà trên mọi thiết bị / emulator.
4. **Bố cục Trang chủ Đa dạng (Discovery Redesign):**
   - Hàng trượt ngang 1: **Món ăn đánh giá cao ⭐** (Top-rated dishes).
   - Hàng trượt ngang 2: **Món ăn khuyến mãi & gần tôi 🛵** (Promotions & Nearby dishes).
   - Danh sách dọc: **Toàn bộ thực đơn & nhà hàng tuyển chọn** (All dishes / restaurants).

---

## 2. Đồ thị Ảnh hưởng Module (Module Impact Graph)

```
[UI: feature:discovery] ──► Hiển thị món ăn trượt ngang & dọc đa dạng
[UI: feature:cart]      ──► Chuẩn hóa hiển thị UTF-8, thêm nút Back về Home
[UI: feature:order]     ──► Thêm nút Back về Home trên TopAppBar, sửa font chữ
[UI: feature:checkout]  ──► Fix logic validate address, enable nút Đặt hàng
[UI: feature:tracking]  ──► Fix lỗi đen màn hình, bổ sung nút Về trang chủ
[app: navigation]       ──► Đồng bộ route callback onNavigateToHome, Back icon
[core: model & data]    ──► Bổ sung dữ liệu món ăn đặc sắc (Featured dishes)
```

---

## 3. Phân Rã Công Việc Chi Tiết (Task Breakdown)

### Task 1: Chuẩn hóa UTF-8 và Sửa lỗi Ký tự `?`
- **Mô tả:** Kiểm tra và thay thế toàn bộ chuỗi ký tự bị vỡ font `?` trong mã nguồn. Cấu hình `gradle.properties` và compiler để luôn biên dịch UTF-8.
- **Tiêu chí nghiệm thu:**
  - Không còn bất kỳ chuỗi nào hiển thị dạng `Gi? hàng`, `T?m tính`, `T?ng c?ng`.
  - Hiển thị tiếng Việt có dấu chuẩn 100% trên toàn bộ màn hình.
- **Files tác động:**
  - `gradle.properties`
  - `feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartScreen.kt`
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/OrderScreen.kt`
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingScreen.kt`

---

### Task 2: Nâng cấp Điều hướng & Bổ sung Back / Home Icons
- **Mô tả:**
  - Thêm `navigationIcon` (Mũi tên trở lại) trên TopAppBar của `OrderScreen` và `CartScreen` để người dùng có thể chạm vào để quay về Trang chủ (`DiscoveryDestination`).
  - Đảm bảo tất cả màn hình chức năng sâu (`TrackingScreen`, `OrderDetailScreen`, `RestaurantDetailScreen`, `CheckoutScreen`, `SearchScreen`) đều có nút Back rõ ràng.
  - Trên `TrackingScreen`, bổ sung nút hành động "Về trang chủ" ở TopAppBar hoặc thanh điều khiển phía dưới.
- **Tiêu chí nghiệm thu:**
  - Vào màn hình Đơn hàng, Giỏ hàng hoặc Tracking đều có nút quay lại Trang chủ ngay lập tức.
- **Files tác động:**
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/OrderScreen.kt`
  - `feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartScreen.kt`
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingScreen.kt`

---

### Task 3: Sửa Logic Thanh toán (Checkout) & Lỗi Màn hình đen (Tracking)
- **Mô tả:**
  - **Checkout:** Khởi tạo địa chỉ mặc định sẵn cho người dùng (`streetAddress = "123 Lê Lợi, Phường Bến Nghé, Quận 1"`, `phoneNumber = "0901234567"`), đồng thời hiển thị thông báo validation trực quan khi người dùng xóa trống thay vì disabled nút im lặng.
  - **Tracking:** Tối ưu hóa `TrackingScreen` và `TrackingViewModel`:
    - Truyền và đọc `orderId` an toàn.
    - Cung cấp dữ liệu đơn hàng mẫu tức thời trong `TrackingUiState` khi load để tránh trạng thái đen màn hình do render lỗi hoặc chưa có dữ liệu.
    - Tối ưu màu sắc nền và Canvas `TrackingMapCard` để luôn hiển thị bản đồ vẽ mượt mà, sáng sủa, không bao giờ bị đen.
- **Tiêu chí nghiệm thu:**
  - Nhấn "Tiến hành thanh toán" từ Giỏ hàng chuyển sang màn hình Checkout mượt mà, nút "Đặt hàng" sẵn sàng hoạt động.
  - Nhấn "Theo dõi" đơn hàng hiển thị ngay màn hình tracking trực quan, có bản đồ lộ trình di chuyển của shipper, thời gian ETA và các mốc tiến trình rõ nét.
- **Files tác động:**
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/CheckoutViewModel.kt`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/CheckoutScreen.kt`
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingViewModel.kt`
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingScreen.kt`

---

### Task 4: Tái cấu trúc Trang chủ (Home Discovery) - Hàng ngang & Danh sách dọc
- **Mô tả:**
  - Xây dựng component `FoodDishCard` đẹp mắt cho món ăn (ảnh thumbnail bo tròn góc, tên món, giá tiền format VND màu cam, đánh giá sao, thời gian chuẩn bị).
  - Tái cấu trúc `DiscoveryScreen`:
    1. **Hàng ngang 1 (LazyRow):** "Món ăn đánh giá cao ⭐" - Trượt ngang mượt mà, hiển thị các món ăn có điểm số xuất sắc nhất kèm nhãn "Best Seller".
    2. **Hàng ngang 2 (LazyRow):** "Ưu đãi gần bạn 📍🛵" - Trượt ngang các món có khuyến mãi, freeship và cự ly gần nhất.
    3. **Hàng trải dọc (Vertical Feed):** "Toàn bộ món ngon & Quán ăn" - Danh sách trải dọc với đầy đủ các món ăn và nhà hàng đối tác, kết hợp bộ lọc danh mục món (Cơm, Phở, Trà sữa, Pizza,...).
- **Tiêu chí nghiệm thu:**
  - Giao diện trang chủ sống động, vuốt trượt ngang mượt mà 60fps, cuộn dọc hiển thị đầy đủ và phong phú.
- **Files tác động:**
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/FoodDishCard.kt` (mới)
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryScreen.kt`
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryViewModel.kt`

---

## 4. Rủi ro & Biện pháp Giảm thiểu (Risks & Mitigations)
- **Rủi ro:** Cache build cũ gây lỗi file nằm ngoài thư mục gốc (`outside the root directory`).
  - **Biện pháp:** Đã chạy `./gradlew clean` thành công để dọn sạch toàn bộ cache cũ.
- **Rủi ro:** Xung đột theme chế độ nền tối (Dark mode) khiến màn hình chuyển sang màu đen mờ.
  - **Biện pháp:** Cố định `containerColor = MaterialTheme.colorScheme.background` và `surface` sáng sủa theo chuẩn BiteFast Material 3.
