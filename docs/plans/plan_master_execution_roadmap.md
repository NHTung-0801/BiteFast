# 🎯 LỘ TRÌNH THỰC THI CHI TIẾT TOÀN DIỆN DỰ ÁN BITEFAST (MASTER EXECUTION ROADMAP)

> **Mã kế hoạch:** `PLAN_20260925_master_execution_roadmap`  
> **Phiên bản:** 1.0 — Chuẩn Enterprise 10/10  
> **Căn cứ đặc tả:** [bitefast_project_plan.md](../../bitefast_project_plan.md) & [AGENTS.md](../../AGENTS.md)  
> **Ngày lập:** 25/09/2026  
> **Mục tiêu:** Chia nhỏ toàn bộ khối lượng công việc còn lại của dự án thành 6 giai đoạn tuần tự (Domain ➔ Data ➔ Presentation/Feature ➔ App), mỗi giai đoạn được phân rã thành các task vi mô (kích thước S/M, từ 1–4 files) có tiêu chí nghiệm thu và bước kiểm thử tự động rõ ràng.

---

## 🗺️ BẢN ĐỒ PHÂN KỲ TRIỂN KHAI (PHASE ROADMAP)

```
[Phase 1: Hoàn Thiện Tầng Nghiệp Vụ Thuần & UseCases 100%]
                          │
                          ▼
[Phase 2: Data Layer Cường Hóa & Bảo Mật Phần Cứng Keystore/SQLCipher]
                          │
                          ▼
[Phase 3: Phân Hệ Auth, Guest Mode & Login Gate]
                          │
                          ▼
[Phase 4: Phân Hệ Đặt Món: Detail, Cart Conflict & Checkout Sinh Trắc]
                          │
                          ▼
[Phase 5: Phân Hệ Hậu Đặt Hàng: Realtime Tracking, Order History & Rating]
                          │
                          ▼
[Phase 6: A11y WCAG 2.1 AA, NDK Hardening, Kover CI Gate ≥ 85% & R8]
```

---

## GIAI ĐOẠN 1: Hoàn Thiện Toàn Diện Tầng Nghiệp Vụ Thuần (Pure Domain UseCases)
*Mục tiêu: Đóng gói 100% logic kinh doanh vào các UseCases độc lập trên Pure Kotlin JVM, không phụ thuộc Android SDK, viết Unit Tests đạt độ bao phủ ≥ 95%.*

### Task 1.1: Bộ UseCases Quản Lý Giỏ Hàng Nâng Cao
- **Quy mô:** S (2 files)
- **Mục tiêu:** Xử lý các tình huống phức tạp của giỏ hàng: Xung đột nhà hàng (Restaurant Conflict), tính toán lại tổng tiền khi có phí giao hàng động, kiểm tra giỏ hàng trống.
- **Tiêu chí nghiệm thu:**
  - `ClearCartUseCase`: Xóa toàn bộ giỏ hàng khi người dùng xác nhận tạo giỏ mới từ nhà hàng khác.
  - `CalculateCartTotalUseCase`: Tính toán Tạm tính, Phí giao hàng (miễn phí nếu đơn ≥ 150k), và Thuế VAT 8%.
- **Xác minh:** Unit Test với JUnit 5 kiểm tra mọi nhánh tính toán và trả về kết quả chuẩn xác.
- **Phụ thuộc:** Không.
- **Files tác động:**
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/cart/ClearCartUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/cart/CalculateCartTotalUseCase.kt`
  - `core/domain/src/test/kotlin/com/bitefast/core/domain/cart/CartUseCasesTest.kt`

### Task 1.2: Bộ UseCases Voucher & Khuyến Mãi (Promotion Engine)
- **Quy mô:** M (3 files)
- **Mục tiêu:** Áp dụng mã giảm giá theo quy tắc: phần trăm (kèm trần tối đa), số tiền cố định, hoặc miễn phí vận chuyển.
- **Tiêu chí nghiệm thu:**
  - `ValidateVoucherUseCase`: Kiểm tra hạn sử dụng, số lượt dùng còn lại, giá trị đơn tối thiểu và danh sách nhà hàng áp dụng.
  - `ApplyVoucherUseCase`: Tính toán số tiền được giảm trừ thực tế và trả về `VoucherResult.Success(discountAmount)` hoặc `VoucherResult.Invalid(reason)`.
- **Xác minh:** Unit Test bao phủ 100% các điều kiện biên (đơn vừa chạm mốc tối thiểu, giảm giá vượt trần tối đa, voucher hết hạn).
- **Phụ thuộc:** Không.
- **Files tác động:**
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/voucher/ValidateVoucherUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/voucher/ApplyVoucherUseCase.kt`
  - `core/domain/src/test/kotlin/com/bitefast/core/domain/voucher/VoucherUseCasesTest.kt`

