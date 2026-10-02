# 🛠️ KẾ HOẠCH TRIỂN KHAI: BỘ LỌC ĐÁNH GIÁ TƯƠNG TÁC & HỆ THỐNG YÊU THÍCH MÓN ĂN (FAVORITES)

> **Tài liệu tham chiếu:** Phản hồi người dùng ngày 02/10/2026  
> **Trạng thái:** ✅ Đã hoàn thành 100% (Completed - Phương án 1)  
> **Mục tiêu:** 
> 1. Kích hoạt bộ lọc tương tác trong Tab Đánh giá trên màn hình Chi tiết quán (`DetailScreen`): Cho phép bấm chọn các thẻ cảm nhận (`Tất cả`, `Món ăn ngon`, `Giao nhanh`, `Đóng gói kỹ`) và mức sao (5★..1★) để lọc nhận xét khách hàng tức thì.
> 2. Xây dựng hoàn chỉnh tính năng Yêu thích món ăn & nhà hàng: Người dùng bấm thả tim món ăn/quán ăn sẽ được lưu trữ bền vững (Local Persistence), xem lại toàn bộ trong mục "Nhà hàng & Món ăn yêu thích" tại màn hình Tài khoản (`FavoritesScreen`).

---

## 🔍 1. PHÂN TÍCH HIỆN TRẠNG & NGUYÊN NHÂN GỐC RỄ

### 📌 Vấn đề 1: Phân loại ở phần Đánh giá chưa sử dụng được
- **Hiện trạng:**
  - Trong Tab `[Đánh giá (4.6 ★)]` của `DetailScreen`, các thẻ cảm nhận nhanh (`Món ăn ngon (340)`, `Giao nhanh (280)`, `Đóng gói kỹ (195)`) và các thanh sao (5★..1★) đang được dựng bằng `Surface` tĩnh, chưa có `onClick` và chưa gắn State trong `DetailViewModel`.
  - Danh sách nhận xét bên dưới (`uiState.recentDishReviews`) luôn hiển thị cố định mà không thay đổi theo thao tác chạm của người dùng.
- **Giải pháp:**
  - Bổ sung `ReviewFilterOption` và State `selectedReviewFilter` trong `DetailViewModel`.
  - Chuyển đổi các thẻ cảm nhận thành `FilterChip` tương tác (có trạng thái chọn màu cam nổi bật).
  - Tự động lọc danh sách nhận xét thực tế phù hợp theo thẻ hoặc số sao được chọn.

