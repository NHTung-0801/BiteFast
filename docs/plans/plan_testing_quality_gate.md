# 🧪 BẢN KẾ HOẠCH NÂNG CẤP TESTING QUALITY GATE (80% ➔ 100%)

> **Tài liệu tham chiếu:** `docs/plans/plan_completion_roadmap.md` (Phase E)  
> **Trạng thái:** ✅ Đã hoàn thành 100% & Đạt chuẩn Quality Gate Enterprise (Completed & Verified)  
> **Mục tiêu:** Nâng độ hoàn thiện Testing từ 80% lên 100% chuẩn Enterprise, phủ kín 100% ViewModels, Data Repositories và Network Interceptors.

---

## 🎯 1. MỤC TIÊU & PHẠM VI (OBJECTIVE & SCOPE)

### 1.1 Hiện Trạng (80% Hoàn Thiện)
- **Đã có:** 
  - Toàn bộ UseCases của `:core:domain` (11 test suites) pass 100%.
  - `:core:network`: `TokenAuthenticatorTest` (MockWebServer) pass 100%.
  - `:core:data`: `AuthRepositoryTest` và `RestaurantRepositoryTest` pass 100%.
  - Presentation: Một số ViewModel phụ (`SearchViewModel`, `PaymentResultViewModel`, `OrderDetailViewModel`, `AddressViewModel`, `ForgotPasswordViewModel`, `AuthViewModel`, `VoucherViewModel`) đã có unit test.
- **Lỗ hổng kiểm thử còn lại (Khoảng trống 20%):**
  - **5 Feature Modules hoàn toàn chưa có Unit Test (`NO-SOURCE`):** `:feature:cart`, `:feature:detail`, `:feature:tracking`, `:feature:rating`, `:feature:notification`.
  - **4 Feature Modules thiếu kiểm thử cho ViewModel chính:**
    - `:feature:checkout`: Thiếu `CheckoutViewModelTest` (luồng đặt hàng, voucher, biometric, payment).
    - `:feature:discovery`: Thiếu `DiscoveryViewModelTest` (carousel banners, category selection, food feeds).
    - `:feature:order`: Thiếu `OrderViewModelTest` (active/history tab, re-order, cancellation).
    - `:feature:profile`: Thiếu `ProfileViewModelTest` (user profile state, logout, navigation shortcuts).
  - **5 Repositories Data Layer chưa có Unit Test:** `OrderRepositoryTest`, `CartRepositoryTest`, `VoucherRepositoryTest`, `RatingRepositoryTest`, `AddressRepositoryTest`.
  - **2 Network Components chưa có Unit Test:** `AuthInterceptorTest` (tự động gắn Bearer Header), `MockNetworkInterceptorTest` (kiểm tra status code, mock delay, JSON mapping).

### 1.2 Mục Tiêu Đạt 100% Quality Gate
1. Bổ sung trọn vẹn Unit Tests cho **tất cả 9 ViewModels còn lại** theo mô hình UDF/MVI StateFlow + SharedFlow Events + Turbine.
2. Bổ sung Unit Tests cho **tất cả 5 Data Repositories còn lại** kết hợp MockK kiểm tra luồng Offline-First (Room DB cache + Remote API fallback/sync).
3. Bổ sung Unit Tests cho **2 Network Interceptors** trong `:core:network`.
4. Cấu hình hoàn chỉnh test dependencies (`junit5`, `mockk`, `coroutines.test`, `turbine`) trong `build.gradle.kts` cho các modules còn thiếu (`:feature:cart`, `:feature:detail`, `:feature:tracking`, `:feature:rating`, `:feature:notification`).
5. Đảm bảo toàn bộ lệnh `./gradlew testDebugUnitTest` chạy thành công 100% trên cả 11 modules mà không còn bất kỳ task `NO-SOURCE` nào.

---

## 🏛️ 2. ĐỒ THỊ ẢNH HƯỞNG MODULE (MODULE IMPACT GRAPH)