### Task 1.3: Bộ UseCases Smart Re-Order ("Đặt Lại Đơn Cũ")
- **Quy mô:** M (3 files)
- **Mục tiêu:** Cho phép người dùng nạp lại toàn bộ món của một đơn hàng cũ vào giỏ chỉ với 1 chạm.
- **Tiêu chí nghiệm thu:**
  - `SmartReOrderUseCase`:
    1. Kiểm tra nhà hàng cũ có đang mở cửa không?
    2. Kiểm tra các món ăn có còn phục vụ không?
    3. Kiểm tra giỏ hàng hiện tại (nếu đang chứa món quán khác thì kích hoạt cảnh báo Conflict).
    4. Nạp lại đúng số lượng và ghi chú vào giỏ hàng.
- **Xác minh:** Unit Test với MockK mô phỏng kịch bản quán đóng cửa, món hết hàng và thành công.
- **Phụ thuộc:** Task 1.1.
- **Files tác động:**
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/order/SmartReOrderUseCase.kt`
  - `core/domain/src/test/kotlin/com/bitefast/core/domain/order/SmartReOrderUseCaseTest.kt`

### Task 1.4: Bộ UseCases Đánh Giá Đơn Hàng & Sổ Địa Chỉ
- **Quy mô:** M (4 files)
- **Mục tiêu:** Xử lý gửi đánh giá nhà hàng/tài xế và quản lý danh sách địa chỉ nhận hàng của người dùng.
- **Tiêu chí nghiệm thu:**
  - `SubmitRatingUseCase`: Kiểm tra đơn hàng đã ở trạng thái `DELIVERED` mới cho phép gửi đánh giá; hỗ trợ đánh giá ẩn danh.
  - `GetAddressesUseCase` & `SetDefaultAddressUseCase`: Quản lý sổ địa chỉ giao hàng.
- **Xác minh:** Unit Test kiểm tra chặn đánh giá khi đơn chưa giao thành công.
- **Phụ thuộc:** Không.
- **Files tác động:**
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/rating/SubmitRatingUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/user/AddressUseCases.kt`
  - `core/domain/src/test/kotlin/com/bitefast/core/domain/rating/RatingUseCaseTest.kt`

> 🏁 **CHECKPOINT 1:** Tầng `core:domain` đạt 100% UseCases nghiệp vụ. Chạy bộ Unit Test thuần JVM với kết quả bao phủ (coverage) ≥ 95%.

---

## GIAI ĐOẠN 2: Data Layer Cường Hóa & Bảo Mật Phần Cứng Keystore/SQLCipher
*Mục tiêu: Đưa điểm số bảo mật dữ liệu lưu trữ tại chỗ và phiên mạng đạt chuẩn 10/10.*

### Task 2.1: Tích Hợp Android Keystore Phần Cứng Cho SQLCipher Passphrase
- **Quy mô:** M (3 files)
- **Mục tiêu:** Thay vì dùng passphrase tĩnh, tự động sinh khóa ngẫu nhiên AES-256 lưu trong Android Keystore phần cứng (Hardware TEE / StrongBox) để mở khóa Room Database.
- **Tiêu chí nghiệm thu:**
  - `KeystoreManager`: Sinh và lưu Master Passphrase an toàn trong Keystore.
  - Cập nhật `DatabaseModule`: Khởi tạo Room DB với `SupportOpenHelperFactory(passphrase)`.
- **Xác minh:** Viết Instrumented Test xác nhận database không thể bị mở nếu thiếu khóa phần cứng.
- **Phụ thuộc:** Checkpoint 1.
- **Files tác động:**
  - `core/database/src/main/kotlin/com/bitefast/core/database/security/KeystoreManager.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/di/DatabaseModule.kt`

