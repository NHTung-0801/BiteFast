# 🧭 Kế Hoạch Sửa Lỗi Toàn Diện Navigation & Lối Tắt (Shortcuts)

> **Mã kế hoạch:** `plan_navigation_shortcut_fixes.md`  
> **Ngày lập:** 01/10/2026 | **Phiên bản:** 1.0  
> **Tuân thủ quy chuẩn:** `AGENTS.md` (Clean Architecture, Inversion of Control, Plan-First)

---

## 🎯 1. MỤC TIÊU & PHẠM VI (OBJECTIVE & SCOPE)

### 1.1 Mục tiêu
Khắc phục triệt để các sự cố điều hướng (Navigation Routing) và xung đột ngăn xếp (BackStack Collision) giữa các Tab chính (BottomBar) và các lối tắt (Shortcuts) trong toàn bộ ứng dụng BiteFast, trọng tâm là:
1. **Lối tắt Profile ➔ Lịch sử đơn hàng ➔ Quay lại**: Khắc phục lỗi kẹt không vào lại được Profile và lỗi icon quay lại (`<-`) tự ý đẩy người dùng về Trang chủ.
2. **Lối tắt Quán ăn ➔ Giỏ hàng ➔ Quay lại**: Khắc phục lỗi bấm quay lại từ Giỏ hàng bị văng về Trang chủ thay vì quay lại Quán ăn vừa xem.
3. **Lối tắt Đăng nhập của Khách vãng lai tại Profile**: Xóa bỏ dialog "Bạn có chắc muốn đăng xuất?" phi lý, chuyển thẳng tới màn hình Đăng nhập.
4. **Lỗi Dead-click "Chỉnh sửa hồ sơ"**: Phản hồi thông báo người dùng minh bạch thay vì đứng im không phản hồi.
5. **Kết nối thiếu sót Checkout ➔ PaymentResult (VietQR)**: Nối callback mở màn hình thanh toán VietQR động.
6. **Đồng bộ hóa 4 Tab BottomBar**: Cơ chế chuyển Tab thông minh, không làm vỡ ngăn xếp (BackStack) khi chuyển đổi qua lại giữa các màn hình.

### 1.2 Phạm vi tác động
- **Module Presentation & App**:
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileScreen.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileViewModel.kt`
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/OrderScreen.kt`
  - `feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartScreen.kt`
- **Tầng Domain & Data**: Không bị ảnh hưởng (giữ nguyên tính độc lập).

---

## 🗺️ 2. ĐỒ THỊ ẢNH HƯỞNG MODULE (MODULE IMPACT GRAPH)

```
       [app: navigation] (BiteFastNavHost.kt)
          │               │             │
          ▼               ▼             ▼
   [:feature:profile] [:feature:order] [:feature:cart]
          │
          ▼
   [:core:common] (UiEvent / UiEffect / BaseViewModel)
```
- Phụ thuộc tuân thủ Clean Architecture 1 chiều: `app` phụ thuộc các feature, các feature hoàn toàn độc lập với nhau.

---

## 🛠️ 3. PHÂN TÍCH NGUYÊN NHÂN GỐC RỄ (ROOT CAUSE ANALYSIS)

### Vấn đề 1: Bấm "Lịch sử đơn hàng" ở Profile ➔ Sang Đơn hàng ➔ Bấm lại Tab Profile không vào được
- **Nguyên nhân:** Trong `BiteFastNavHost.kt`, `onNavigateToOrderHistory` gọi `navController.navigate(OrdersDestination) { launchSingleTop = true }` mà không dùng cấu hình chuyển tab của BottomBar (`popUpTo(startDestination) { saveState = true }`). 
- Điều này đẩy `OrdersDestination` đè lên trên `ProfileDestination` trong cùng một nhánh ngăn xếp: `[Discovery, Profile, Orders]`.
- Khi người dùng ở `Orders` nhấn vào tab Profile trên BottomBar, lệnh `popUpTo(DiscoveryDestination.id) { saveState = true }` cố gắng pop cả Orders và Profile, gây ra xung đột trạng thái (state collision) trong Navigation Compose 2.8 khiến việc restore Profile bị treo hoặc không kích hoạt.