```
[Presentation / Feature ViewModels]
  ├── :feature:cart         --> CartViewModelTest.kt
  ├── :feature:checkout     --> CheckoutViewModelTest.kt
  ├── :feature:discovery    --> DiscoveryViewModelTest.kt
  ├── :feature:detail       --> DetailViewModelTest.kt (Star rating + Dish detail)
  ├── :feature:order        --> OrderViewModelTest.kt
  ├── :feature:tracking     --> TrackingViewModelTest.kt (Polyline, GPS, Status update)
  ├── :feature:profile      --> ProfileViewModelTest.kt
  ├── :feature:rating       --> RatingViewModelTest.kt
  └── :feature:notification --> NotificationViewModelTest.kt
                │
                ▼ (gọi UseCases / Repositories)
[Data Layer Repositories]
  └── :core:data
        ├── OrderRepositoryTest.kt
        ├── CartRepositoryTest.kt
        ├── VoucherRepositoryTest.kt
        ├── RatingRepositoryTest.kt
        └── AddressRepositoryTest.kt
                │
                ▼ (gọi ApiService / OkHttp Interceptors)
[Network Layer Interceptors]
  └── :core:network
        ├── AuthInterceptorTest.kt
        └── MockNetworkInterceptorTest.kt
```

---

## 📋 3. PHÂN RÃ CÔNG VIỆC (TASK BREAKDOWN)

Chia thành các nhóm task kích thước **S** (1-2 files) hoặc **M** (3-4 files) thực hiện tuần tự Bottom-Up:

### 🔹 TASK 1: Bổ Sung Test Dependencies Cho Các Feature Modules [Kích thước: M]
- **Mô tả:** Cập nhật `build.gradle.kts` cho `:feature:cart`, `:feature:detail`, `:feature:tracking`, `:feature:rating`, `:feature:notification` để hỗ trợ JUnit5, MockK, Coroutines Test và Turbine, đồng thời kích hoạt `unitTests.all { it.useJUnitPlatform() }`.
- **Tiêu chí nghiệm thu (Acceptance Criteria):** Gradle sync thành công, các module này sẵn sàng biên dịch và chạy test JUnit5.
- **Bước xác minh (Verification):** `./gradlew assembleDebug` thành công.
- **Danh sách file tác động:**
  - `feature/cart/build.gradle.kts`
  - `feature/detail/build.gradle.kts`
  - `feature/tracking/build.gradle.kts`
  - `feature/rating/build.gradle.kts`
  - `feature/notification/build.gradle.kts`

---

### 🔹 TASK 2: Network Interceptors Unit Tests [Kích thước: S]
- **Mô tả:** Viết unit test cho `AuthInterceptor` và `MockNetworkInterceptor` trong `:core:network`.
  - `AuthInterceptorTest`: Kiểm tra việc tự động chèn `Authorization: Bearer <token>` vào request khi token tồn tại, và bỏ qua khi token rỗng/guest.
  - `MockNetworkInterceptorTest`: Kiểm tra interceptor trả về HTTP 200 kèm payload JSON hợp lệ cho các endpoints `/restaurants`, `/orders`, `/vouchers`, và xử lý URL không tồn tại.
- **Tiêu chí nghiệm thu:** Chạy pass 100% với JUnit5 & MockWebServer/MockOkHttp.
- **Bước xác minh:** `./gradlew :core:network:testDebugUnitTest`.
- **Danh sách file tác động:**
  - `core/network/src/test/kotlin/com/bitefast/core/network/interceptor/AuthInterceptorTest.kt`
  - `core/network/src/test/kotlin/com/bitefast/core/network/mock/MockNetworkInterceptorTest.kt`

---

### 🔹 TASK 3: Data Layer Core Repositories Unit Tests [Kích thước: M]
- **Mô tả:** Viết unit test cho `OrderRepositoryImpl` và `CartRepositoryImpl`.
  - `OrderRepositoryTest`: Kiểm tra `createOrder()` map chính xác DTO, lưu Order vào local Room DB và gọi Remote API; kiểm tra `getOrderHistory()` fallback Room DB khi offline; kiểm tra `cancelOrder()`.
  - `CartRepositoryTest`: Kiểm tra thêm món vào giỏ, cập nhật số lượng, xóa món, xóa toàn bộ giỏ, và đảm bảo Mutex thread-safety khi có nhiều coroutine cùng cập nhật giỏ hàng.