### Task 2.2: Kiểm Thử Tích Hợp OkHttp 401 Silent Token Renewal Với MockWebServer
- **Quy mô:** S (2 files)
- **Mục tiêu:** Chứng minh cơ chế làm mới token ngầm bằng `Mutex` trong [TokenAuthenticator.kt](../../core/network/src/main/kotlin/com/bitefast/core/network/authenticator/TokenAuthenticator.kt) hoạt động hoàn hảo dưới tải đồng thời.
- **Tiêu chí nghiệm thu:**
  - Mô phỏng 3 request đồng thời gặp 401 ➔ Chỉ có duy nhất 1 request gọi refresh token ➔ Cả 3 request được retry thành công với token mới.
- **Xác minh:** Chạy kiểm thử tự động với `MockWebServer`.
- **Phụ thuộc:** Checkpoint 1.
- **Files tác động:**
  - `core/network/src/test/kotlin/com/bitefast/core/network/TokenAuthenticatorTest.kt`

> 🏁 **CHECKPOINT 2:** Data Layer được bọc lót bảo mật tuyệt đối: Database mã hóa AES-256 SQLCipher và OkHttp tự động refresh phiên an toàn.

---

## GIAI ĐOẠN 3: Phân Hệ Xác Thực, Guest Mode & Login Gate (Auth 10/10)
*Mục tiêu: Triển khai luồng đăng nhập, đăng ký mượt mà kèm tính năng duyệt ứng dụng dạng Khách (Guest Mode) không bắt buộc đăng nhập sớm.*

### Task 3.1: Giao Diện Đăng Nhập & Đăng Ký MVI Trong `feature:auth`
- **Quy mô:** M (4 files)
- **Mục tiêu:** Xây dựng `LoginScreen` và `RegisterScreen` theo đúng hợp đồng UDF (`AuthUiState`, `AuthUiEvent`, `AuthUiEffect`).
- **Tiêu chí nghiệm thu:**
  - Validate email định dạng chuẩn và mật khẩu ≥ 8 ký tự realtime.
  - Phản hồi trạng thái Loading, hiển thị lỗi rõ ràng nếu sai tài khoản.
  - Hỗ trợ nút *"Khám phá ngay không cần đăng nhập"* (Bật Guest Mode).
- **Xác minh:** Compose Preview cho cả Light/Dark Theme; Robot Test cho luồng đăng nhập.
- **Phụ thuộc:** Checkpoint 2.
- **Files tác động:**
  - `feature/auth/src/main/kotlin/com/bitefast/feature/auth/AuthViewModel.kt`
  - `feature/auth/src/main/kotlin/com/bitefast/feature/auth/LoginScreen.kt`
  - `feature/auth/src/main/kotlin/com/bitefast/feature/auth/RegisterScreen.kt`

### Task 3.2: Thành Phần Tái Sử Dụng `LoginGateBottomSheet` & Bảo Toàn Deep Link
- **Quy mô:** S (2 files)
- **Mục tiêu:** Khi người dùng đang ở chế độ Khách (Guest Mode) mà bấm vào các thao tác nhạy cảm (Thanh toán, Xem đơn hàng, Đánh giá), ứng dụng bung BottomSheet yêu cầu đăng nhập.
- **Tiêu chí nghiệm thu:**
  - Sau khi đăng nhập thành công từ BottomSheet, tự động thực thi tiếp hành động trước đó (`savedAction/deepLink`) mà không làm gián đoạn trải nghiệm người dùng.
- **Xác minh:** Test kịch bản: Khách bấm "Thanh toán" ➔ Bung Login Gate ➔ Đăng nhập ➔ Điều hướng thẳng tới màn hình Thanh toán.
- **Phụ thuộc:** Task 3.1.
- **Files tác động:**
  - `feature/auth/src/main/kotlin/com/bitefast/feature/auth/component/LoginGateBottomSheet.kt`

> 🏁 **CHECKPOINT 3:** Phân hệ xác thực hoàn chỉnh, khách vãng lai thoải mái xem món nhưng được bảo vệ chặt chẽ khi đến các hành động giao dịch.

---

## GIAI ĐOẠN 4: Phân Hệ Chi Tiết Món Ăn, Giỏ Hàng & Thanh Toán Sinh Trắc
*Mục tiêu: Xây dựng trải nghiệm chọn món, tùy biến topping, xử lý xung đột giỏ hàng và bảo vệ thanh toán bằng Biometric.*

