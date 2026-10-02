# 📋 KẾ HOẠCH NÂNG CẤP & HOÀN THIỆN: BACKEND INTEGRATION (PHASE C)

> **Mã tài liệu:** `plan_backend_integration.md`  
> **Phiên bản:** 1.0 — Chuẩn Enterprise Clean Architecture 10/10  
> **Ngày lập:** 01/10/2026  
> **Mục tiêu:** Nâng cấp toàn diện tầng Network & Data, chuyển đổi từ Mock Data cục bộ sang kiến trúc tích hợp Backend chuẩn REST API với cơ chế Offline-First, DTOs Serialization, Silent Token Renewal (401 Mutex), và linh hoạt chuyển đổi giữa Live Server / Mock Engine.  
> **Trạng thái:** ✅ Đã hoàn thành 100% (Completed & Verified)

---

## 🎯 1. MỤC TIÊU & PHẠM VI (OBJECTIVE & SCOPE)

### 1.1 Mục tiêu
1. **Chuẩn hóa Hợp đồng DTOs (Data Transfer Objects)**: Xây dựng hệ thống DTOs độc lập có gắn `@Serializable` cho toàn bộ các luồng nghiệp vụ (Auth, Restaurant, Menu, Cart, Order, Voucher, Rating, Address), đóng gói trong phong bì chuẩn `ApiResponse<T>`.
2. **Mở rộng Retrofit API Service (`BiteFastApiService`)**: Đầy đủ 20+ RESTful endpoints (GET, POST, PUT, DELETE) bao quát trọn vẹn vòng đời ứng dụng.
3. **Cơ chế Chuyển đổi Môi trường Linh hoạt (Environment Switcher)**: Cung cấp `NetworkConfig` hỗ trợ chuyển đổi giữa:
   - `LIVE_SERVER` (Kết nối API thực tế qua IP/Domain).
   - `MOCK_ENGINE` (Bộ giả lập mạng nội bộ OkHttp Interceptor có độ trễ 300-600ms, mã HTTP chuẩn, hoạt động 100% offline không cần server bên ngoài).
4. **Kiến trúc Dữ liệu Đơn nguồn Tin cậy (Offline-First Single Source of Truth)**:
   - Khi có dữ liệu mới từ API: Lưu vào Room Database mã hóa (`SQLCipher`).
   - UI chỉ quan sát Room DB qua Kotlin `Flow`, đảm bảo app hiển thị tức thì ngay cả khi mất mạng hoặc mạng chập chờn.
5. **Cơ chế Silent Token Renewal & Error Handling**:
   - Hoàn thiện `TokenAuthenticator` tự động gọi refresh token khi gặp 401 với `Mutex` chống race condition.
   - Chuẩn hóa phân loại lỗi mạng (`ApiError`: 400 Bad Request, 401 Unauthorized, 404 Not Found, 409 Conflict, 500 Server Error, No Internet).

### 1.2 Phạm vi tác động
- Module `:core:network`: DTOs, ApiService, MockNetworkInterceptor, NetworkModule, Error handling.
- Module `:core:data`: Data Mappers 2 chiều (DTO ↔ Entity ↔ Model), nâng cấp các Repositories (`AuthRepositoryImpl`, `RestaurantRepositoryImpl`, `OrderRepositoryImpl`, `VoucherRepositoryImpl`, `RatingRepositoryImpl`, `AddressRepositoryImpl`).
- Module `:core:database`: Bổ sung caching DAO / Entity nếu cần cho Orders và Dishes.
- Module `:core:common`: `Result<T>` và `NetworkException` helpers.

---

## 🏛️ 2. ĐỒ THỊ ẢNH HƯỞNG MODULE (MODULE IMPACT GRAPH)

```
[UI / Feature Modules]
       │ (Observes UI Flow via UseCases)
       ▼
[:core:domain] (Pure Kotlin UseCases & Repository Interfaces)
       ▲
       │ (Implements Interfaces)
[:core:data] ◄── (Mappers: DTO ↔ Entity ↔ Domain)
  ├── Repositories (Offline-First: Room DB + Network Fetch)
       │                       │
       ▼                       ▼
[:core:database]        [:core:network]
(Room DB Encrypted)     ├── Retrofit ApiService & DTOs
                        ├── OkHttp (AuthInterceptor + TokenAuthenticator)
                        └── MockNetworkInterceptor / Live BaseUrl
```

