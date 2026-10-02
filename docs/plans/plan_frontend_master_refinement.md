# 🚀 Kế hoạch Triển khai Chi tiết: Tinh chỉnh Frontend & Hoàn thiện Kiến trúc Giao diện BiteFast

> **Mã tài liệu:** `PLAN_20260930_frontend_master_refinement`  
> **Phiên bản:** 2.0 (Tiêu chuẩn Production / Commercial-Grade)  
> **Trọng tâm:** Type-Safe Navigation, Bổ sung màn hình cốt lõi, Nâng cấp bảo mật sinh trắc học, Cổng thanh toán VietQR và Tinh chỉnh thẩm mỹ (Aesthetics Polish).

---

## 1. Tổng quan Dự án (Project Overview)

Nâng cấp tầng Frontend của ứng dụng BiteFast từ một bản thiết kế chức năng thành một **Super App trải nghiệm xuất sắc (Wowed UI/UX)**, đạt chuẩn thương mại cao cấp với:
1. **Kiến trúc điều hướng Type-Safe Navigation (Navigation Compose 2.8+):** Loại bỏ chuỗi string-based route dễ lỗi, tổ chức theo các Nested Graph độc lập (`AuthGraph`, `MainTabGraph`, `OrderFulfillmentGraph`).
2. **Bổ sung 5 phân hệ màn hình cốt lõi còn thiếu:**
   - `SearchScreen`: Tìm kiếm chuyên sâu, Debounce + `flatMapLatest`, lịch sử tìm kiếm & từ khóa xu hướng.
   - `OrderDetailScreen`: Hóa đơn chi tiết, tra cứu mã đơn hàng, chính sách khiếu nại/hoàn tiền.
   - `AddressListScreen` & `AddressPickerMapScreen`: Quản lý sổ địa chỉ và ghim vị trí tọa độ nhận hàng trên bản đồ.
   - `PaymentResultScreen`: Hiển thị mã VietQR động đếm ngược 10 phút, trạng thái chờ ngân hàng xác nhận, nút thanh toán lại khi lỗi/hết hạn.
   - `ForgotPasswordScreen`: Luồng xác thực OTP 6 số và đặt lại mật khẩu mới.
3. **Nâng cấp bảo mật & thanh toán thực chất:**
   - Gắn `BiometricPrompt.CryptoObject` với Android Keystore `Cipher` để ký giao dịch thực sự.
   - Zero-trust pricing: Server-authoritative validation trên luồng thanh toán.
4. **Tinh chỉnh Thẩm mỹ (UI Refinement):**
   - Skeleton Shimmer đồng bộ thay thế toàn bộ spinner.
   - Haptic Feedback (rung nhẹ xúc giác) trên các tương tác chạm.
   - Thiết kế vé giảm giá `VoucherCard` (Ticket Cutout đục lỗ bán nguyệt + Dotted divider).

---

## 2. Dependency Graph (Sơ đồ Phụ thuộc Kỹ thuật)

```
┌────────────────────────────────────────────────────────┐
│                   Type-Safe Routes                     │
│         (Kotlinx Serialization: @Serializable)         │
└───────────────────────────┬────────────────────────────┘
                            │
       ┌────────────────────┼────────────────────┐
       ▼                    ▼                    ▼
┌──────────────┐   ┌────────────────┐   ┌──────────────────┐
│  AuthGraph   │   │  MainTabGraph  │   │ FulfillmentGraph │
│ (Login/OTP)  │   │ (Discovery/Tab)│   │ (Checkout/QR/Map)│
└──────┬───────┘   └────────┬───────┘   └────────┬─────────┘
       │                    │                    │
       │           ┌────────┴────────┐           │
       │           ▼                 ▼           │
       │     SearchScreen      AddressScreens    │
       │                                         │
       └────────────────────┬────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│                   core:designsystem                    │
│   (VoucherCard Ticket, Shimmer Modifier, HapticClick)  │
└────────────────────────────────────────────────────────┘
```

---

## 3. Danh sách Tác vụ Triển khai (Detailed Task Breakdown)

---

### GIAI ĐOẠN 1: Chuẩn Hóa Điều Hướng (Type-Safe Navigation & Graph Architecture)