- **Tiêu chí nghiệm thu:** Sử dụng MockK mock ApiService và Room Dao, pass 100%.
- **Bước xác minh:** `./gradlew :core:data:testDebugUnitTest`.
- **Danh sách file tác động:**
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/OrderRepositoryTest.kt`
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/CartRepositoryTest.kt`

---

### 🔹 TASK 4: Data Layer Supporting Repositories Unit Tests [Kích thước: M]
- **Mô tả:** Viết unit test cho `VoucherRepositoryImpl`, `RatingRepositoryImpl`, và `AddressRepositoryImpl`.
  - `VoucherRepositoryTest`: Kiểm tra lấy danh sách vouchers từ API + cache, kiểm tra tìm kiếm theo code (`getVoucherByCode`), xử lý lỗi 404/hết hạn.
  - `RatingRepositoryTest`: Kiểm tra gửi đánh giá nhà hàng và món ăn (`submitRating`), mapping DTO sang Domain.
  - `AddressRepositoryTest`: Kiểm tra lấy danh sách địa chỉ, thêm địa chỉ mới, và đặt địa chỉ mặc định.
- **Tiêu chí nghiệm thu:** Pass 100% trên `:core:data`.
- **Bước xác minh:** `./gradlew :core:data:testDebugUnitTest`.
- **Danh sách file tác động:**
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/VoucherRepositoryTest.kt`
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/RatingRepositoryTest.kt`
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/AddressRepositoryTest.kt`

---

### 🔹 TASK 5: Presentation Tests - Discovery & Detail Modules [Kích thước: M]
- **Mô tả:** Viết unit test cho `DiscoveryViewModel` và `DetailViewModel`.
  - `DiscoveryViewModelTest`: Test tải danh sách Banner Carousel, danh mục (Categories), món ăn Top-rated, Nearby; xử lý lỗi mạng và trạng thái Refresh.
  - `DetailViewModelTest`: Test tải chi tiết nhà hàng và danh sách thực đơn; test chọn món ăn; test tính năng gửi đánh giá số sao riêng cho món ăn (Dish-Level Star Rating) và cập nhật danh sách đánh giá gần đây.
- **Tiêu chí nghiệm thu:** Sử dụng `TestDispatcher`, `StandardTestDispatcher` và Turbine để verify UI State Flow, pass 100%.
- **Bước xác minh:** `./gradlew :feature:discovery:testDebugUnitTest :feature:detail:testDebugUnitTest`.
- **Danh sách file tác động:**
  - `feature/discovery/src/test/kotlin/com/bitefast/feature/discovery/DiscoveryViewModelTest.kt`
  - `feature/detail/src/test/kotlin/com/bitefast/feature/detail/DetailViewModelTest.kt`

---

### 🔹 TASK 6: Presentation Tests - Cart & Checkout Modules [Kích thước: M]
- **Mô tả:** Viết unit test cho `CartViewModel` và `CheckoutViewModel`.
  - `CartViewModelTest`: Test phát ra danh sách món ăn trong giỏ, tăng/giảm số lượng, xóa món, tính tổng tiền giỏ hàng (subtotal, shipping, discount).
  - `CheckoutViewModelTest`: Test áp dụng voucher hợp lệ/không hợp lệ; test chọn phương thức thanh toán (COD, VietQR, MoMo); test đặt hàng thành công điều hướng sang Tracking/Payment Result.
- **Tiêu chí nghiệm thu:** Pass 100%, verify mọi SideEffect SharedFlow (NavigateToPayment, ShowToast).
- **Bước xác minh:** `./gradlew :feature:cart:testDebugUnitTest :feature:checkout:testDebugUnitTest`.
- **Danh sách file tác động:**
  - `feature/cart/src/test/kotlin/com/bitefast/feature/cart/CartViewModelTest.kt`
  - `feature/checkout/src/test/kotlin/com/bitefast/feature/checkout/CheckoutViewModelTest.kt`

---

### 🔹 TASK 7: Presentation Tests - Order & Tracking Modules [Kích thước: M]
- **Mô tả:** Viết unit test cho `OrderViewModel` và `TrackingViewModel`.
  - `OrderViewModelTest`: Test chuyển đổi tab Đang xử lý (Active) và Lịch sử (History); test kích hoạt Đặt lại thông minh (Smart Re-Order); test hủy đơn hàng.
  - `TrackingViewModelTest`: Test khởi tạo hành trình đơn hàng, luồng cập nhật trạng thái đơn (PREPARING ➔ PICKED_UP ➔ DELIVERING ➔ DELIVERED), giả lập tọa độ shipper và cập nhật lộ trình.
- **Tiêu chí nghiệm thu:** Pass 100% với Turbine coroutines flow testing.
- **Bước xác minh:** `./gradlew :feature:order:testDebugUnitTest :feature:tracking:testDebugUnitTest`.
- **Danh sách file tác động:**
  - `feature/order/src/test/kotlin/com/bitefast/feature/order/OrderViewModelTest.kt`
  - `feature/tracking/src/test/kotlin/com/bitefast/feature/tracking/TrackingViewModelTest.kt`

---

### 🔹 TASK 8: Presentation Tests - Profile, Rating & Notification Modules [Kích thước: M]
- **Mô tả:** Viết unit test cho `ProfileViewModel`, `RatingViewModel`, và `NotificationViewModel`.
  - `ProfileViewModelTest`: Test tải thông tin người dùng, trạng thái Guest mode, sự kiện Đăng xuất (Logout) xóa sạch token.
  - `RatingViewModelTest`: Test chọn số sao, nhập bình luận, gửi đánh giá thành công và điều hướng về Home.
  - `NotificationViewModelTest`: Test tải danh sách thông báo, đánh dấu đã đọc, xóa thông báo.
- **Tiêu chí nghiệm thu:** Pass 100%.
- **Bước xác minh:** `./gradlew :feature:profile:testDebugUnitTest :feature:rating:testDebugUnitTest :feature:notification:testDebugUnitTest`.
- **Danh sách file tác động:**
  - `feature/profile/src/test/kotlin/com/bitefast/feature/profile/ProfileViewModelTest.kt`
  - `feature/rating/src/test/kotlin/com/bitefast/feature/rating/RatingViewModelTest.kt`
  - `feature/notification/src/test/kotlin/com/bitefast/feature/notification/NotificationViewModelTest.kt`

---

### 🔹 TASK 9: Tổng Hợp & Đánh Giá Chất Lượng Toàn Diện (Final Quality Gate) [Kích thước: S] - ✅ HOÀN THÀNH 100%
- **Mô tả:** 
  - Chạy `./gradlew testDebugUnitTest` trên toàn bộ dự án (tất cả 11 modules).
  - Đảm bảo 100% tests pass (dự kiến >70 unit tests).
  - Không còn bất kỳ module nào bị `NO-SOURCE`.
  - Cập nhật tài liệu tiến độ `docs/plans/plan_completion_roadmap.md` nâng cột mốc `[Testing]` lên **100% ✅**.
- **Tiêu chí nghiệm thu:** `BUILD SUCCESSFUL`, 0 failure, 0 skipped, 0 flakiness.
- **Bước xác minh:** `./gradlew testDebugUnitTest` && `./gradlew :app:assembleDebug`.
- **Kết quả thực tế:**
  - Lệnh `./gradlew testDebugUnitTest` hoàn tất trong 45s: `BUILD SUCCESSFUL`.
  - Toàn bộ 11 modules (`:feature:auth`, `:feature:cart`, `:feature:checkout`, `:feature:detail`, `:feature:discovery`, `:feature:notification`, `:feature:order`, `:feature:profile`, `:feature:rating`, `:feature:tracking`, `:feature:voucher`, `:core:network`, `:core:data`, `:core:domain`) đều có test suites hợp lệ và pass 100%.
  - Zero module `NO-SOURCE`.
- **Danh sách file tác động:**
  - `docs/plans/plan_completion_roadmap.md`
  - `docs/plans/README.md`

---

## ⚠️ 4. RỦI RO & BIỆN PHÁP GIẢM THIỂU (RISKS & MITIGATIONS)

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| **Coroutine Main Dispatcher missing trong Unit Test** (`IllegalStateException: Module with the Main dispatcher had failed to initialize`) | Cao | Sử dụng `StandardTestDispatcher` kết hợp `Dispatchers.setMain(testDispatcher)` trong `@BeforeEach` và `Dispatchers.resetMain()` trong `@AfterEach` cho toàn bộ ViewModel tests. |
| **Turbine flow emission collection timeout** khi ViewModel sử dụng `stateIn(SharingStarted.WhileSubscribed)` | Trung bình | Kích hoạt `backgroundScope` hoặc gọi `testDispatcher.scheduler.runCurrent()` trước khi kiểm tra emission qua Turbine. |
| **MockK relaxed mock che giấu lỗi logic** | Thấp | Chỉ dùng `relaxed = true` cho các hàm lưu trữ/logging đơn giản (như DataStore), đối với usecases/repository trả về dữ liệu nghiệp vụ bắt buộc phải khai báo `coEvery { ... } returns ...` rõ ràng. |
| **Xung đột phiên bản JUnit giữa JUnit 4 và JUnit 5 (Jupiter)** | Trung bình | Đồng bộ hóa toàn bộ cấu hình `unitTests.all { it.useJUnitPlatform() }` trên tất cả các `build.gradle.kts` và chỉ import `org.junit.jupiter.api.*`. |

---

## ❓ 5. CÂU HỎI THẢO LUẬN & ĐỀ XUẤT PHƯƠNG ÁN (OPEN QUESTIONS)

### ⚖️ ĐỀ XUẤT CÁC PHƯƠNG ÁN XỬ LÝ:

#### 🔹 Phương án 1 (Khuyến nghị - Recommended): Nâng Cấp Toàn Diện Chuẩn Enterprise (Tất cả 11 Modules - Full Coverage)
- **Mô tả:** Triển khai đầy đủ từ Task 1 đến Task 9: Phủ trọn vẹn 100% các ViewModels (9 ViewModels còn lại), 100% Repositories (5 Repositories còn lại), và Interceptors mạng. Loại bỏ hoàn toàn trạng thái `NO-SOURCE`.
- **Ưu điểm:**
  - Đưa độ hoàn thiện Testing lên mức **100% tuyệt đối** chuẩn Enterprise Production.
  - Ngăn ngừa 100% các lỗi hồi quy (regression bugs) khi sau này gắn real backend hoặc thêm tính năng real-time (Phase D).
  - Codebase cực kỳ bền vững, dễ bảo trì, mọi hành vi UDF/MVI đều được bảo vệ bởi test tự động.
- **Nhược điểm:** Cần thời gian thực thi tuần tự nhiều task (9 tasks).
- **Lý do khuyến nghị:** BiteFast đã đạt ~95% hoàn thiện ở tất cả các tầng khác (Architecture, UI, Navigation, Backend Mock API). Việc hoàn thiện trọn vẹn Testing lên 100% là bước đệm hoàn hảo để dự án đạt độ chín muồi cao nhất.

#### 🔹 Phương án 2: Tập Trung Vào Core Business Flows (Chỉ Test ViewModels & Repositories Quan Trọng)
- **Mô tả:** Chỉ viết test cho các luồng nghiệp vụ cốt lõi phát sinh doanh thu: `CartViewModel`, `CheckoutViewModel`, `DiscoveryViewModel`, `OrderRepository`, `CartRepository` (bỏ qua Tracking GPS simulation, Notification và Interceptors).
- **Ưu điểm:** Triển khai nhanh hơn, tập trung ngay vào luồng giỏ hàng và thanh toán.
- **Nhược điểm:** Độ phủ chỉ đạt khoảng 90%, vẫn còn các modules bị `NO-SOURCE` (`feature:tracking`, `feature:notification`, `feature:rating`), không đạt tiêu chí 100% Quality Gate của dự án.

👉 **Bạn muốn chúng ta thực hiện theo Phương án 1 hay Phương án 2?**