### Vấn đề 2: Nhấn icon quay lại (`<-`) trên Đơn hàng/Giỏ hàng bị văng về Trang chủ
- **Nguyên nhân:** TopAppBar của cả `OrderScreen.kt` và `CartScreen.kt` sử dụng icon `ArrowBack` (`<-`), nhưng sự kiện click lại bị gán cứng vào `onNavigateToHome` (gọi `navController.navigate(DiscoveryDestination)`).
- **Hệ quả UX:** Người dùng từ Profile sang Đơn hàng, hoặc từ Chi tiết quán ăn sang Giỏ hàng, khi bấm `<-` (mũi tên quay lại) đều bị cưỡng ép bay về Trang chủ (`DiscoveryDestination`) thay vì trở về màn hình trước đó.

### Vấn đề 3: Khách vãng lai bấm "Đăng nhập / Đăng ký" bị hỏi "Bạn có chắc muốn đăng xuất?"
- **Nguyên nhân:** Tại `ProfileScreen.kt` dòng 207:
  `if (uiState.isGuest) BiteFastButton(onClick = { onEvent(ProfileUiEvent.RequestLogout) })`
  Nút Đăng nhập lại gọi sự kiện `RequestLogout`, kích hoạt `LogoutConfirmDialog`.

---

## 📋 4. PHÂN RÃ CÔNG VIỆC CHI TIẾT (TASK BREAKDOWN)

### Task 1 (Size: S): Sửa lỗi Logic Khách vãng lai & Dead Click tại Profile
- **Mô tả:**
  1. Thêm `ProfileUiEvent.ClickLogin` vào `ProfileViewModel.kt`, khi kích hoạt sẽ phát `ProfileUiEffect.NavigateToLogin`.
  2. Tại `ProfileScreen.kt`, đổi `onClick` của nút "Đăng nhập / Đăng ký" khi `isGuest == true` thành `onEvent(ProfileUiEvent.ClickLogin)`.
  3. Tại `ProfileViewModel.kt`, xử lý `ClickEditProfile` phát ra `ProfileUiEffect.ShowSnackbar("Tính năng chỉnh sửa hồ sơ đang được cập nhật")` để loại bỏ dead-click.
  4. Sửa icon `Icons.Default.Logout` thành `Icons.AutoMirrored.Filled.Logout` để xóa compiler warning.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - Khách vãng lai bấm "Đăng nhập / Đăng ký" chuyển ngay tới `LoginDestination`, không hiện dialog đăng xuất.
  - Bấm "Chỉnh sửa hồ sơ" hiện Snackbar thông báo thân thiện.