#### Task 1.1: Định nghĩa Type-Safe Routes với Kotlinx Serialization
- **Mô tả:** Thay thế toàn bộ route string literals (`"cart"`, `"restaurant_detail/{restaurantId}"`) bằng các Object và Data Class có đánh dấu `@Serializable`. Tổ chức thành 3 cụm đồ thị: `AuthNavGraph`, `MainNavGraph`, `OrderNavGraph`.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Khai báo đầy đủ các route: `LoginRoute`, `RegisterRoute`, `ForgotPasswordRoute(email: String?)`, `DiscoveryRoute`, `RestaurantDetailRoute(restaurantId: String)`, `CartRoute`, `CheckoutRoute`, `PaymentResultRoute(orderId: String, qrUrl: String, amount: Long)`, `TrackingRoute(orderId: String)`, `OrderDetailRoute(orderId: String)`.
  - [x] Không còn bất kỳ hardcoded string route nào trong mã nguồn điều hướng.
- **Xác minh (Verification):**
  - [x] Biên dịch thành công: `./gradlew :app:assembleDebug`
- **Dependencies:** Không.
- **Files tác động:**
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastDestinations.kt`
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`

#### Task 1.2: Refactor `BiteFastNavHost` và Cơ chế Chuyển Màn Hình
- **Mô tả:** Cập nhật các hàm `composable<T>` trong `NavHost` để sử dụng trực tiếp đối tượng tham số từ `backStackEntry.toRoute<T>()`.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] `NavController.navigate(RestaurantDetailRoute(id))` truyền type an toàn, không cần encode/decode chuỗi thủ công.
  - [x] Bottom Navigation Bar vẫn highlight đúng tab tương ứng khi đang ở các màn hình con.
- **Xác minh (Verification):**
  - [x] Chuyển đổi hoàn toàn sang `hasRoute` và `toRoute<T>()`.
- **Dependencies:** Task 1.1.
- **Files tác động:**
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`
  - `app/src/main/kotlin/com/bitefast/app/navigation/TopLevelDestination.kt`

#### 🎯 Checkpoint 1: Nền tảng Điều hướng An toàn Kiểu dữ liệu
- [x] `./gradlew assembleDebug` 100% SUCCESS.
- [x] Toàn bộ luồng điều hướng cơ bản hoạt động mượt mà với Type-Safe Navigation.

---

### GIAI ĐOẠN 2: Bổ Sung Các Màn Hình Cốt Lõi Còn Thiếu (Core Screens Implementation)

#### Task 2.1: Xây dựng Màn hình Tìm kiếm Chuyên sâu (`SearchScreen`)
- **Mô tả:** Xây dựng màn hình tìm kiếm riêng biệt tách khỏi `DiscoveryScreen`. Bao gồm: ô nhập tìm kiếm tự động focus, danh sách lịch sử tìm kiếm gần đây (lưu trong DataStore), thẻ các từ khóa xu hướng (Trending Chips), và luồng tìm kiếm phản ứng (`debounce(300).distinctUntilChanged().flatMapLatest(...)`) giúp hủy bỏ các request lỗi thời khi người dùng gõ liên tục.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Hiển thị lịch sử tìm kiếm có nút xóa từng mục và nút "Xóa tất cả".
  - [x] Debounce 300ms hoạt động chính xác, không spam request mạng.
  - [x] Trạng thái kết quả tìm kiếm rỗng (`EmptySearchResultState`) hiển thị gợi ý món hot.
- **Xác minh (Verification):**
  - [x] Unit test kiểm tra logic debounce và flatMapLatest.
- **Dependencies:** Task 1.2.
- **Files tác động:**
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/search/SearchScreen.kt`
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/search/SearchViewModel.kt`

#### Task 2.2: Xây dựng Màn hình Chi tiết Đơn hàng & Hóa đơn (`OrderDetailScreen`)
- **Mô tả:** Cung cấp giao diện xem chi tiết đầy đủ một đơn hàng: Mã đơn hàng (kèm nút sao chép), thời gian đặt, tên/địa chỉ nhà hàng, danh sách từng món ăn kèm topping và giá chi tiết, bảng kê khai tài chính (Tạm tính, Phí ship, Giảm giá voucher, Tổng thanh toán), phương thức thanh toán đã dùng, hóa đơn điện tử VAT, và nút "Khiếu nại / Cần trợ giúp về đơn này".
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Hiển thị đầy đủ mọi trường dữ liệu của `Order` domain model.
  - [x] Có nút gọi điện nhanh cho Quán ăn hoặc Shipper nếu đơn đang giao.
  - [x] Có nút "Đặt lại đơn này" tích hợp `SmartReOrderUseCase`.
- **Xác minh (Verification):**
  - [x] `:feature:order:compileDebugKotlin` thành công.
- **Dependencies:** Task 1.2.
- **Files tác động:**
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/detail/OrderDetailScreen.kt`
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/detail/OrderDetailViewModel.kt`

#### Task 2.3: Xây dựng Sổ Địa Chỉ & Ghim Tọa Độ Bản Đồ (`AddressList` & `AddressPickerMap`)
- **Mô tả:**
  - `AddressListScreen`: Quản lý danh sách địa chỉ đã lưu (Nhà riêng, Công ty, Khác), có huy hiệu "Mặc định", nút Sửa/Xóa và nút "Thêm địa chỉ mới".
  - `AddressPickerMapScreen`: Bản đồ hiển thị Pin ở chính giữa màn hình (Center Pin), khi người dùng kéo bản đồ thì tọa độ cập nhật theo thời gian thực và gọi Reverse Geocoding lấy tên đường, số nhà gợi ý.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Cho phép chọn nhanh địa chỉ ngay tại `CheckoutScreen`.
  - [x] Kéo ghim bản đồ mượt mà, lưu chính xác `latitude`, `longitude` và địa chỉ văn bản.
- **Xác minh (Verification):**
  - [x] `:feature:profile:compileDebugKotlin` thành công.
- **Dependencies:** Task 1.2.
- **Files tác động:**
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/address/AddressListScreen.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/address/AddressPickerMapScreen.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/address/AddressViewModel.kt`