### Task 4.1: Triển Khai Màn Hình Chi Tiết Nhà Hàng & Món Ăn (`feature:detail`)
- **Quy mô:** M (4 files)
- **Mục tiêu:** Hiển thị thực đơn phân chia theo danh mục (Cơm, Nước uống, Tráng miệng); BottomSheet chọn size và topping cho món ăn.
- **Tiêu chí nghiệm thu:**
  - Nút *"Thêm vào giỏ hàng"* tự động tính tổng tiền theo số lượng và topping đã chọn.
  - Tích hợp hiệu ứng phản hồi xúc giác (Haptic Feedback) khi thêm món thành công.
- **Xác minh:** Compose UI Test kịch bản chọn topping và bấm thêm món.
- **Phụ thuộc:** Checkpoint 3.
- **Files tác động:**
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/RestaurantDetailViewModel.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/RestaurantDetailScreen.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/component/ToppingSelectionBottomSheet.kt`

### Task 4.2: Tích Hợp `ConflictDialog` Khi Xung Đột Nhà Hàng Trong `feature:cart`
- **Quy mô:** S (2 files)
- **Mục tiêu:** Hiển thị hộp thoại cảnh báo khi người dùng cố thêm món từ nhà hàng B trong khi giỏ đang có món của nhà hàng A.
- **Tiêu chí nghiệm thu:**
  - Thông báo rõ ràng: *"Bạn có muốn tạo giỏ hàng mới? Các món của [Nhà hàng A] sẽ bị xóa."*
  - Nút *"Tạo giỏ mới"* gọi `ClearCartUseCase` và nạp món mới. Nút *"Giữ lại"* hủy thao tác.
- **Xác minh:** Unit test ViewModel và Compose Test kịch bản xung đột.
- **Phụ thuộc:** Task 4.1.
- **Files tác động:**
  - `feature/cart/src/main/kotlin/com/bitefast/feature/cart/component/RestaurantConflictDialog.kt`

### Task 4.3: Triển Khai Màn Hình Thanh Toán & Bảo Vệ Sinh Trắc Học (`feature:checkout`)
- **Quy mô:** M (4 files)
- **Mục tiêu:** Màn hình thanh toán hoàn chỉnh với chọn địa chỉ giao hàng, áp mã giảm giá voucher và xác thực vân tay/khuôn mặt (`BiometricPrompt`).
- **Tiêu chí nghiệm thu:**
  - Áp dụng `BiometricAuthenticator` (mức độ `BIOMETRIC_STRONG` - Keystore Hardware) khi đơn hàng vượt hạn mức 500,000đ hoặc thanh toán qua thẻ tín dụng.
  - Nút *"Đặt hàng"* được debounce để chống người dùng bấm đúp gửi 2 đơn trùng nhau.
- **Xác minh:** Test luồng thanh toán thành công và luồng fallback sang mã PIN thiết bị nếu không có sinh trắc.
- **Phụ thuộc:** Task 4.2.
- **Files tác động:**
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/CheckoutViewModel.kt`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/CheckoutScreen.kt`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/component/VoucherBottomSheet.kt`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/security/BiometricHandler.kt`

> 🏁 **CHECKPOINT 4:** Hoàn tất luồng thương mại cốt lõi: Chọn món ➔ Tùy biến topping ➔ Giỏ hàng an toàn ➔ Thanh toán bảo mật vân tay.

---

## GIAI ĐOẠN 5: Phân Hệ Hậu Đặt Hàng: Realtime Tracking, Order History & Rating
*Mục tiêu: Bản đồ theo dõi tài xế thời gian thực bằng Google Maps Compose, lịch sử đơn và đánh giá chất lượng.*

### Task 5.1: Màn Hình Theo Dõi Đơn Hàng Realtime (`feature:tracking`)
- **Quy mô:** M (4 files)
- **Mục tiêu:** Tích hợp Google Maps Compose vẽ vị trí nhà hàng, điểm giao hàng và vị trí shipper.
- **Tiêu chí nghiệm thu:**
  - Mô phỏng tài xế di chuyển mượt mà dọc theo tuyến đường polyline (Spherical Linear Interpolation).
  - Thanh tiến trình trạng thái đơn hàng (Chuẩn bị ➔ Đang lấy món ➔ Đang giao ➔ Đã giao).
- **Xác minh:** Chạy thử nghiệm trên máy ảo có tọa độ GPS giả lập.
- **Phụ thuộc:** Checkpoint 4.
- **Files tác động:**
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingViewModel.kt`
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingScreen.kt`
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/component/OrderTimeline.kt`