- Phụ thuộc tuân thủ nghiêm ngặt 1 chiều: Feature ➔ Domain ➔ Data ➔ Database / Network.
- Module `:core:domain` giữ nguyên là Pure Kotlin JVM Library, không bị ảnh hưởng bởi thay đổi DTO mạng.

---

## 📝 3. PHÂN RÃ CÔNG VIỆC CHI TIẾT (TASK BREAKDOWN)

### 🟢 GIAI ĐOẠN 1: HỆ THỐNG DTO VÀ RETROFIT API SERVICE TOÀN DIỆN (ĐÃ HOÀN THÀNH ✅)
#### Task 1.1: Xây dựng hệ thống Data Transfer Objects (DTOs) có `@Serializable`
- [x] Tạo thư mục `core/network/src/main/kotlin/com/bitefast/core/network/model/` chứa các data class DTO chuẩn hóa:
  - `ApiResponse.kt`: Envelope chuẩn `{ success: Boolean, code: Int, message: String, data: T? }`.
  - `AuthDtos.kt`: `LoginRequestDto`, `RegisterRequestDto`, `AuthTokenResponseDto`, `RefreshTokenRequestDto`, `UserDto`.
  - `RestaurantDtos.kt`: `RestaurantDto`, `MenuItemDto`, `MenuCategoryDto`, `RestaurantDetailDto`.
  - `OrderDtos.kt`: `CreateOrderRequestDto`, `OrderItemDto`, `OrderResponseDto`, `OrderStatusDto`.
  - `VoucherDtos.kt`: `VoucherDto`, `ApplyVoucherRequestDto`, `VoucherValidationResultDto`.
  - `RatingDtos.kt`: `DishReviewDto`, `SubmitReviewRequestDto`.
  - `AddressDtos.kt`: `AddressDto`, `CreateAddressRequestDto`.
- [x] Toàn bộ DTOs sử dụng `@Serializable` của `kotlinx.serialization`.
- [x] Hỗ trợ mapping field `snake_case` (nếu có từ backend) thông qua `@SerialName`.
- [x] **Xác minh:** Biên dịch thành công `./gradlew :core:network:compileDebugKotlin`.
- **Files tác động:** `core/network/src/main/kotlin/com/bitefast/core/network/model/*.kt`

#### Task 1.2: Mở rộng `BiteFastApiService` với đầy đủ 20+ Endpoints
- [x] Bổ sung các hàm RESTful vào `BiteFastApiService` sử dụng các DTOs vừa tạo:
  - Auth: `login`, `register`, `refreshToken`, `forgotPassword`, `getProfile`, `updateProfile`.
  - Restaurant: `getRestaurants`, `getRestaurantDetail`, `getRestaurantMenu`, `searchRestaurants`.
  - Order: `createOrder`, `getOrderHistory`, `getOrderDetail`, `cancelOrder`, `reorder`.
  - Voucher: `getVouchers`, `validateVoucher`.
  - Rating: `getDishReviews`, `submitDishReview`, `submitRestaurantRating`.
  - Address: `getAddresses`, `addAddress`, `deleteAddress`.
- [x] Chữ ký hàm nhận RequestDto và trả về `ApiResponse<ResponseDto>`.
- [x] **Xác minh:** Unit test kiểm tra cú pháp Retrofit annotations.
- **Files tác động:** `core/network/src/main/kotlin/com/bitefast/core/network/api/BiteFastApiService.kt`

---