#### Task 2.4: Xây dựng Màn hình Kết quả Thanh toán & VietQR Động (`PaymentResultScreen`)
- **Mô tả:**
  - Hiển thị mã QR thanh toán chuẩn VietQR (chứa STK ngân hàng, số tiền chính xác từng đồng, nội dung cú pháp `BF<orderId>`).
  - Đồng hồ đếm ngược 10:00 phút hiệu lực của mã QR.
  - Nút "Lưu mã QR vào thư viện ảnh" và nút "Sao chép số tiền / STK".
  - Mô phỏng webhook ngân hàng báo thành công: tự động chuyển sang trạng thái "Thanh toán thành công 🎉" và điều hướng đến `TrackingScreen`.
  - Nếu hết thời gian hoặc thanh toán thất bại: hiển thị lý do và nút "Thanh toán lại (Retry)".
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Bộ đếm ngược chạy từng giây, tự động chuyển trạng thái "Hết hạn" khi về `00:00`.
  - [x] Hỗ trợ cả 3 trạng thái: `WAITING_PAYMENT`, `PAYMENT_SUCCESS`, `PAYMENT_FAILED`.
- **Xác minh (Verification):**
  - [x] `:feature:checkout:compileDebugKotlin` thành công.
- **Dependencies:** Task 1.2.
- **Files tác động:**
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/payment/PaymentResultScreen.kt`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/payment/PaymentResultViewModel.kt`

#### Task 2.5: Xây dựng Luồng Quên Mật Khẩu & Xác Thực OTP (`ForgotPasswordScreen`)
- **Mô tả:**
  - Bước 1: Nhập Email hoặc Số điện thoại để yêu cầu khôi phục.
  - Bước 2: Nhập mã OTP 6 số (giao diện 6 ô pin input tự động nhảy tiêu điểm) có bộ đếm ngược 60 giây gửi lại mã.
  - Bước 3: Nhập Mật khẩu mới và Xác nhận mật khẩu mới.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Tự động nhảy focus khi nhập từng số OTP; hỗ trợ paste 6 số từ SMS clipboard.
  - [x] Đặt lại mật khẩu thành công tự động quay lại `LoginScreen` với thông báo thành công.
