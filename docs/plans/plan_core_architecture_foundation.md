# 🛠️ KẾ HOẠCH HÀNH ĐỘNG CHI TIẾT BỔ SUNG CÁC THÀNH PHẦN CÒN THIẾU (BITEFAST)

> **Mục tiêu:** Bổ sung toàn bộ các thành phần kiến trúc, hạ tầng build, bảo mật, dữ liệu và màn hình nghiệp vụ còn thiếu để đưa dự án đạt chuẩn **10/10 Enterprise Grade** theo đúng bản đặc tả kỹ thuật trong [bitefast_project_plan.md](file:///d:/Personal_Project/Mobile_Project/bitefast_project_plan.md).  
> **Nguyên tắc phân rã:** Tiếp cận từ gốc lên ngọn (Bottom-Up: Build System → Core Foundation → Data/Security → Domain → Feature Slices), chia nhỏ task thành các đơn vị công việc độc lập (kích thước S và M, từ 1–4 file/task), có tiêu chí nghiệm thu và bước xác minh rõ ràng.

---

## 🗺️ Đồ Thị Phụ Thuộc Triển Khai (Execution Dependency Graph)

```
[Phase 1: Build System & Root Config]
           │
           ▼
[Phase 2: Core Foundation & Design System]
           │
           ▼
[Phase 3: Core Data, Storage & Hardened Security]
           │
           ▼
[Phase 4: Pure Domain Layer & Use Cases]
           │
           ▼
[Phase 5: Navigation & Feature Vertical Slices]
           │
           ▼
[Phase 6: Testing Pyramid & Security Hardening]
```

---

## GIAI ĐOẠN 1: Chuẩn Hóa Hệ Thống Build & Cấu Hình Gốc (Phase 1)
*Mục tiêu: Đưa dự án về trạng thái biên dịch Gradle thành công (Green Build).*

### Task 1.1: Khởi tạo Gradle Version Catalog (`gradle/libs.versions.toml`)
- **Mô tả:** Tạo file Version Catalog chuẩn theo định nghĩa tại Phần C.1 của kế hoạch, chứa toàn bộ phiên bản (Kotlin 2.1, AGP 8.7, Compose BOM 2024.12.01, Hilt 2.53, Room 2.6.1, SQLCipher 4.5.4, Retrofit 2.11, OkHttp 4.12, JUnit 5, MockK, Turbine).
- **Tiêu chí nghiệm thu:**
  - File [gradle/libs.versions.toml](file:///d:/Personal_Project/Mobile_Project/gradle/libs.versions.toml) được tạo đúng cấu trúc `[versions]`, `[libraries]`, `[plugins]`.
  - Không còn dependency nào bị hardcode rải rác.
- **Xác minh:** Kiểm tra tính toàn vẹn cú pháp TOML.
- **Phụ thuộc:** Không.
- **Files tạo mới/sửa đổi:** `gradle/libs.versions.toml`
- **Quy mô:** S (1 file).

### Task 1.2: Khởi tạo Gradle Wrapper Script
- **Mô tả:** Bổ sung `gradle-wrapper.properties` (Gradle 8.9/8.10) cùng các script khởi động `gradlew` và `gradlew.bat` để đảm bảo dự án có thể build độc lập trên mọi môi trường và máy chủ CI/CD.
- **Tiêu chí nghiệm thu:**
  - Có file `gradle/wrapper/gradle-wrapper.properties` trỏ đúng Gradle distribution tương thích với AGP 8.7.0.
  - Có file chạy `gradlew` và `gradlew.bat` ở thư mục gốc.
- **Xác minh:** Chạy thử lệnh kiểm tra phiên bản Gradle wrapper.
- **Phụ thuộc:** Task 1.1.
- **Files tạo mới/sửa đổi:** `gradle/wrapper/gradle-wrapper.properties`, `gradlew`, `gradlew.bat`
- **Quy mô:** S (3 files).

### Task 1.3: Tái cấu trúc Gradle Convention Plugins (`build-logic`)
- **Mô tả:** 
  1. Cấu hình lại [build-logic/convention/build.gradle.kts](file:///d:/Personal_Project/Mobile_Project/build-logic/convention/build.gradle.kts) áp dụng `kotlin-dsl` và đăng ký plugin IDs chuẩn (`bitefast.android.application`, `bitefast.android.library`, `bitefast.android.compose`, `bitefast.android.hilt`).
  2. Sửa thứ tự `import` lên đầu file.
  3. Xóa bỏ `kapt` và cấu hình hoàn toàn bằng KSP 2.x.
  4. Loại bỏ thuộc tính đã lỗi thời `kotlinCompilerExtensionVersion = "1.7.0"` (thay bằng Kotlin 2.0+ Compose Compiler).
  5. Cập nhật `settings.gradle.kts` dùng `pluginManagement { includeBuild("build-logic") }`.
- **Tiêu chí nghiệm thu:**
  - Các plugin được đăng ký chuẩn với Gradle Plugin Manager.
  - Các module con có thể áp dụng plugin dạng: `plugins { id("bitefast.android.library") }`.
- **Xác minh:** Cấu hình build script không có warning/error cú pháp.
- **Phụ thuộc:** Task 1.1, Task 1.2.
- **Files tạo mới/sửa đổi:**
  - [settings.gradle.kts](file:///d:/Personal_Project/Mobile_Project/settings.gradle.kts)
  - [build-logic/convention/build.gradle.kts](file:///d:/Personal_Project/Mobile_Project/build-logic/convention/build.gradle.kts)
  - [build-logic/convention/AndroidApplicationConventionPlugin.kt](file:///d:/Personal_Project/Mobile_Project/build-logic/convention/AndroidApplicationConventionPlugin.kt)
  - [build-logic/convention/AndroidLibraryConventionPlugin.kt](file:///d:/Personal_Project/Mobile_Project/build-logic/convention/AndroidLibraryConventionPlugin.kt)
  - [build-logic/convention/AndroidComposeConventionPlugin.kt](file:///d:/Personal_Project/Mobile_Project/build-logic/convention/AndroidComposeConventionPlugin.kt)
  - [build-logic/convention/AndroidHiltConventionPlugin.kt](file:///d:/Personal_Project/Mobile_Project/build-logic/convention/AndroidHiltConventionPlugin.kt)
- **Quy mô:** M (6 files).

### Task 1.4: Sửa lỗi cú pháp Root [build.gradle.kts](file:///d:/Personal_Project/Mobile_Project/build.gradle.kts) & Chuẩn hóa Manifest
- **Mô tả:**
  1. Sửa lỗi chính tả `cconfigureKotlinCompiler()` và chuyển logic cấu hình compiler sang Kotlin DSL thuần (`tasks.withType<KotlinCompile>().configureEach { ... }`).
  2. Sửa lỗi đóng thẻ XML malformed trong [app/src/main/AndroidManifest.xml](file:///d:/Personal_Project/Mobile_Project/app/src/main/AndroidManifest.xml).
  3. Xóa bỏ 11 thẻ `<activity>` ảo không tồn tại, đưa app về cấu trúc Single-Activity Compose thuần nhất (`MainActivity`).
  4. Loại bỏ các quyền vi phạm chính sách Google Play (`REQUEST_INSTALL_PACKAGES`, `BIND_NOTIFICATION_LISTENER_SERVICE`).
  5. Tạo file [app/src/main/res/xml/network_security_config.xml](file:///d:/Personal_Project/Mobile_Project/app/src/main/res/xml/network_security_config.xml) và liên kết vào manifest (`android:networkSecurityConfig`).
- **Tiêu chí nghiệm thu:**
  - Root `build.gradle.kts` là 100% hợp lệ Kotlin DSL.
  - Manifest chuẩn XML, không chứa permission độc hại, có cấu hình Network Security.
- **Xác minh:** Manifest file validate hợp lệ, Gradle root script build sạch.
- **Phụ thuộc:** Task 1.3.
- **Files tạo mới/sửa đổi:**
  - [build.gradle.kts](file:///d:/Personal_Project/Mobile_Project/build.gradle.kts)
  - [app/src/main/AndroidManifest.xml](file:///d:/Personal_Project/Mobile_Project/app/src/main/AndroidManifest.xml)
  - `app/src/main/res/xml/network_security_config.xml`
- **Quy mô:** S (3 files).

> 🏁 **CHECKPOINT 1:** Sau Phase 1, toàn bộ hệ thống Gradle sync thành công, Gradle Wrapper sẵn sàng, không còn lỗi cú pháp kts hoặc xml.

---

## GIAI ĐOẠN 2: Nền Tảng Cốt Lõi (Core Foundation) & Design System (Phase 2)

### Task 2.1: Hoàn thiện MVI Contract & BaseViewModel tại `core:common`
- **Mô tả:** 
  1. Viết lại [core/common/.../BaseViewModel.kt](file:///d:/Personal_Project/Mobile_Project/core/common/src/main/kotlin/com/bitefast/core/common/BaseViewModel.kt) kế thừa `androidx.lifecycle.ViewModel()`.
  2. Quản lý trạng thái bằng `MutableStateFlow<S>` + `StateFlow<S>`.
  3. Quản lý sự kiện 1 lần (One-shot effect) bằng `Channel<F>(Channel.BUFFERED)` + `Flow<F>` (`_effect.receiveAsFlow()`).
  4. Tích hợp `SavedStateHandle` an toàn cho Process Death.
  5. Định nghĩa contract `UiState`, `UiEvent`, `UiEffect` bất biến (`@Immutable`).
- **Tiêu chí nghiệm thu:**
  - Không kế thừa `AndroidViewModel`, không tạo `Application()` thủ công.
  - Loại bỏ hoàn toàn `LiveData` khỏi BaseViewModel.
- **Xác minh:** Unit test cho BaseViewModel với Turbine và TestScope.
- **Phụ thuộc:** Checkpoint 1.
- **Files tạo mới/sửa đổi:**
  - [core/common/src/main/kotlin/com/bitefast/core/common/BaseViewModel.kt](file:///d:/Personal_Project/Mobile_Project/core/common/src/main/kotlin/com/bitefast/core/common/BaseViewModel.kt)
  - `core/common/src/main/kotlin/com/bitefast/core/common/UiContract.kt`
- **Quy mô:** S (2 files).

### Task 2.2: Phân rã Utilities & Triển khai Coroutine Dispatchers
- **Mô tả:** 
  1. Xóa bỏ God Object [CommonUtils.kt](file:///d:/Personal_Project/Mobile_Project/core/common/src/main/kotlin/com/bitefast/core/common/CommonUtils.kt).
  2. Tạo module `core/common/dispatcher/BiteFastDispatchers.kt` cung cấp `@Dispatcher(IO)`, `@Dispatcher(Default)` cho Dependency Injection (không hardcode `Dispatchers.IO`).
  3. Tạo `core/common/extension/CurrencyExt.kt`, `ValidationExt.kt`, `LocationExt.kt` (có đầy đủ `import kotlin.math.*`).
  4. Tạo `core/common/network/NetworkMonitor.kt` (Flow giám sát trạng thái kết nối mạng realtime bằng `ConnectivityManager.NetworkCallback`).
- **Tiêu chí nghiệm thu:**
  - Không còn static scope hay leak context trong `core:common`.
  - Các hàm tính toán khoảng cách và validate hoạt động chính xác.
- **Xác minh:** Viết Unit Test cho CurrencyExt và LocationExt.
- **Phụ thuộc:** Task 2.1.
- **Files tạo mới/sửa đổi:**
  - `core/common/src/main/kotlin/com/bitefast/core/common/dispatcher/BiteFastDispatchers.kt`
  - `core/common/src/main/kotlin/com/bitefast/core/common/extension/CurrencyExt.kt`
  - `core/common/src/main/kotlin/com/bitefast/core/common/extension/LocationExt.kt`
  - `core/common/src/main/kotlin/com/bitefast/core/common/extension/ValidationExt.kt`
  - `core/common/src/main/kotlin/com/bitefast/core/common/network/NetworkMonitor.kt`
- **Quy mô:** M (5 files).

### Task 2.3: Chuẩn hóa Model & Result Layer tại `core:model`
- **Mô tả:** 
  1. Chuyển [core/model/.../Result.kt](file:///d:/Personal_Project/Mobile_Project/core/model/src/main/kotlin/com/bitefast/core/model/Result.kt) thành `sealed interface Result<out T>` (`Success<T>`, `Error(val exception: Throwable)`, `Loading`).
  2. Bổ sung extension helper: `asResult()`, `onSuccess()`, `onError()`.
  3. Làm sạch [core/model/.../Model.kt](file:///d:/Personal_Project/Mobile_Project/core/model/src/main/kotlin/com/bitefast/core/model/Model.kt): Tách biệt domain entity thuần, loại bỏ các getter format giao diện tiếng Việt (đưa vào Presentation/UiModel sau này), sửa lỗi enum `OrderStatus.displayName` và thiếu `min()`.
- **Tiêu chí nghiệm thu:**
  - `Result<T>` hỗ trợ `when` exhaustive không cần else nhánh null.
  - [Model.kt](file:///d:/Personal_Project/Mobile_Project/core/model/src/main/kotlin/com/bitefast/core/model/Model.kt) biên dịch không lỗi.
- **Xác minh:** Compile `core:model` sạch không warning.
- **Phụ thuộc:** Checkpoint 1.
- **Files tạo mới/sửa đổi:**
  - [core/model/src/main/kotlin/com/bitefast/core/model/Result.kt](file:///d:/Personal_Project/Mobile_Project/core/model/src/main/kotlin/com/bitefast/core/model/Result.kt)
  - [core/model/src/main/kotlin/com/bitefast/core/model/Model.kt](file:///d:/Personal_Project/Mobile_Project/core/model/src/main/kotlin/com/bitefast/core/model/Model.kt)
- **Quy mô:** S (2 files).

### Task 2.4: Hoàn thiện Design System & Accessible Components (`core:designsystem`)
- **Mô tả:**
  1. Xây dựng Design System Material 3 (`BiteFastTheme`, `Color`, `Typography`, `Shape`).
  2. Cài đặt hiệu ứng `Modifier.shimmerBrush(...)` với chuyển động linear gradient mượt mà (Tween 1200ms) theo đúng Phần B++++++.1.
  3. Triển khai các component đạt chuẩn Accessibility WCAG 2.1 AA:
     - `RestaurantCard.kt` (hợp nhất TalkBack semantics qua `clearAndSetSemantics`).
     - `QuantitySelector.kt` (touch target ≥ 48dp, custom actions tăng/giảm số lượng cho TalkBack).
     - `OfflineBanner.kt` (banner tự động đổi màu cam/xanh theo trạng thái mạng).
     - `BiteFastButton.kt`, `EmptyState.kt`, `ErrorState.kt` (có Actionable Retry button).
- **Tiêu chí nghiệm thu:**
  - Không còn `@Composable` rỗng `{ }`.
  - Mọi nút bấm đều đảm bảo kích thước tối thiểu 48x48dp.
- **Xác minh:** Compose Preview hiển thị chính xác cả Light và Dark Theme.
- **Phụ thuộc:** Task 2.2.
- **Files tạo mới/sửa đổi:**
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/theme/Theme.kt`
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/theme/Color.kt`
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/ShimmerEffect.kt`
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/RestaurantCard.kt`
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/QuantitySelector.kt`
  - `core/designsystem/src/main/kotlin/com/bitefast/core/designsystem/component/StateViews.kt`
- **Quy mô:** L (6 files).

> 🏁 **CHECKPOINT 2:** `core:common`, `core:model`, `core:designsystem` hoàn thành và biên dịch sạch. BaseViewModel và Design System sẵn sàng phục vụ các tầng trên.

---

## GIAI ĐOẠN 3: Tầng Dữ Liệu & Bảo Mật Doanh Nghiệp (Phase 3)

### Task 3.1: Mã Hóa DataStore (`core:datastore`)
- **Mô tả:** 
  1. Cấu hình DataStore Preferences kết hợp Jetpack Security `MasterKey` chuẩn `AES256_GCM`.
  2. Viết `AuthPreferencesDataSource` lưu trữ an toàn `accessToken`, `refreshToken`, `userId`, `isGuest`.
  3. Cung cấp Flow phản ánh trạng thái đăng nhập cho toàn ứng dụng.
- **Tiêu chí nghiệm thu:**
  - Dữ liệu token không lưu dưới dạng plain-text trên bộ nhớ trong thiết bị.
  - Hỗ trợ hàm xóa token an toàn khi người dùng đăng xuất.
- **Xác minh:** Viết Unit Test với in-memory datastore.
- **Phụ thuộc:** Checkpoint 2.
- **Files tạo mới/sửa đổi:**
  - `core/datastore/build.gradle.kts`
  - `core/datastore/src/main/kotlin/com/bitefast/core/datastore/di/DataStoreModule.kt`
  - `core/datastore/src/main/kotlin/com/bitefast/core/datastore/AuthPreferencesDataSource.kt`
- **Quy mô:** S (3 files).

### Task 3.2: Cơ Sở Dữ Liệu Cục Bộ Room Mã Hóa Bằng SQLCipher (`core:database`)
- **Mô tả:**
  1. Cấu hình Room Database với SQLCipher AES-256 (`SupportOpenHelperFactory`).
  2. Tạo khóa passphrase an toàn sinh qua Android Keystore phần cứng (TEE/StrongBox).
  3. Định nghĩa Entities và DAOs:
     - `CartDao` & `CartItemEntity` (có `@Transaction` atomic read-modify-write).
     - `RestaurantDao` & `RestaurantEntity`.
     - `OrderDao` & `OrderEntity`.
- **Tiêu chí nghiệm thu:**
  - File database `bitefast.db` được mã hóa hoàn toàn; không thể mở bằng SQLite reader thông thường nếu không có khóa.
- **Xác minh:** AndroidJUnit4 Test với in-memory encrypted Room database.
- **Phụ thuộc:** Checkpoint 2.
- **Files tạo mới/sửa đổi:**
  - `core/database/build.gradle.kts`
  - `core/database/src/main/kotlin/com/bitefast/core/database/BiteFastDatabase.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/security/KeystoreManager.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/dao/CartDao.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/dao/RestaurantDao.kt`
- **Quy mô:** M (5 files).

### Task 3.3: Network Client Kiên Cố Hóa (`core:network`)
- **Mô tả:**
  1. Cấu hình OkHttp với Certificate Pinning (HPKP/SPKI SHA-256) chống tấn công Man-in-the-Middle (MITM).
  2. Cài đặt `AuthInterceptor` tự động gắn `Authorization: Bearer <accessToken>`.
  3. Cài đặt `TokenAuthenticator` (OkHttp Authenticator): Khi nhận HTTP 401, sử dụng Coroutine `Mutex` để chặn race-condition, gọi refresh token ngầm một lần duy nhất rồi tự động retry request ban đầu.
  4. Cấu hình Retrofit với `KotlinxSerializationConverter`.
- **Tiêu chí nghiệm thu:**
  - Token refresh diễn ra trong suốt với người dùng.
  - Các request đồng thời khi gặp 401 không tạo ra bão refresh token (Token flood).
- **Xác minh:** Integration test với `MockWebServer` mô phỏng phản hồi 401 → 200 refresh → 200 retried.
- **Phụ thuộc:** Task 3.1.
- **Files tạo mới/sửa đổi:**
  - `core/network/build.gradle.kts`
  - `core/network/src/main/kotlin/com/bitefast/core/network/di/NetworkModule.kt`
  - `core/network/src/main/kotlin/com/bitefast/core/network/interceptor/AuthInterceptor.kt`
  - `core/network/src/main/kotlin/com/bitefast/core/network/authenticator/TokenAuthenticator.kt`
  - `core/network/src/main/kotlin/com/bitefast/core/network/api/BiteFastApiService.kt`
- **Quy mô:** M (5 files).

### Task 3.4: Triển Khai Repository & Mappers Tại `core:data`
- **Mô tả:**
  1. Triển khai các Repository (Single Source of Truth) kết hợp Room + Retrofit:
     - `CartRepositoryImpl`: Sử dụng `Mutex` để bảo vệ chống Race Condition khi người dùng bấm tăng/giảm giỏ hàng liên tục.
     - `RestaurantRepositoryImpl`: Cache danh sách nhà hàng offline.
     - `AuthRepositoryImpl`: Xử lý đăng nhập, đăng ký, đồng bộ Guest Mode.
     - `OrderRepositoryImpl`: Quản lý tạo đơn, lịch sử đơn và smart re-order.
  2. Xây dựng Bidirectional Mappers (DTO ↔ Entity ↔ Domain).
- **Tiêu chí nghiệm thu:**
  - Không có tình trạng lệch dữ liệu giỏ hàng khi tương tác nhanh (Race-free).
  - Tách bạch hoàn toàn Entity (DB) và Model (Domain).
- **Xác minh:** Unit test cho `CartRepositoryImpl` với Coroutine TestScope.
- **Phụ thuộc:** Task 3.2, Task 3.3.
- **Files tạo mới/sửa đổi:**
  - `core/data/build.gradle.kts`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/CartRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/RestaurantRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/AuthRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/mapper/Mappers.kt`
- **Quy mô:** M (5 files).

> 🏁 **CHECKPOINT 3:** Tầng Data Layer và Security hoàn tất. Đã có khả năng lưu trữ mã hóa, mạng chống MITM, tự động refresh phiên và bảo vệ ghi dữ liệu giỏ hàng.

---

## GIAI ĐOẠN 4: Tầng Nghiệp Vụ Thuần Khiết (Pure Domain Layer) (Phase 4)

### Task 4.1: Chuyển Đổi `core:domain` Sang Pure Kotlin JVM
- **Mô tả:**
  1. Chỉnh sửa [core/domain/build.gradle.kts](file:///d:/Personal_Project/Mobile_Project/core/domain/build.gradle.kts) loại bỏ `com.android.library`, chuyển sang `org.jetbrains.kotlin.jvm`.
  2. Đảm bảo module không phụ thuộc bất kỳ class Android SDK nào (`Context`, `Bundle`, `Looper`...).
  3. Định nghĩa hợp đồng Repository Interfaces (`CartRepository`, `RestaurantRepository`, `AuthRepository`, `OrderRepository`).
- **Tiêu chí nghiệm thu:**
  - Module `core:domain` compile độc lập siêu tốc không cần Android Gradle Plugin.
- **Xác minh:** Kiểm tra dependency tree của `core:domain`.
- **Phụ thuộc:** Checkpoint 3.
- **Files tạo mới/sửa đổi:**
  - [core/domain/build.gradle.kts](file:///d:/Personal_Project/Mobile_Project/core/domain/build.gradle.kts)
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/repository/RepositoryInterfaces.kt`
- **Quy mô:** S (2 files).

### Task 4.2: Xây Dựng Bộ UseCases Cho Giỏ Hàng & Nhà Hàng
- **Mô tả:**
  1. `AddToCartUseCase`: Kiểm tra xung đột nhà hàng (Conflict Detection - giỏ hàng chỉ chứa món từ 1 nhà hàng tại một thời điểm).
  2. `GetCartUseCase`: Trả về Flow giỏ hàng realtime kèm tính toán tổng tiền.
  3. `UpdateCartItemQuantityUseCase`: Tăng, giảm, tự xóa khi số lượng về 0.
  4. `GetRestaurantsUseCase`: Lọc theo từ khóa, đánh giá, khoảng cách.
  5. `GetRestaurantDetailUseCase`: Lấy chi tiết kèm thực đơn theo danh mục.
- **Tiêu chí nghiệm thu:**
  - Xử lý xung đột trả về rõ ràng: `CartResult.Success` hoặc `CartResult.Conflict(currentRestaurant, newRestaurant)`.
- **Xác minh:** Unit tests cho tất cả 5 UseCases đạt độ bao phủ 100%.
- **Phụ thuộc:** Task 4.1.
- **Files tạo mới/sửa đổi:**
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/cart/AddToCartUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/cart/GetCartUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/cart/UpdateCartQuantityUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/restaurant/GetRestaurantsUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/restaurant/GetRestaurantDetailUseCase.kt`
- **Quy mô:** M (5 files).

### Task 4.3: Xây Dựng Bộ UseCases Cho Auth, Checkout & Order
- **Mô tả:**
  1. `LoginUseCase` & `RegisterUseCase` & `LogoutUseCase`.
  2. `CheckoutOrderUseCase`: Xác thực địa chỉ, giỏ hàng, áp mã khuyến mãi voucher.
  3. `ReOrderUseCase`: Kiểm tra quán còn mở cửa không, món còn hàng không và nạp lại vào giỏ hàng.
  4. `GetOrderHistoryUseCase`: Phân loại đơn hàng đang giao / đã hoàn thành.
- **Tiêu chí nghiệm thu:**
  - Logic tính toán tiền giảm giá voucher đúng quy tắc (phần trăm, tối đa, đơn tối thiểu).
- **Xác minh:** Unit test cho Voucher Engine và ReOrder logic.
- **Phụ thuộc:** Task 4.1.
- **Files tạo mới/sửa đổi:**
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/auth/AuthUseCases.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/order/CheckoutOrderUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/order/ReOrderUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/voucher/ApplyVoucherUseCase.kt`
- **Quy mô:** M (4 files).

> 🏁 **CHECKPOINT 4:** Toàn bộ Core Business Logic được đóng gói vào Pure Kotlin UseCases với Unit Tests đầy đủ (JUnit 5 + MockK + Turbine).

---

## GIAI ĐOẠN 5: Tính Năng & Màn Hình Giao Diện (Feature Vertical Slices) (Phase 5)

### Task 5.1: Thiết Lập Điều Hướng Trung Tâm (Navigation Host tại `app`)
- **Mô tả:**
  1. Cấu hình Single Activity [MainActivity.kt](file:///d:/Personal_Project/Mobile_Project/app/src/main/kotlin/com/bitefast/app/MainActivity.kt) với Hilt `@AndroidEntryPoint`.
  2. Xây dựng `BiteFastNavHost` dùng Type-safe Navigation Compose (`navigation = 2.8.4`).
  3. Triển khai `BottomNavigationBar` (Khám phá, Giỏ hàng, Đơn hàng, Hồ sơ) tự động ẩn hiện theo màn hình.
  4. Tích hợp Offline Banner trên thanh trạng thái.
- **Tiêu chí nghiệm thu:**
  - Chuyển màn hình mượt mà, backstack được lưu trữ chính xác khi chuyển tab.
- **Xác minh:** Chạy app trên máy ảo/thiết bị thật, chuyển hướng không bị crash.
- **Phụ thuộc:** Checkpoint 4.
- **Files tạo mới/sửa đổi:**
  - [app/src/main/kotlin/com/bitefast/app/MainActivity.kt](file:///d:/Personal_Project/Mobile_Project/app/src/main/kotlin/com/bitefast/app/MainActivity.kt)
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`
  - `app/src/main/kotlin/com/bitefast/app/navigation/TopLevelDestination.kt`
- **Quy mô:** S (3 files).

### Task 5.2: Triển Khai `feature:discovery` (Trang Chủ & Tìm Kiếm)
- **Mô tả:**
  1. Tạo `DiscoveryViewModel` kế thừa `BaseViewModel` (`DiscoveryUiState`, `DiscoveryUiEvent`, `DiscoveryUiEffect`).
  2. Tạo `DiscoveryScreen`: Header địa chỉ, thanh tìm kiếm debounced 300ms, danh mục món ăn dạng Carousel, danh sách nhà hàng với Shimmer Loading Skeleton khi tải dữ liệu.
  3. Hỗ trợ Pull-to-refresh.
- **Tiêu chí nghiệm thu:**
  - Không giật lag khung hình khi cuộn (60/120fps).
  - TalkBack đọc trọn vẹn thông tin thẻ nhà hàng.
- **Xác minh:** Compose Preview và Robot Test cho DiscoveryScreen.
- **Phụ thuộc:** Task 5.1.
- **Files tạo mới/sửa đổi:**
  - `feature/discovery/build.gradle.kts`
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryViewModel.kt`
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryScreen.kt`
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/component/CategoryChips.kt`
- **Quy mô:** M (4 files).

### Task 5.3: Triển Khai `feature:cart` & Xử Lý Xung Đột Nhà Hàng
- **Mô tả:**
  1. Tạo `CartViewModel` & `CartScreen`.
  2. Hiển thị danh sách món đã chọn, điều khiển số lượng bằng `QuantitySelector`.
  3. Bảng tổng kết chi phí: Tạm tính, Phí giao hàng, Giảm giá, Tổng cộng.
  4. Hiển thị `RestaurantConflictDialog` khi người dùng cố thêm món từ nhà hàng khác: Cho phép chọn *"Tạo giỏ hàng mới"* (xóa món cũ) hoặc *"Giữ giỏ hàng hiện tại"*.
- **Tiêu chí nghiệm thu:**
  - Thao tác bấm nút +/- liên tục không sinh race-condition.
  - Xung đột nhà hàng được cảnh báo rõ ràng.
- **Xác minh:** Compose UI Test kịch bản xung đột giỏ hàng.
- **Phụ thuộc:** Task 5.1.
- **Files tạo mới/sửa đổi:**
  - `feature/cart/build.gradle.kts`
  - `feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartViewModel.kt`
  - `feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartScreen.kt`
  - `feature/cart/src/main/kotlin/com/bitefast/feature/cart/component/ConflictDialog.kt`
- **Quy mô:** M (4 files).

### Task 5.4: Triển Khai `feature:checkout` & Xác Thực Sinh Trắc Học
- **Mô tả:**
  1. Tạo `CheckoutViewModel` & `CheckoutScreen`.
  2. Lựa chọn địa chỉ giao hàng và phương thức thanh toán (Tiền mặt, Thẻ, Ví điện tử).
  3. Áp dụng mã khuyến mãi voucher (`VoucherBottomSheet`).
  4. Tích hợp `BiometricAuthenticator` (`BiometricPrompt` cấp độ `BIOMETRIC_STRONG` - Keystore Hardware) khi đơn hàng vượt hạn mức hoặc yêu cầu thanh toán thẻ.
- **Tiêu chí nghiệm thu:**
  - Hỗ trợ fallback sang mã PIN/Passcode hệ thống nếu thiết bị không có vân tay.
  - Ngăn chặn click đúp nút "Đặt hàng" (Debounced checkout button).
- **Xác minh:** Test flow thanh toán thành công và từ chối sinh trắc học.
- **Phụ thuộc:** Task 5.3.
- **Files tạo mới/sửa đổi:**
  - `feature/checkout/build.gradle.kts`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/CheckoutViewModel.kt`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/CheckoutScreen.kt`
  - `feature/checkout/src/main/kotlin/com/bitefast/feature/checkout/security/BiometricHandler.kt`
- **Quy mô:** M (4 files).

### Task 5.5: Triển Khai `feature:tracking` (Bản Đồ Realtime & Shipper Simulation)
- **Mô tả:**
  1. Tạo `TrackingViewModel` & `TrackingScreen` tích hợp Google Maps Compose (`maps-compose = 6.2.1`).
  2. Vẽ Marker nhà hàng, khách hàng và shipper.
  3. Mô phỏng di chuyển mượt mà của tài xế bằng nội suy tọa độ (Polyline Spherical Linear Interpolation).
  4. Hiển thị các mốc trạng thái đơn hàng (`PREPARING`, `ON_THE_WAY`, `DELIVERED`).
- **Tiêu chí nghiệm thu:**
  - Bản đồ hiển thị mượt, marker tài xế xoay theo hướng di chuyển (bearing).
- **Xác minh:** Manual check trên máy ảo có GPS giả lập.
- **Phụ thuộc:** Task 5.4.
- **Files tạo mới/sửa đổi:**
  - `feature/tracking/build.gradle.kts`
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingViewModel.kt`
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingScreen.kt`
- **Quy mô:** S (3 files).

### Task 5.6: Triển Khai `feature:auth`, `feature:order`, `feature:profile`, `feature:rating`
- **Mô tả:**
  1. `feature/auth`: Đăng nhập/Đăng ký kèm chế độ Khách (Guest Mode) và `LoginGateBottomSheet`.
  2. `feature/order`: Lịch sử đơn hàng có bộ lọc và tính năng **Smart Re-Order** (1 chạm nạp lại vào giỏ).
  3. `feature/profile`: Xem thông tin cá nhân, quản lý sổ địa chỉ giao hàng.
  4. `feature/rating`: Đánh giá 1-5 sao, chọn tag nhận xét và gửi phản hồi.
- **Tiêu chí nghiệm thu:**
  - Khách vãng lai dùng được trang chủ, khi ấn thanh toán thì bung `LoginGateBottomSheet`.
  - Smart Re-Order khôi phục chính xác các món và tùy chọn của đơn trước.
- **Xác minh:** End-to-end testing luồng đặt hàng → nhận hàng → đánh giá → đặt lại.
- **Phụ thuộc:** Task 5.1 đến Task 5.5.
- **Files tạo mới/sửa đổi:**
  - Các file ViewModel và Screen tương ứng trong 4 feature modules.
- **Quy mô:** L (8 files).

> 🏁 **CHECKPOINT 5:** 100% các phân hệ tính năng và luồng người dùng hoạt động trơn tru từ đầu đến cuối.

---

## GIAI ĐOẠN 6: Kiểm Thử Tự Động, Tối Ưu Hóa & Security Hardening (Phase 6)

### Task 6.1: Cấu Hình NDK C++ Bảo Vệ API Key & Anti-Tamper
- **Mô tả:**
  1. Tạo file C++ JNI `app/src/main/cpp/native-lib.cpp` che giấu API Key qua thuật toán XOR Masking theo Phần B+++.1.
  2. Tích hợp Secrets Gradle Plugin (`secrets-gradle-plugin`).
  3. Bổ sung `RootBeer` kiểm tra thiết bị Rooted và chạy trên môi trường giả lập (Emulator).
- **Tiêu chí nghiệm thu:**
  - Không có khóa API Key hoặc endpoint nhạy cảm dạng plain-text trong file `.class` hay `strings.xml`.
- **Xác minh:** Kiểm tra file APK sau build bằng lệnh decompile `jadx`.
- **Phụ thuộc:** Checkpoint 5.
- **Files tạo mới/sửa đổi:**
  - `app/src/main/cpp/native-lib.cpp`
  - `app/src/main/cpp/CMakeLists.txt`
  - `core/common/src/main/kotlin/com/bitefast/core/common/security/NativeSecurity.kt`
- **Quy mô:** S (3 files).

### Task 6.2: Kiểm Thử Kim Tự Tháp (Test Pyramid) & JaCoCo/Kover CI Gate
- **Mô tả:**
  1. Cấu hình Kover (`kover = 0.8.3`) chặn ngưỡng bao phủ mã: Domain Layer ≥ 95%, toàn bộ app ≥ 85%.
  2. Viết bộ Integration Tests với MockWebServer cho các trường hợp: 401 Unauthorized, Network Timeout, 500 Server Error.
  3. Viết Compose UI Semantics Tests cho các luồng then chốt.
- **Tiêu chí nghiệm thu:**
  - Toàn bộ test suite chạy pass 100%.
  - Báo cáo Kover HTML đạt trên 85% tổng thể.
- **Xác minh:** Chạy `./gradlew test koverHtmlReport`.
- **Phụ thuộc:** Checkpoint 5.
- **Files tạo mới/sửa đổi:**
  - `core/testing/build.gradle.kts`
  - Các file test trong `core/domain/src/test/...`, `core/data/src/test/...`, `feature/*/src/test/...`
- **Quy mô:** M (5+ files).

### Task 6.3: Cấu Hình ProGuard / R8 Dictionary Obfuscation
- **Mô tả:**
  1. Cấu hình [app/proguard-rules.pro](file:///d:/Personal_Project/Mobile_Project/app/proguard-rules.pro) chuyên sâu: repackage class, xóa bỏ mã `android.util.Log` trong bản Release, bảo vệ Data Models của Kotlinx Serialization và Room Entities.
  2. Giữ an toàn cho các native JNI methods.
- **Tiêu chí nghiệm thu:**
  - Bản build release `assembleRelease` thành công, kích thước APK được tối ưu hóa tối đa, bytecode được làm mờ an toàn.
- **Xác minh:** Kiểm tra mapping file và chạy APK release trên thiết bị thử nghiệm.
- **Phụ thuộc:** Task 6.1.
- **Files tạo mới/sửa đổi:**
  - `app/proguard-rules.pro`
- **Quy mô:** S (1 file).

> 🏁 **CHECKPOINT 6 (HOÀN THÀNH TOÀN DIỆN):** Dự án hoàn tất 100% 8 tiêu chí chuẩn Enterprise 10/10.

---

## ⚠️ Quản Trị Rủi Ro & Biện Pháp Giảm Thiểu

| Rủi ro kỹ thuật | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| **Xung đột phiên bản Gradle Plugin & Kotlin 2.1** | Cao | Khóa chặt phiên bản trong `libs.versions.toml`, sử dụng K2 compiler và KSP 2.1.0-1.0.29 tương thích chính xác. |
| **Race condition khi thêm/sửa giỏ hàng dồn dập** | Trung bình | Áp dụng 3 tầng phòng thủ: Debounce 300ms tại UI + `Mutex` trong Repository + Room `@Transaction` atomic. |
| **Mất kết nối mạng đột ngột giữa chừng** | Trung bình | Tích hợp cơ chế Exponential Backoff with Jitter cho Network Flow; tự động lưu cache trạng thái đơn xuống Room Database. |
| **Ứng dụng bị OS hủy tiến trình (Process Death)** | Thấp | 100% ViewModel kế thừa `BaseViewModel` phải nhận và lưu trạng thái vào `SavedStateHandle`. |

---

## ❓ Câu Hỏi Thảo Luận & Lựa Chọn Triển Khai (Open Questions)

1. **Thứ tự ưu tiên thực thi:** Bạn muốn chúng tôi bắt tay vào thực hiện ngay **Giai đoạn 1 (Sửa lỗi Build Gradle & Chuẩn hóa Version Catalog)** để dự án có thể biên dịch được trước, hay bạn muốn rà soát và điều chỉnh lại danh sách các task trên?
2. **Khóa API Google Maps & Firebase:** Đối với phân hệ Tracking và Notification, bạn đã có sẵn Google Maps API Key và file `google-services.json` chưa, hay chúng ta sẽ dùng mock service/fake polyline trước trong giai đoạn phát triển này?