- **Files:**
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileScreen.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileViewModel.kt`

---

### Task 2 (Size: M): Sửa Nút Quay Lại Thông Minh cho `OrderScreen` và `CartScreen`
- **Mô tả:**
  1. Trong `OrderRoute` và `OrderScreen.kt`, đổi `onNavigateToHome: () -> Unit` thành `onNavigateBack: () -> Unit`. Icon `ArrowBack` sẽ gọi `onNavigateBack()`.
  2. Trong `CartRoute` và `CartScreen.kt`, đổi `onNavigateToHome: () -> Unit` thành `onNavigateBack: () -> Unit`. Icon `ArrowBack` sẽ gọi `onNavigateBack()`.
  3. Trong `BiteFastNavHost.kt`:
     ```kotlin
     val onBackOrHome: () -> Unit = {
         if (!navController.popBackStack()) {
             navController.navigate(DiscoveryDestination) {
                 popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                 launchSingleTop = true
                 restoreState = true
             }
         }
     }
     ```
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - Đi từ Profile ➔ Đơn hàng ➔ Bấm `<-` ➔ Quay lại đúng Profile.
  - Đi từ Chi tiết quán ăn ➔ Giỏ hàng ➔ Bấm `<-` ➔ Quay lại đúng Chi tiết quán ăn.
  - Mở Đơn hàng hoặc Giỏ hàng từ BottomBar ➔ Bấm `<-` ➔ Quay về Trang chủ an toàn.
- **Files:**
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/OrderScreen.kt`
  - `feature/cart/src/main/kotlin/com/bitefast/feature/cart/CartScreen.kt`
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`

---

### Task 3 (Size: M): Tối Ưu Hóa Bộ Điều Hướng BottomBar & Lối Tắt Liên Tab
- **Mô tả:**
  1. Trong `BiteFastNavHost.kt`, cập nhật `onDestinationSelected` của `BiteFastBottomBar`:
     - Kiểm tra nếu destination đã có trong backstack thì dùng `popBackStack(destination.destination, inclusive = false)` để đưa màn hình lên trước một cách sạch sẽ, không làm kẹt stack.
     - Nếu chưa có thì thực hiện `navigate` với `saveState/restoreState` chuẩn Jetpack Navigation.
  2. Chuẩn hóa lối tắt `onNavigateToOrderHistory` trong `ProfileDestination`:
     - Cho phép chuyển đổi tab mượt mà hoặc popBackStack về Profile khi quay lại.
  3. Kết nối `CheckoutRoute` với `onNavigateToPaymentResult = { orderId, qrUrl, amount -> navController.navigate(PaymentResultDestination(orderId, amount, qrUrl)) }`.
- **Tiêu chí nghiệm thu (Acceptance Criteria):**
  - Chuyển từ Profile sang Đơn hàng, sau đó bấm bất kỳ tab nào (Profile, Cart, Discovery) đều chuyển đổi mượt mà 100%, không bị treo hay mất trạng thái.
  - Checkout với phương thức VietQR mở được `PaymentResultDestination`.
- **Files:**
  - `app/src/main/kotlin/com/bitefast/app/navigation/BiteFastNavHost.kt`

---

## ⚖️ 5. THAM VẤN NGƯỜI DÙNG & ĐỀ XUẤT PHƯƠNG ÁN (THEO ĐIỀU 3 & 4 AGENTS.MD)

### 🔹 Phương án 1 (Khuyến nghị - Recommended): Điều Hướng Linh Hoạt 2 Chiều (Smart BackStack Navigation)
- **Cách hoạt động:**
  - Khi từ Profile bấm "Lịch sử đơn hàng": Mở màn hình Đơn hàng.
  - Nút `<-` (ArrowBack) ở TopBar sẽ hoạt động như **nút Back thực thụ**: đưa người dùng quay lại Profile (hoặc quán ăn nếu từ giỏ hàng).
  - Khi bấm vào các Tab ở thanh BottomBar: Tự động dọn dẹp và chuyển tab mượt mà, bấm lại tab Profile sẽ ngay lập tức trở lại Profile.
- **Ưu điểm:** Đúng 100% thói quen của người dùng điện thoại (bấm mũi tên góc trên thì quay lại trang vừa đứng, bấm thanh dưới thì chuyển tab).
- **Nhược điểm:** Cần cập nhật nhẹ ở 4 file (`BiteFastNavHost.kt`, `ProfileScreen.kt`, `OrderScreen.kt`, `CartScreen.kt`).
- **Lý do khuyến nghị:** Giải quyết triệt để và tự nhiên nhất toàn bộ các hiện tượng lỗi mà bạn vừa gặp phải.

### 🔹 Phương án 2: Chuyển Tab Thuần Túy & Ẩn Mũi Tên Quay Lại Trên Các Tab Chính
- **Cách hoạt động:**
  - Theo chuẩn Material Design thuần túy: Các màn hình cấp cao nhất (Discovery, Cart, Orders, Profile) là các Tab độc lập, **không hiển thị mũi tên `<-`** trên TopBar.
  - Khi ở Profile bấm "Lịch sử đơn hàng": Đơn giản là đổi active tab sang tab "Đơn hàng" ở BottomBar. Người dùng muốn về Profile chỉ cần chạm lại tab "Tài khoản" ở dưới đáy.
- **Ưu điểm:** Đúng chuẩn Material Design gốc của Google.
- **Nhược điểm:** Mất nút bấm nhanh `<-` ở góc trên mà người dùng đã quen sử dụng.

---

## 🔒 6. BẢO VỆ DỰ ÁN & BƯỚC TIẾP THEO
- Sau khi bạn phê duyệt bản kế hoạch này và lựa chọn phương án:
  1. AI sẽ triển khai code từng Task (Domain ➔ Data ➔ Feature ➔ App).
  2. Chạy `./gradlew assembleDebug` để đảm bảo build xanh 100%.
  3. Báo cáo trạng thái mã nguồn sạch trên Local (Tuyệt đối không tự ý commit/push Git theo Điều 5).