### 📌 Vấn đề 2: Tính năng Yêu thích món ăn chưa lưu trữ và chưa hiển thị
- **Hiện trạng:**
  - Tại màn hình Chi tiết (`DetailScreen`), nút trái tim ở Header chỉ đổi biến `isFavorite: Boolean` tạm thời trong ViewModel, chưa lưu vào Database.
  - Trên thẻ món ăn (`MenuItemRow`) chưa có nút thả tim riêng cho từng món.
  - Tại màn hình Tài khoản (`ProfileScreen`), khi bấm vào "Nhà hàng yêu thích", hệ thống chỉ bắn `ShowSnackbar("Chức năng Yêu thích đang phát triển")`.
  - Destination `WishlistDestination` đã có trong [BiteFastDestinations.kt](file:///d:/Personal_Project/Mobile_Project/app/src/main/kotlin/com/bitefast/app/navigation/BiteFastDestinations.kt) nhưng chưa được cấu hình màn hình hiển thị trong `BiteFastNavHost.kt`.
- **Giải pháp:**
  - Xây dựng bảng lưu trữ `favorite_items` (Room DB / DataStore) hỗ trợ lưu cả món ăn (`DISH`) và quán ăn (`RESTAURANT`).
  - Định nghĩa Repository và UseCases trong `:core:domain` (`GetFavoritesUseCase`, `ToggleFavoriteUseCase`).
  - Thêm nút thả tim trực tiếp trên món ăn (`MenuItemRow`) và đồng bộ trạng thái trái tim.
  - Xây dựng màn hình `FavoritesScreen` hoàn chỉnh (gồm 2 Tab: `[Món ăn yêu thích]` & `[Nhà hàng yêu thích]`) cho phép xem, đặt món nhanh (+) hoặc bỏ thích.

---

## 🏛️ 2. ĐỒ THỊ ẢNH HƯỞNG MODULE (MODULE IMPACT GRAPH)

```
[:core:model]
    └── Bổ sung FavoriteItem (id, type: DISH/RESTAURANT, targetId, name, price, imageUrl, rating, subtitle)
           │
           ▼
[:core:database]
    ├── FavoriteEntity.kt & FavoriteDao.kt: Quản lý bảng favorite_items (Room)
    └── BiteFastDatabase.kt: Đăng ký FavoriteDao
           │
           ▼
[:core:domain] (Pure Kotlin JVM)
    ├── FavoriteRepository.kt: Interface quản lý lưu/xóa/đọc danh sách yêu thích
    ├── GetFavoritesUseCase.kt: Lấy Flow danh sách món ăn & quán yêu thích
    └── ToggleFavoriteUseCase.kt: Thêm/Xóa khỏi danh sách yêu thích
           │
           ▼
[:core:data]
    └── FavoriteRepositoryImpl.kt: Cài đặt FavoriteRepository giao tiếp Room DAO
           │
           ▼
[:feature:detail]
    ├── DetailViewModel.kt: Tích hợp ToggleFavorite cho món & quán, State lọc Review
    └── DetailScreen.kt: FilterChips tương tác cho Reviews, nút Tim trên món ăn
           │
           ▼
[:feature:profile] & [:app]
    ├── FavoritesScreen.kt: Màn hình danh sách yêu thích (Tab Món ăn & Tab Nhà hàng)
    ├── ProfileScreen.kt / ProfileViewModel.kt: Điều hướng đến FavoritesScreen
    └── BiteFastNavHost.kt: Định tuyến WishlistDestination -> FavoritesScreen
```

---

## 📋 3. PHÂN RÃ CÔNG VIỆC (TASK BREAKDOWN)

### 🔹 TASK 1: Kích Hoạt Bộ Lọc Đánh Giá Tương Tác Trên `DetailScreen` [Kích thước: S]
- **Mô tả:**
  - Trong `DetailViewModel.kt`:
    - Bổ sung `selectedReviewTag: String = "Tất cả"`.
    - Thêm sự kiện `DetailUiEvent.SelectReviewTag(val tag: String)`.
    - Tính toán `filteredReviews: List<DishReview>` dựa trên tag được chọn (`Tất cả`, `Món ăn ngon`, `Giao nhanh`, `Đóng gói kỹ`, `5 sao`,...).
  - Trong `DetailScreen.kt`:
    - Biến đổi các thẻ cảm nhận nhanh và thanh sao thành các `FilterChip` tương tác mượt mà.
    - Hiển thị badge số lượng phản hồi tương ứng.
- **Tiêu chí nghiệm thu:**
  - Bấm vào bất kỳ tag đánh giá nào, danh sách nhận xét bên dưới lập tức lọc đúng nội dung.
- **File tác động:**
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailViewModel.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailScreen.kt`
  - `feature/detail/src/test/kotlin/com/bitefast/feature/detail/DetailViewModelTest.kt`

---

### 🔹 TASK 2: Kiến Trúc Tầng Cơ Sở Cho Tính Năng Yêu Thích (Domain & Data) [Kích thước: M]
- **Mô tả:**
  - `:core:model`: Tạo `FavoriteItem` (`id`, `type`: `DISH` hoặc `RESTAURANT`, `targetId`, `name`, `price`, `imageUrl`, `rating`, `restaurantName`).
  - `:core:database`: Tạo `FavoriteEntity` và `FavoriteDao` (hỗ trợ `getFavoritesByType`, `isFavorite`, `insertFavorite`, `deleteFavoriteById`). Đăng ký vào `BiteFastDatabase`.
  - `:core:domain`:
    - Tạo `FavoriteRepository`:
      `fun getFavoriteDishes(): Flow<List<FavoriteItem>>`
      `fun getFavoriteRestaurants(): Flow<List<FavoriteItem>>`
      `suspend fun toggleFavoriteDish(item: MenuItem, restaurantName: String): Boolean`
      `suspend fun toggleFavoriteRestaurant(restaurant: Restaurant): Boolean`
      `fun isFavorite(targetId: String): Flow<Boolean>`
    - Viết `GetFavoritesUseCase` và `ToggleFavoriteUseCase`.
  - `:core:data`: Cài đặt `FavoriteRepositoryImpl`.
- **Tiêu chí nghiệm thu:**
  - Module Domain thuần JVM, dữ liệu yêu thích được lưu bền vững vào Room DB.
  - Unit tests cho DAO và Repository pass 100%.
- **File tác động:**
  - `core/model/src/main/kotlin/com/bitefast/core/model/Model.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/entity/FavoriteEntity.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/dao/FavoriteDao.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/BiteFastDatabase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/repository/FavoriteRepository.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/favorite/GetFavoritesUseCase.kt`
  - `core/domain/src/main/kotlin/com/bitefast/core/domain/favorite/ToggleFavoriteUseCase.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/FavoriteRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/di/DataModule.kt`

---

### 🔹 TASK 3: Tích Hợp Nút Yêu Thích Món Ăn & Quán Ăn Trên `DetailScreen` [Kích thước: S]
- **Mô tả:**
  - Trên `MenuItemRow` của `DetailScreen`: Bổ sung nút trái tim nhỏ gọn bên cạnh nút `+` (thêm nhanh), hoặc trong `CustomizationBottomSheet`.
  - Khi bấm tim: Gọi `ToggleFavoriteUseCase`, đổi màu tim đỏ, hiển thị Snackbar nhẹ thông báo: `"Đã lưu vào danh sách yêu thích"`.
  - Nút tim trên AppBar của quán: Đồng bộ lưu/xóa quán vào danh sách yêu thích bền vững.
- **Tiêu chí nghiệm thu:**
  - Thả tim/bỏ tim cập nhật tức thì trạng thái giao diện và lưu vào DB.
- **File tác động:**
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailViewModel.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailScreen.kt`

---

### 🔹 TASK 4: Xây Dựng Màn Hình `FavoritesScreen` & Điều Hướng Từ Profile [Kích thước: M]
- **Mô tả:**
  - Trong `feature/profile`:
    - Tạo `FavoritesScreen.kt` & `FavoritesViewModel.kt` (hoặc mở rộng `ProfileViewModel`).
    - Giao diện gồm 2 Tab Material 3: **`[Món ăn đã lưu]`** và **`[Quán ăn yêu thích]`**.
    - Mỗi thẻ món ăn hiển thị: Ảnh đại diện, tên món, quán ăn, giá tiền (`55.000 đ`), nút xóa yêu thích (trái tim đỏ) và nút đặt nhanh (`+`).
    - Có trạng thái `EmptyStateView` khi chưa lưu món/quán nào.
  - Cập nhật [ProfileScreen.kt](file:///d:/Personal_Project/Mobile_Project/feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileScreen.kt):
    - Đổi nhãn menu thành `"Món ăn & Quán yêu thích"`.
    - Bấm vào sẽ kích hoạt điều hướng `NavigateToFavorites`.
  - Trong [BiteFastNavHost.kt](file:///d:/Personal_Project/Mobile_Project/app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt):
    - Khai báo route `composable<WishlistDestination>` trỏ đến `FavoritesScreen`.
- **Tiêu chí nghiệm thu:**
  - Bấm từ Profile chuyển sang FavoritesScreen mượt mà, hiển thị chính xác các món đã thả tim.
- **File tác động:**
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/favorites/FavoritesScreen.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/favorites/FavoritesViewModel.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileScreen.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileViewModel.kt`
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`

---

### 🔹 TASK 5: Kiểm Thử Toàn Diện & Xác Minh Trực Tiếp Trên Máy Ảo [Kích thước: S]
- **Mô tả:**
  - Chạy toàn bộ Unit Tests: `./gradlew testDebugUnitTest`.
  - Biên dịch và cài đặt APK lên máy ảo: `./gradlew :app:installDebug`.
  - Kiểm thử trực tiếp luồng:
    1. Bấm lọc tag đánh giá `Món ăn ngon` trong Tab Đánh giá.
    2. Thả tim món "Trà Đào Cam Sả Đặc Biệt" và "Trà Sữa Phúc Long".
    3. Vào mục "Tài khoản" -> "Món ăn & Quán yêu thích" -> kiểm tra danh sách hiển thị chuẩn xác.
- **Tiêu chí nghiệm thu:** Pass 100% tests, chụp ảnh màn hình nghiệm thu trực quan.

---

## ⚠️ 4. RỦI RO & BIỆN PHÁP GIẢM THIỂU (RISKS & MITIGATIONS)

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| **Room Database Migration** | Thấp | Bảng `favorite_items` là bảng mới độc lập, sử dụng `fallbackToDestructiveMigration()` trong môi trường debug để không gây crash app. |
| **Phụ thuộc chéo Feature module** | Tuyệt đối tránh | `feature:profile` và `feature:detail` không import lẫn nhau. Toàn bộ logic chia sẻ thông qua `:core:domain` (`FavoriteRepository`) và `:core:model`. |

---

## ❓ 5. THAM VẤN & ĐỀ XUẤT PHƯƠNG ÁN (OPEN QUESTIONS)

### ⚖️ ĐỀ XUẤT CÁC PHƯƠNG ÁN XỬ LÝ:

#### 🔹 Phương án 1 (Khuyến nghị - Recommended): Xây Dựng Hệ Thống Yêu Thích Bền Vững & Màn Hình Favorites Chuyên Nghiệp
- **Mô tả:**
  1. Kích hoạt bộ lọc Tab Đánh giá với đầy đủ các tag (`Tất cả`, `Món ăn ngon`, `Giao nhanh`, `Đóng gói kỹ`) lọc real-time.
  2. Lưu trữ danh sách yêu thích vào Room Database (bền vững ngay cả khi tắt app).
  3. Thêm nút tim trên từng món ăn (`MenuItemRow`) và nhà hàng.
  4. Xây dựng màn hình `FavoritesScreen` chuyên biệt với 2 tab `[Món ăn]` & `[Nhà hàng]` có nút xóa/đặt món nhanh.
- **Ưu điểm:** Chuẩn Enterprise 10/10, dữ liệu lưu trữ vĩnh viễn, trải nghiệm người dùng trọn vẹn như GrabFood/ShopeeFood.
- **Nhược điểm:** Tác động qua các tầng Core Domain, Database, Feature Detail và Feature Profile (5 tasks).
- **Lý do khuyến nghị:** BiteFast đã có sẵn `WishlistDestination` trong Navigation; hoàn thiện tính năng này sẽ lấp đầy 100% các tính năng người dùng trong ứng dụng.

#### 🔹 Phương án 2: Lưu Trữ Bộ Nhớ Tạm (In-Memory) & Hiển Thị Đơn Giản
- **Mô tả:**
  1. Kích hoạt bộ lọc Tab Đánh giá.
  2. Chỉ lưu danh sách yêu thích trong Bộ nhớ RAM (StateFlow của Repository trong phiên chạy), không tạo bảng Room DB mới.
  3. Hiển thị danh sách yêu thích dưới dạng BottomSheet đơn giản trong Profile.
- **Ưu điểm:** Triển khai nhanh hơn.
- **Nhược điểm:** Tắt app hoặc khởi động lại sẽ mất toàn bộ món ăn đã yêu thích.

👉 **Bạn muốn chúng ta thực hiện theo Phương án 1 (Khuyến nghị) hay Phương án 2?**