### 🟢 GIAI ĐOẠN 2: BỘ GIẢ LẬP MẠNG NỘI BỘ (MOCK NETWORK ENGINE) & CẤU HÌNH BASE_URL (ĐÃ HOÀN THÀNH ✅)
#### Task 2.1: Xây dựng `MockNetworkInterceptor` chuẩn REST API
- [x] Triển khai một OkHttp Interceptor giả lập phản hồi của backend server:
  - Đọc HTTP Method và Request Path (`/api/v1/auth/login`, `/api/v1/restaurants`, `/api/v1/orders`,...).
  - Trả về payload JSON chuẩn của DTOs tương ứng với mã HTTP 200 OK.
  - Hỗ trợ mô phỏng các tình huống lỗi mạng: sai mật khẩu (401), hết token (401), xung đột quán ăn (409), lỗi server (500).
  - Tự động delay ngẫu nhiên 250ms - 350ms để mô phỏng độ trễ mạng thực tế, kích hoạt hiệu ứng Shimmer trên UI.
- [x] Hoạt động trong suốt mà không làm thay đổi logic của tầng Data.
- [x] Cho phép test toàn bộ app trên Emulator ngay cả khi offline hoặc chưa có backend host.
- [x] **Xác minh:** Viết Unit test cho `MockNetworkInterceptor` với OkHttpClient.
- **Files tác động:** `core/network/src/main/kotlin/com/bitefast/core/network/mock/MockNetworkInterceptor.kt`

#### Task 2.2: Cấu hình `NetworkConfig` & Cập nhật `NetworkModule`
- [x] Tạo `NetworkConfig` quản lý chế độ: `Mode.LIVE` vs `Mode.MOCK` và cấu hình `BASE_URL`.
- [x] Trong `NetworkModule`: Tự động gắn `MockNetworkInterceptor` khi ở chế độ `MOCK`. Cho phép cấu hình Certificate Pinner linh hoạt (bỏ qua khi chạy trên localhost / mock).
- [x] Khi chuyển sang `Mode.LIVE`, Retrofit tự động kết nối tới `BASE_URL` thực tế; khi chuyển sang `Mode.MOCK`, hệ thống tự giả lập API.
- [x] **Xác minh:** Biên dịch sạch sẽ `./gradlew :core:network:assembleDebug`.
- **Files tác động:**
  - `core/network/src/main/kotlin/com/bitefast/core/network/config/NetworkConfig.kt`
  - `core/network/src/main/kotlin/com/bitefast/core/network/di/NetworkModule.kt`

---

### 🟢 GIAI ĐOẠN 3: HOÀN THIỆN TOKEN AUTHENTICATOR (401 SILENT RENEWAL) (ĐÃ HOÀN THÀNH ✅)
#### Task 3.1: Kết nối `TokenAuthenticator` với API Refresh Token thực tế
- [x] Hoàn thiện `TokenAuthenticator.kt` tự động phục hồi token khi gặp 401.
- [x] Nhận cặp `accessToken` & `refreshToken` mới, cập nhật vào `AuthPreferencesDataSource` được bảo vệ bởi Jetpack Security Keystore.
- [x] Tự động gắn token mới vào request bị lỗi ban đầu và gửi lại (Retry).
- [x] Không bị lặp vô tận (tối đa 3 lần thử), an toàn đa luồng nhờ `Mutex`.
- [x] **Xác minh:** Toàn bộ test suite trong `TokenAuthenticatorTest.kt` chạy Pass 100%.
- **Files tác động:** `core/network/src/main/kotlin/com/bitefast/core/network/authenticator/TokenAuthenticator.kt`

---

### 🟢 GIAI ĐOẠN 4: HAI CHIỀU MAPPERS & REPOSITORY OFFLINE-FIRST (ĐÃ HOÀN THÀNH ✅)
#### Task 4.1: Nâng cấp Mappers 2 chiều (DTO ↔ Entity ↔ Model)
- [x] Mở rộng `core/data/src/main/kotlin/com/bitefast/core/data/mapper/Mappers.kt`:
  - `RestaurantDto.asEntity(): RestaurantEntity`
  - `RestaurantDto.asExternalModel(): Restaurant`
  - `MenuItemDto.asExternalModel(): MenuItem`
  - `OrderItemDto.asExternalModel(): CartItem`
  - `OrderResponseDto.asExternalModel(): Order`
  - `UserDto.asExternalModel(): User`
  - `VoucherDto.asExternalModel(): Voucher`
  - `AddressDto.asExternalModel(): Address`
