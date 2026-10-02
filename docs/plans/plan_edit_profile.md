# 🛠️ KẾ HOẠCH TRIỂN KHAI: TÍNH NĂNG CHỈNH SỬA HỒ SƠ CÁ NHÂN (EDIT PROFILE)

> **Tài liệu tham chiếu:** Yêu cầu người dùng ngày 02/10/2026  
> **Trạng thái:** ✅ **ĐÃ HOÀN THÀNH** — 02/10/2026  
> **Kết quả:** Build SUCCESSFUL + 7/7 Unit Tests PASSED + APK đã install lên Pixel_7 AVD  
> **Mục tiêu:** 
> Xây dựng hoàn chỉnh tính năng "Chỉnh sửa hồ sơ" cho người dùng ứng dụng BiteFast: Cho phép cập nhật Họ và tên, Số điện thoại liên hệ, lựa chọn Avatar đại diện; đồng bộ dữ liệu hai chiều giữa Bộ nhớ bảo mật (Encrypted DataStore) và Backend API, đồng thời tự động cập nhật ngay trên giao diện màn hình Tài khoản (`ProfileScreen`).

---

## 🔍 1. MỤC TIÊU & PHẠM VI (OBJECTIVE & SCOPE)

### 1.1 Mục tiêu
- Thay thế thông báo tạm thời `"Tính năng chỉnh sửa hồ sơ đang được cập nhật"` trên `ProfileScreen` bằng luồng điều hướng thực tế đến màn hình chỉnh sửa hồ sơ chuyên nghiệp.
- Cung cấp form chỉnh sửa thông tin cá nhân trực quan, chuẩn UI Material 3 với:
  - Bộ chọn ảnh đại diện (Preset Avatars & Custom Avatar).
  - Trường chỉnh sửa Họ và tên (Validate độ dài tối thiểu).
  - Trường chỉnh sửa Số điện thoại (Validate định dạng 10 số chuẩn Việt Nam: 03x, 05x, 07x, 08x, 09x).
  - Trường Email đăng nhập hiển thị minh bạch (Read-only kèm giải thích bảo mật).
- Lưu trữ bền vững thông tin người dùng trong `DataStore` và gọi API `PUT /api/v1/user/profile` lên hệ thống.
- Khi lưu thành công, tự động quay về `ProfileScreen` với tên và avatar mới được cập nhật tức thì (Reactive StateFlow).

### 1.2 Phạm vi tác động
- Module `:core:datastore`: Bổ sung lưu trữ thông tin User Profile (`user_name`, `user_phone`, `user_avatar`) vào `AuthPreferencesDataSource`.
- Module `:core:network`: Bổ sung mock response cho API `PUT /api/v1/user/profile` trong `MockNetworkInterceptor`.
- Module `:core:domain`: Thêm hàm `updateProfile` vào `AuthRepository`, xây dựng `UpdateProfileUseCase` và `GetUserProfileUseCase` tại package `com.bitefast.core.domain.user` (Zero Android SDK Dependencies).
- Module `:core:data`: Cài đặt `updateProfile` trong `AuthRepositoryImpl`, đồng bộ dữ liệu vào `DataStore` và `BiteFastApiService`.
- Module `:feature:profile`: Xây dựng màn hình `EditProfileScreen.kt`, ViewModel `EditProfileViewModel.kt`, kết nối sự kiện từ `ProfileScreen.kt`.
- Module `:app`: Đăng ký `EditProfileDestination` trong `BiteFastDestinations.kt` và cấu hình Navigation trong `BiteFastNavHost.kt`.

---

## 🏛️ 2. ĐỒ THỊ ẢNH HƯỞNG MODULE (MODULE IMPACT GRAPH)