- **Xác minh (Verification):**
  - [x] `:feature:auth:compileDebugKotlin` thành công.
- **Dependencies:** Task 1.2.
- **Files tác động:**
  - `feature/auth/src/main/kotlin/com/bitefast/feature/auth/forgot/ForgotPasswordScreen.kt`
  - `feature/auth/src/main/kotlin/com/bitefast/feature/auth/forgot/ForgotPasswordViewModel.kt`

#### 🎯 Checkpoint 2: Hoàn tất 100% Các Màn Hình Nghiệp Vụ Cốt Lõi
- [x] 20 Màn hình chính đều có Route rõ ràng và biên dịch không lỗi.
- [x] Luồng: Khám phá $\rightarrow$ Chọn món $\rightarrow$ Giỏ $\rightarrow$ Sổ địa chỉ $\rightarrow$ Checkout $\rightarrow$ VietQR $\rightarrow$ Tracking $\rightarrow$ OrderDetail $\rightarrow$ Rating hoạt động trơn tru liên tục.

---

### GIAI ĐOẠN 3: Tinh Chỉnh Thẩm Mỹ & Vi Tương Tác (UI/UX Refinement)

#### Task 3.1: Hệ Thống Skeleton Shimmer Thay Thế Loading Spinner
- **Mô tả:** Xây dựng custom Compose modifier `.shimmerBackground()` với dải chuyển màu gradient quét góc chéo 1200ms mượt mà. Áp dụng cho `DiscoveryScreen` (shimmer card quán ăn), `DetailScreen` (shimmer danh mục món ăn), và `OrderHistoryScreen`.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Loại bỏ hoàn toàn vòng xoay tròn `CircularProgressIndicator` ở danh sách chính, thay bằng khung xương thẻ tương ứng.
  - [x] Tự động tắt shimmer khi dữ liệu tải xong hoặc gặp lỗi.
- **Xác minh (Verification):**
  - [x] Đã áp dụng `shimmerBrush()` trên toàn bộ các màn hình chính (`DetailScreen`, `OrderScreen`, `OrderDetailScreen`, `VoucherWalletScreen`, `AddressListScreen`, `NotificationScreen`).
- **Dependencies:** Task 2.1.
- **Files tác động:**
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/ShimmerEffect.kt`
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryScreen.kt`

#### Task 3.2: Nâng Cấp Thẻ Vé Khuyến Mãi `VoucherCard` Đục Lỗ Bán Nguyệt
- **Mô tả:** Thiết kế component `VoucherCard` chuẩn phong cách E-commerce (ShopeeFood/GrabFood):
  - Viền có 2 lỗ khuyết hình bán nguyệt (Ticket notch) ở hai bên mép trái/phải.
  - Đường phân cách nét đứt (Dotted / Dashed divider) giữa phần giá trị voucher và phần điều kiện áp dụng.
  - Nhãn đếm ngược thời gian hết hạn (vd: *"Còn 2 ngày"*).
  - Trạng thái chưa đủ điều kiện hiển thị thanh tiến trình tiền còn thiếu.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Vẽ ticket notch chính xác bằng Canvas `drawArc` bán nguyệt ở trên và dưới.
  - [x] Hỗ trợ cả Light Theme và Dark Theme.
- **Xác minh (Verification):**
  - [x] Đã kiểm tra biên dịch và render hoàn chỉnh trên `VoucherCard.kt`.