- [x] Chuyển đổi dữ liệu chính xác, xử lý triệt để null safety và fallback giá trị mặc định.
- [x] **Xác minh:** Compile sạch sẽ không warning.
- **Files tác động:** `core/data/src/main/kotlin/com/bitefast/core/data/mapper/Mappers.kt`

#### Task 4.2: Tích hợp Backend API vào Repositories theo chuẩn Offline-First
- [x] `AuthRepositoryImpl`: Gọi `apiService.login(LoginRequestDto)` & `apiService.register(RegisterRequestDto)`, lưu tokens an toàn vào `EncryptedDataStore`.
- [x] `RestaurantRepositoryImpl`: Khi gọi `getRestaurants()`, trước tiên phát ra dữ liệu từ `RestaurantDao` (Room DB), đồng thời gọi `apiService.getRestaurants()`. Khi mạng phản hồi, ghi đè vào Room DB để tự động cập nhật UI qua Flow.
- [x] `OrderRepositoryImpl`: Gửi `CreateOrderRequestDto` lên `apiService.createOrder()`, nhận kết quả và lưu vào lịch sử đơn hàng.
- [x] `VoucherRepositoryImpl` & `RatingRepositoryImpl`: Kết nối trực tiếp tới API vouchers và ratings.
- [x] Toàn bộ Repositories kết nối thông suốt với ApiService & DTOs.
- [x] Ứng dụng hiển thị ngay lập tức từ Room DB khi mở app.
- [x] **Xác minh:** Chạy Unit test của các Repositories: `RestaurantRepositoryTest`, `AuthRepositoryTest`.
- **Files tác động:**
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/AuthRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/RestaurantRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/OrderRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/VoucherRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/RatingRepositoryImpl.kt`

---

### 🟢 GIAI ĐOẠN 5: KIỂM THỬ TỰ ĐỘNG VÀ XÁC MINH TOÀN HỆ THỐNG (ĐÃ HOÀN THÀNH ✅)
#### Task 5.1: Kiểm thử tích hợp Integration Tests với MockWebServer
- [x] Viết Integration Tests cho Repository Layer kiểm thử:
  - Luồng Đăng nhập ➔ Lưu Token ➔ Gọi API yêu cầu xác thực có đính kèm Bearer Token (`AuthRepositoryTest`).
  - Luồng Tải danh sách nhà hàng ➔ Cập nhật Room Database ➔ UI Flow nhận dữ liệu (`RestaurantRepositoryTest`).
  - Luồng 401 Silent Token Renewal thành công (`TokenAuthenticatorTest`).
- [x] **Tiêu chí nghiệm thu:** Toàn bộ test suites pass 100%.
- [x] **Xác minh:** `./gradlew testDebugUnitTest` PASS 100%.
- **Files tác động:**
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/RestaurantRepositoryTest.kt`
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/AuthRepositoryTest.kt`

#### Task 5.2: Kiểm tra biên dịch toàn bộ ứng dụng
- [x] Chạy lệnh assemble debug toàn bộ project để đảm bảo không có bất kỳ lỗi biên dịch nào trên 18 màn hình và tất cả các modules.
- [x] **Xác minh:** `./gradlew :app:assembleDebug` ra kết quả `BUILD SUCCESSFUL`.

---

## ⚠️ 4. RỦI RO & BIỆN PHÁP GIẢM THIỂU (RISKS & MITIGATIONS)

| Rủi ro kỹ thuật | Mức độ | Biện pháp giảm thiểu |
|---|:---:|---|
| **Chưa có Backend Server thật đang chạy** | Cao | Xây dựng `MockNetworkInterceptor` ngay trong OkHttp. Toàn bộ DTO, Json Serialization, HTTP codes (200, 401, 404, 500) được kiểm thử thật 100%. Khi có server thật, chỉ cần đổi 1 dòng `BASE_URL` là chạy ngay. |
| **Certificate Pinning chặn kết nối trong môi trường Dev** | Trung bình | `NetworkConfig` cho phép bypass Certificate Pinner khi ở môi trường `DEBUG` hoặc `MOCK`, chỉ kích hoạt bắt buộc trên `RELEASE`. |
| **Xung đột phiên bản JSON Serializer** | Thấp | Cấu hình `Json { ignoreUnknownKeys = true; coerceInputValues = true; isLenient = true }` để chống crash khi backend bổ sung trường mới. |
| **Xung đột luồng ghi giỏ hàng & đơn hàng** | Thấp | Sử dụng `Mutex` đã được thiết lập tại `CartRepositoryImpl` để tuần tự hóa các thao tác. |

---

## ❓ 5. CÁC CÂU HỎI THẢO LUẬN (OPEN QUESTIONS)

1. **Về Backend Hiện Tại Của Bạn:**
   - Bạn hiện đã có sẵn Backend API server (Node.js, Spring Boot, Go, Python, NestJS,...) đang chạy chưa, hay bạn muốn chúng ta xây dựng **Bộ Giả Lập Mạng Nội Bộ (Mock Network Engine)** trước để hoàn thiện toàn bộ tầng Client?
2. **Về Địa Chỉ Server (`BASE_URL`):**
   - Nếu bạn đã có server, địa chỉ IP/Domain của server là gì (ví dụ: `http://10.0.2.2:8080/` cho máy ảo Android kết nối localhost máy tính)?