```
[:core:model]
    └── User, UserDto, UpdateProfileRequestDto (đã có sẵn trong Model/Network)
           │
           ▼
[:core:datastore]
    └── AuthPreferencesDataSource.kt: Bổ sung lưu trữ user_name, user_phone, user_avatar
           │
           ▼
[:core:network]
    └── MockNetworkInterceptor.kt: Mock xử lý endpoint PUT /api/v1/user/profile
           │
           ▼
[:core:domain] (Pure Kotlin JVM - Zero Android SDK)
    ├── AuthRepository.kt: Thêm suspend fun updateProfile(name, phone, avatar): User
    └── ProfileUseCases.kt (UpdateProfileUseCase, GetUserProfileUseCase)
           │
           ▼
[:core:data]
    └── AuthRepositoryImpl.kt: Cài đặt gọi API PUT và lưu DataStore
           │
           ▼
[:feature:profile]
    ├── edit/EditProfileContract.kt: State, Event, Effect
    ├── edit/EditProfileViewModel.kt: Logic xác thực form và gọi UseCase
    ├── edit/EditProfileScreen.kt: Giao diện form chỉnh sửa & chọn avatar
    └── ProfileScreen.kt / ProfileViewModel.kt: Bắn Effect điều hướng sang EditProfile
           │
           ▼
[:app]
    ├── navigation/BiteFastDestinations.kt: Thêm EditProfileDestination
    └── navigation/BiteFastNavHost.kt: Cấu hình Route composable<EditProfileDestination>
```

---

## 📋 3. PHÂN RÃ CÔNG VIỆC (TASK BREAKDOWN)