### Task 5.2: Màn Hình Lịch Sử Đơn Hàng & Smart Re-Order (`feature:order`)
- **Quy mô:** M (3 files)
- **Mục tiêu:** Danh sách đơn hàng đã đặt phân theo tab: *Tất cả*, *Đang giao*, *Đã hoàn thành*, *Đã hủy*.
- **Tiêu chí nghiệm thu:**
  - Nút *"Đặt lại đơn này"*: Kích hoạt `SmartReOrderUseCase`, tự động nạp lại đúng các món vào giỏ hàng và điều hướng thẳng sang giỏ hàng.
- **Xác minh:** Test flow đặt lại đơn cũ thành công.
- **Phụ thuộc:** Task 5.1.
- **Files tác động:**
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/OrderHistoryViewModel.kt`
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/OrderHistoryScreen.kt`

### Task 5.3: Màn Hình Đánh Giá Đơn Hàng (`feature:rating`)
- **Quy mô:** S (2 files)
- **Mục tiêu:** Màn hình chấm điểm 1–5 sao cho nhà hàng và shipper sau khi đơn hàng `DELIVERED`.
- **Tiêu chí nghiệm thu:**
  - Chọn nhanh các tag nhận xét (*"Giao nhanh"*, *"Món ăn nóng hổi"*, *"Đóng gói kỹ"*).
  - Tùy chọn gửi đánh giá ẩn danh (Anonymous).
- **Xác minh:** Test gửi đánh giá thành công và cập nhật trạng thái đơn hàng.
- **Phụ thuộc:** Task 5.2.
- **Files tác động:**
  - `feature/rating/src/main/kotlin/com/bitefast/feature/rating/RatingViewModel.kt`
  - `feature/rating/src/main/kotlin/com/bitefast/feature/rating/RatingScreen.kt`

> 🏁 **CHECKPOINT 5:** 100% các tính năng nghiệp vụ người dùng từ lúc mở app đến khi nhận món và đánh giá đều hoàn tất.

---

## GIAI ĐOẠN 6: Accessibility WCAG 2.1 AA, NDK Hardening, Kover CI Gate ≥ 85% & R8
*Mục tiêu: Tối ưu hóa toàn diện để đạt điểm tuyệt đối 10/10 trên tất cả các tiêu chí phi chức năng.*

### Task 6.1: NDK C++ JNI Che Giấu Khóa Nhạy Cảm & Chống Dò Quét (Anti-Tamper)
- **Quy mô:** S (3 files)
- **Mục tiêu:** Ẩn toàn bộ API Key (Google Maps, Payment Gateway) vào thư viện native C++ với thuật toán XOR Masking theo Phần B+++.1 của đặc tả.
- **Tiêu chí nghiệm thu:**
  - Decompile file APK bằng `jadx` không thể tìm thấy API Key dạng plain-text.
  - Tích hợp kiểm tra thiết bị Rooted và chạy trên máy ảo bằng thư viện RootBeer.
- **Xác minh:** Chạy kiểm tra native library load thành công và giải mã khóa đúng tại runtime.
- **Phụ thuộc:** Checkpoint 5.
- **Files tác động:**
  - `app/src/main/cpp/native-lib.cpp`
  - `app/src/main/cpp/CMakeLists.txt`
  - `core/common/src/main/kotlin/com/bitefast/core/common/security/NativeSecurity.kt`

### Task 6.2: Kiểm Thử Toàn Diện Accessibility (A11y WCAG 2.1 AA)
- **Quy mô:** M (Đánh giá trên toàn bộ màn hình)
- **Mục tiêu:** Rà soát và đảm bảo mọi phần tử tương tác đều đạt chuẩn tiếp cận:
  - Touch target tối thiểu **48dp × 48dp**.
  - Phóng to cỡ chữ hệ thống **200%** không làm tràn hay mất chữ.
  - Tương phản màu sắc (Color Contrast) đạt tối thiểu **4.5:1** cho cả Light và Dark Theme.