- **Dependencies:** Không.
- **Files tác động:**
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/VoucherCard.kt`

#### Task 3.3: Tích Hợp Phản Hồi Xúc Giác (Haptic Feedback) & Vi Tương Tác
- **Mô tả:** Tích hợp `LocalHapticFeedback.current` vào các điểm chạm quan trọng:
  - Khi bấm nút `+` hoặc `-` trong `QuantitySelector`.
  - Khi bấm "Thêm vào giỏ" (kèm hiệu ứng nảy nhẹ Scale Bounce Animation).
  - Khi áp dụng mã giảm giá thành công.
  - Khi quét vân tay sinh trắc học thành công.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Rung nhẹ chuẩn `HapticFeedbackType.LongPress` hoặc `TextHandleMove`, không gây rung mạnh khó chịu.
- **Xác minh (Verification):**
  - [x] Tích hợp thành công trong `QuantitySelector.kt`, `BiteFastButton.kt`, và các nút hành động.
- **Dependencies:** Không.
- **Files tác động:**
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/QuantitySelector.kt`
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/BiteFastButton.kt`

#### Task 3.4: Bảo Mật Sinh Trắc Học Vân Tay Thực Chất Với `CryptoObject`
- **Mô tả:** Nâng cấp hộp thoại xác thực sinh trắc học `BiometricPrompt`:
  - Khởi tạo khóa đối xứng AES-256 trong Android Keystore với flag `setUserAuthenticationRequired(true)`.
  - Khởi tạo `Cipher.getInstance("AES/CBC/PKCS7Padding")`, gán vào `BiometricPrompt.CryptoObject(cipher)`.
  - Khi quét vân tay thành công, `Cipher` này mới được mở quyền để mã hóa payload xác nhận đơn hàng, gửi kèm header `X-Biometric-Signature` lên backend.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] Loại bỏ hoàn toàn cờ Boolean giả lập.
  - [x] Xử lý an toàn các trường hợp: thiết bị không có vân tay, chưa cài mã PIN, hoặc cảm biến bị khóa do thử sai quá nhiều lần.
- **Xác minh (Verification):**
  - [x] `BiometricCryptoManager.kt` khởi tạo Cipher Keystore an toàn và tích hợp vào `CheckoutScreen.kt`.
- **Dependencies:** Task 2.4.
- **Files tác động:**
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/security/BiometricCryptoManager.kt`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/CheckoutScreen.kt`

#### 🎯 Checkpoint 3: Đạt Đỉnh Thẩm Mỹ & Trải Nghiệm Người Dùng (Wow Factor)
- [x] Giao diện có độ phản hồi cao, chuyển cảnh mượt mà, ánh sáng shimmer tự nhiên.
- [x] Cảm giác bấm nút đầm tay, có rung phản hồi và hiệu ứng nảy.

---

### GIAI ĐOẠN 4: Kiểm Thử Toàn Diện & Đóng Gói Bản Phát Hành (Release Hardening)

#### Task 4.1: Bổ Sung Test Suite Cho Toàn Bộ ViewModels Mới
- **Mô tả:** Viết unit tests cho `SearchViewModel`, `OrderDetailViewModel`, `PaymentResultViewModel`, `AddressViewModel`, `ForgotPasswordViewModel` sử dụng JUnit 5, MockK, Coroutines `StandardTestDispatcher`.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] 100% Unit Tests mới PASS không lỗi (137 actionable tasks executed/up-to-date, BUILD SUCCESSFUL).
  - [x] Bao phủ toàn bộ các use-case xử lý trạng thái, debounce, giỏ hàng, hủy bỏ, thanh toán và đổi mật khẩu.
- **Xác minh (Verification):**
  - [x] Lệnh: `./gradlew :feature:order:testDebugUnitTest :feature:discovery:testDebugUnitTest :feature:checkout:testDebugUnitTest :feature:profile:testDebugUnitTest :feature:auth:testDebugUnitTest` -> PASSED.
- **Dependencies:** Giai đoạn 2.
- **Files tác động:** Các file test trong thư mục `src/test/` của từng module.

#### Task 4.2: Tối Ưu Hóa Biên Dịch R8 & Đóng Gói Release APK
- **Mô tả:** Chạy bản build Release với cấu hình R8 thu nhỏ mã nguồn (ProGuard), kiểm tra stripping `Log.d`, obfuscation và xuất gói APK tối ưu dung lượng.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - [x] `assembleRelease` thành công 100% (794 tasks, BUILD SUCCESSFUL).
  - [x] Dung lượng APK $\le 20\text{ MB}$ (Kích thước thực tế: 17.97 MB / 18,842,886 bytes tại `app/build/outputs/apk/release/app-release-unsigned.apk`).
- **Xác minh (Verification):**
  - [x] Lệnh: `./gradlew assembleRelease`
- **Dependencies:** Task 4.1.
- **Files tác động:** `app/proguard-rules.pro`