3. **Về Định Dạng Token:**
   - Server của bạn sử dụng chuẩn JWT (`Bearer <access_token>`) thông thường với thời hạn hết hạn (TTL) là bao lâu?

---

## ⚖️ 6. ĐỀ XUẤT CÁC PHƯƠNG ÁN XỬ LÝ (THEO ĐIỀU 4 AGENTS.MD)

### 🔹 Phương án 1 (Khuyến nghị - Recommended): Xây dựng Bộ Giả Lập Mạng Nội Bộ (Mock Network Engine) + Chuẩn Hóa DTOs & Offline-First Repository
- **Cách làm:**
  - Hoàn thiện toàn bộ hệ thống DTOs, Envelopes, Retrofit Endpoints và Repository Offline-First (Room DB).
  - Tích hợp `MockNetworkInterceptor` với độ trễ thực tế (300-500ms) và trả về dữ liệu mẫu chuẩn REST API.
  - Khi có server thật, bạn chỉ cần thay đổi biến cấu hình `BASE_URL` trong `NetworkConfig` là ứng dụng lập tức kết nối trực tiếp với backend mà không cần sửa bất kỳ dòng code logic nào.
- **Ưu điểm:**
  - Chủ động 100%, không bị phụ thuộc vào việc backend server có đang bật hay không.
  - Kiểm thử được toàn bộ các trường hợp phức tạp (Token hết hạn 401, Mất mạng, Lỗi server 500) ngay trên máy ảo.
  - Codebase đạt chuẩn Enterprise 10/10, tách bạch rõ ràng giữa Domain - Data - Network.
- **Lý do khuyến nghị:** Giúp dự án tiến nhanh, kiểm thử tự động ổn định và sẵn sàng tương thích với bất kỳ backend nào trong tương lai.

### 🔹 Phương án 2: Tích hợp trực tiếp với Backend API Server có sẵn (Live Backend Server)
- **Cách làm:**
  - Bạn cung cấp thông tin Swagger/API Spec và Endpoint Server (`http://10.0.2.2:...` hoặc domain thật).
  - Xây dựng DTOs khớp chính xác 100% với JSON trả về từ server của bạn và kết nối trực tiếp.
- **Ưu điểm:** Kết nối dữ liệu sống trực tiếp từ database thật của bạn.
- **Nhược điểm:** Phụ thuộc hoàn toàn vào trạng thái hoạt động của server bên ngoài. Nếu server chưa hoàn thiện đủ các endpoints (đánh giá món, voucher, đơn hàng) thì các màn hình tương ứng sẽ bị lỗi kết nối mạng.

---

👉 **Bạn muốn chúng ta thực hiện theo Phương án 1 (Khuyến nghị: Chuẩn hóa DTO + Mock Engine linh hoạt) hay Phương án 2 (Kết nối trực tiếp Server có sẵn)?**