- **Tiêu chí nghiệm thu:**
  - 100% màn hình vượt qua kiểm tra của Accessibility Scanner.
- **Xác minh:** Kiểm thử thực tế với chế độ TalkBack trên thiết bị Android.
- **Phụ thuộc:** Checkpoint 5.

### Task 6.3: Thiết Lập Chốt Chặn Kover Code Coverage CI Gate ≥ 85%
- **Quy mô:** S (2 files)
- **Mục tiêu:** Cấu hình Kover chốt chặn chất lượng: Từ chối build nếu độ bao phủ mã nguồn không đạt:
  - `core:domain`: **≥ 95%**
  - Toàn bộ ứng dụng: **≥ 85%**
- **Tiêu chí nghiệm thu:**
  - Chạy `./gradlew koverHtmlReport` xuất báo cáo xanh toàn diện.
- **Xác minh:** Lệnh Gradle verification chạy thành công.
- **Phụ thuộc:** Task 6.2.
- **Files tác động:**
  - `build.gradle.kts`
  - `core/testing/build.gradle.kts`

### Task 6.4: Cấu Hình Tối Ưu Hóa & Làm Mờ Mã Nguồn R8 / ProGuard
- **Quy mô:** S (1 file)
- **Mục tiêu:** Cấu hình [app/proguard-rules.pro](../../app/proguard-rules.pro) với các quy tắc chuyên sâu: repackage class, xóa bỏ mã `android.util.Log` trong bản Release, bảo vệ Data Models của Kotlinx Serialization và Room Entities.
- **Tiêu chí nghiệm thu:**
  - Bản build release `assembleRelease` thành công, kích thước APK được tối ưu hóa tối đa, bytecode được làm mờ an toàn.
- **Xác minh:** Kiểm tra mapping file và chạy APK release trên thiết bị thử nghiệm.
- **Phụ thuộc:** Task 6.3.
- **Files tác động:**
  - `app/proguard-rules.pro`

> 🏁 **CHECKPOINT 6 (DỰ ÁN ĐẠT ĐIỂM 10/10 TOÀN DIỆN):** Sản phẩm sẵn sàng xuất bản lên Google Play Store đạt chuẩn ngân hàng / thương mại điện tử quốc tế.

---

## ⚖️ ĐỀ XUẤT CỦA AI VỀ CHIẾN LƯỢC BẮT ĐẦU (RULE 4)

Để bắt đầu triển khai theo kế hoạch trên, tôi xin đề xuất 2 phương án thực thi để bạn lựa chọn:

### 🔹 Phương án 1 (Khuyến nghị - Recommended): Khởi động từ Giai đoạn 1 (Pure Domain UseCases)
- **Cách làm:** Tập trung hoàn thiện 100% logic kinh doanh cốt lõi (Cart, Voucher, Re-Order) bằng Pure Kotlin UseCases kèm bộ Unit Tests phủ ≥ 95%.
- **Ưu điểm:** 
  - Tuân thủ chuẩn mực Clean Architecture: Xây móng vững trước khi xây nhà.
  - Tốc độ phát triển cực nhanh vì chạy test trên máy ảo JVM thuần, không cần chờ Gradle Android build nặng nề.
  - Đảm bảo logic nghiệp vụ chính xác 100% trước khi ghép vào màn hình UI.
- **Nhược điểm:** Chưa thấy ngay giao diện mới trên màn hình điện thoại trong vài task đầu.

### 🔹 Phương án 2: Triển khai theo lát cắt dọc tính năng (Vertical Slice: Auth hoặc Detail)
- **Cách làm:** Chọn ngay 1 tính năng (ví dụ `feature:auth` hoặc `feature:detail`) và làm từ Domain ➔ Data ➔ Màn hình Compose để có thể nhìn thấy và tương tác ngay.
- **Ưu điểm:** Thấy ngay kết quả trực quan trên màn hình.
- **Nhược điểm:** Dễ phát sinh thay đổi ngược (rework) ở tầng Domain khi các tính năng khác cần tái sử dụng logic chung.

👉 **Bạn muốn chúng ta bắt đầu ngay theo Phương án 1 (Chuẩn hóa Domain UseCases) hay Phương án 2 (Làm trực quan từng màn hình)?**