### 🔹 TASK 1: Nâng Cấp Tầng Cơ Sở Domain & Data Cho Hồ Sơ Cá Nhân [Kích thước: M]
- **Mô tả:**
  - Trong `:core:datastore/AuthPreferencesDataSource.kt`:
    - Bổ sung các `PreferencesKeys`: `USER_NAME`, `USER_PHONE`, `USER_AVATAR`.
    - Thêm các hàm `saveUserProfile(name: String, phone: String, avatar: String?)` và Flow đọc dữ liệu.
  - Trong `:core:network/MockNetworkInterceptor.kt`:
    - Bổ sung xử lý `method == "PUT" && path.endsWith("/user/profile")`, trả về `UserDto` với thông tin cập nhật mới nhất.
  - Trong `:core:domain`:
    - Cập nhật [AuthRepository.kt](file:///d:/Personal_Project/Mobile_Project/core/domain/src/main/kotlin/com/bitefast/core/domain/repository/AuthRepository.kt): Thêm `suspend fun updateProfile(name: String, phone: String, avatar: String?): User`.
    - Tạo file [ProfileUseCases.kt](file:///d:/Personal_Project/Mobile_Project/core/domain/src/main/kotlin/com/bitefast/core/domain/user/ProfileUseCases.kt) chứa:
      - `UpdateProfileUseCase`: Kiểm tra tính hợp lệ dữ liệu (Tên không được rỗng >= 2 ký tự, SĐT đúng 10 chữ số chuẩn VN).
      - `GetUserProfileUseCase`: Lấy thông tin user hiện tại.
  - Trong `:core:data/AuthRepositoryImpl.kt`:
    - Cài đặt `updateProfile`: Gửi yêu cầu qua `BiteFastApiService.updateProfile()`, lưu vào `AuthPreferencesDataSource`, trả về `User` đã cập nhật.
- **Tiêu chí nghiệm thu:**
  - `:core:domain` thuần Kotlin JVM không chứa Android context.
  - Dữ liệu chỉnh sửa được lưu vào DataStore và đồng bộ qua API.
- **Bước xác minh (Verification):**
  - Chạy Unit Test `AuthUseCasesTest` và viết mới `ProfileUseCasesTest`.
- **Files tác động:**
  - `core/datastore/src/main/kotlin/com/bitefast/core/datastore/AuthPreferencesDataSource.kt`
  - `core/network/src/main/kotlin/com/bitefast/core/network/mock/MockNetworkInterceptor.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/repository/AuthRepository.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/user/ProfileUseCases.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/AuthRepositoryImpl.kt`

---

### 🔹 TASK 2: Xây Dựng ViewModel & Giao Diện `EditProfileScreen` [Kích thước: M]
- **Mô tả:**
  - Tạo package `com.bitefast.feature.profile.edit`:
    - `EditProfileContract.kt`:
      - `EditProfileUiState`: `name`, `phone`, `email`, `avatar`, `selectedAvatar`, `nameError`, `phoneError`, `isLoading`, `isSuccess`.
      - `EditProfileUiEvent`: `NameChanged`, `PhoneChanged`, `AvatarSelected`, `Submit`, `BackClicked`.
      - `EditProfileUiEffect`: `NavigateBack`, `ShowSnackbar`.
    - `EditProfileViewModel.kt`:
      - Inject `GetUserProfileUseCase` (hoặc `AuthRepository`) và `UpdateProfileUseCase`.
      - Tải thông tin ban đầu của người dùng khi khởi tạo.
      - Validate dữ liệu realtime khi người dùng nhập.
      - Xử lý `Submit`: Gọi `UpdateProfileUseCase`, phát thông báo thành công và kích hoạt `NavigateBack`.
    - `EditProfileScreen.kt`:
      - Header: `TopAppBar` với nút quay lại, tiêu đề `"Chỉnh sửa hồ sơ"`.
      - Khu vực Avatar: Ảnh đại diện tròn to với icon máy ảnh/chỉnh sửa, kèm thanh danh sách Preset Avatars (các avatar vui nhộn phong cách Foodie: Pizza Lover, Burger King, Coffee Addict, Chef,...).
      - Form thông tin:
        - `OutlinedTextField` Họ và tên (Icon Person, hiển thị lỗi nếu < 2 ký tự).
        - `OutlinedTextField` Số điện thoại (Icon Phone, bàn phím số, kiểm tra định dạng SĐT Việt Nam).
        - `OutlinedTextField` Email (Icon Email, chế độ Read-only với nhãn thông tin bảo mật "Email liên kết tài khoản").
      - Nút hành động: `BiteFastButton` `"Lưu thay đổi"` to rõ, hiển thị loading khi đang lưu.
- **Tiêu chí nghiệm thu:**
  - Giao diện chuẩn Material 3, hỗ trợ Dark/Light Theme.
  - Phản hồi lỗi nhập liệu rõ ràng bằng tiếng Việt có dấu.
- **Bước xác minh (Verification):**
  - Viết `EditProfileViewModelTest` kiểm thử đầy đủ các kịch bản: tải profile thành công, validate lỗi tên/sđt, lưu thành công.
- **Files tác động:**
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/edit/EditProfileContract.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/edit/EditProfileViewModel.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/edit/EditProfileScreen.kt`
  - `feature/profile/src/test/kotlin/com/bitefast/feature/profile/edit/EditProfileViewModelTest.kt`

---

### 🔹 TASK 3: Tích Hợp Điều Hướng & Cập Nhật Reactive Trên `ProfileScreen` [Kích thước: S]
- **Mô tả:**
  - Trong `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileViewModel.kt`:
    - Thay thế `ShowSnackbar` ở `ClickEditProfile` bằng `sendEffect(ProfileUiEffect.NavigateToEditProfile)`.
    - Khi quay lại màn hình, tự động lắng nghe hoặc tải lại thông tin `User` mới để giao diện cập nhật ngay lập tức.
  - Trong `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileScreen.kt`:
    - Thêm callback `onNavigateToEditProfile: () -> Unit` trong `ProfileRoute`.
  - Trong `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastDestinations.kt`:
    - Khai báo `@Serializable object EditProfileDestination`.
  - Trong `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`:
    - Khai báo `composable<EditProfileDestination> { EditProfileRoute(onNavigateBack = { navController.popBackStack() }) }`.
    - Kết nối `onNavigateToEditProfile = { navController.navigate(EditProfileDestination) }` từ `ProfileRoute`.
- **Tiêu chí nghiệm thu:**
  - Nhấp vào "Chỉnh sửa hồ sơ" ở màn hình Tài khoản chuyển sang màn hình EditProfile mượt mà.
  - Bấm "Lưu thay đổi" xong tự động quay lại `ProfileScreen`, tên và ảnh đại diện hiển thị đúng thông tin mới.
- **Files tác động:**
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileViewModel.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileScreen.kt`
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastDestinations.kt`
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`

---

### 🔹 TASK 4: Kiểm Thử Toàn Diện & Nghiệm Thu Trên Máy Ảo [Kích thước: S]
- **Mô tả:**
  - Chạy toàn bộ Unit Tests kiểm thử hồi quy: `./gradlew testDebugUnitTest`.
  - Build và cài đặt APK lên máy ảo Pixel 7: `./gradlew :app:installDebug`.
  - Kiểm thử trực quan:
    1. Mở màn hình Tài khoản -> Bấm "Chỉnh sửa hồ sơ".
    2. Thử nhập tên trống hoặc SĐT không đúng định dạng -> Kiểm tra thông báo lỗi.
    3. Chọn Avatar mới, nhập tên `"Nguyễn Hữu Tùng"`, SĐT `"0987654321"` -> Bấm "Lưu thay đổi".
    4. Kiểm tra màn hình Profile đã cập nhật tên `"Nguyễn Hữu Tùng"` và avatar mới.
    5. Đóng app và mở lại -> Kiểm tra thông tin vẫn được lưu trữ vĩnh viễn (DataStore Persistence).
  - Chụp ảnh màn hình nghiệm thu visual.
- **Tiêu chí nghiệm thu:** Build thành công 100%, tất cả test cases pass.

---

## ⚠️ 4. RỦI RO & BIỆN PHÁP GIẢM THIỂU (RISKS & MITIGATIONS)

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| **Dữ liệu trên ProfileScreen không cập nhật sau khi sửa** | Trung bình | `ProfileViewModel` sẽ reload lại user khi màn hình được resumed hoặc `AuthRepository` phát StateFlow/Flow lắng nghe thay đổi DataStore để kích hoạt recomposition tự động. |
| **Xung đột định dạng số điện thoại Việt Nam** | Thấp | Sử dụng Regex kiểm tra nghiêm ngặt `^(0[3|5|7|8|9])+([0-9]{8})$` cho SĐT 10 số. |
| **Tuân thủ Clean Architecture** | Nghiêm ngặt | Không truyền bất kỳ đối tượng Android View/Context nào vào `:core:domain`. Toàn bộ xử lý logic nằm tại UseCase. |

---

## ❓ 5. THAM VẤN & ĐỀ XUẤT PHƯƠNG ÁN (OPEN QUESTIONS)

### ⚖️ ĐỀ XUẤT CÁC PHƯƠNG ÁN XỬ LÝ:

#### 🔹 Phương án 1 (Khuyến nghị - Recommended): Màn Hình Chuyên Biệt `EditProfileScreen` Kèm Bộ Chọn Avatar & Đồng Bộ Đa Tầng
- **Mô tả:**
  1. Tạo màn hình độc lập `EditProfileScreen` (Type-safe Navigation `EditProfileDestination`).
  2. Hỗ trợ đầy đủ các trường: Chọn ảnh đại diện (bộ sưu tập Preset Avatars sinh động), Họ & tên, Số điện thoại (kiểm tra định dạng), Email (read-only).
  3. Lưu bền vững vào `Encrypted DataStore` và gọi API Backend `PUT /api/v1/user/profile`.
  4. Trải nghiệm người dùng cao cấp chuẩn Enterprise, có hiệu ứng chuyển cảnh, kiểm tra hợp lệ tức thì và đồng bộ tự động ra màn hình Tài khoản.
- **Ưu điểm:** Trải nghiệm người dùng trọn vẹn, tính năng hoàn thiện 100% theo tiêu chuẩn ứng dụng giao đồ ăn hàng đầu (Grab, ShopeeFood), hỗ trợ chọn avatar đại diện.
- **Nhược điểm:** Cần tạo mới màn hình `EditProfileScreen` và ViewModel đi kèm (3-4 files).
- **Lý do khuyến nghị:** Giữ vững sự nhất quán với các màn hình khác trong `:feature:profile` (như `AddressListScreen`, `FavoritesScreen`), mang lại giao diện chỉn chu, chuyên nghiệp.

#### 🔹 Phương án 2: Chỉnh Sửa Nhanh Dưới Dạng `BottomSheet` Trên `ProfileScreen`
- **Mô tả:**
  1. Không tạo màn hình mới hay route navigation mới.
  2. Bấm vào "Chỉnh sửa hồ sơ" sẽ mở một `ModalBottomSheet` ngay trên `ProfileScreen` cho phép sửa nhanh Họ tên và Số điện thoại.
  3. Không có bộ chọn Avatar riêng, chỉ sửa text.
- **Ưu điểm:** Triển khai nhanh hơn, ít file mới.
- **Nhược điểm:** Không gian BottomSheet hẹp hơn, khó bố trí bộ chọn avatar trực quan, trải nghiệm kém chuyên nghiệp hơn so với màn hình chuyên biệt.

👉 **Bạn muốn chúng ta thực hiện theo Phương án 1 (Khuyến nghị) hay Phương án 2?**
