# ⚡ BẢN KẾ HOẠCH TRIỂN KHAI REAL-TIME FEATURES & NOTIFICATIONS (PHASE D)

> **Tài liệu tham chiếu:** `docs/plans/plan_completion_roadmap.md` (Phase D)  
> **Trạng thái:** ✅ Đã hoàn thành 100% (Completed & Verified - Phương án 1)  
> **Mục tiêu:** Nâng độ hoàn thiện dự án từ 98% lên **100% trọn vẹn**, hoàn tất 2 tính năng Real-time cốt lõi: **Real-time Order Tracking (WebSocket Live GPS)** và **Push & In-App Notification Engine (FCM + Room Cache + Heads-up Notifications)**.

---

## 🎯 1. MỤC TIÊU & PHẠM VI (OBJECTIVE & SCOPE)

### 1.1 Hiện Trạng (30% Hoàn Thiện Phần Real-time)
- **Đã có:**
  - `TrackingScreen` giao diện bản đồ, thông tin tài xế, thanh tiến trình bước giao hàng.
  - `NotificationScreen` hiển thị danh sách thông báo, bộ lọc theo loại (Đơn hàng, Khuyến mãi, Hệ thống).
  - `NotificationRepositoryImpl` quản lý danh sách in-memory `StateFlow`.
- **Khoảng trống còn lại (70%):**
  - **Chưa có kênh WebSocket Client**: Chưa có tầng xử lý WebSocket trong `:core:network` để kết nối Live Stream và tự động reconnect khi mất mạng.
  - **Dữ liệu thông báo chưa lưu trữ bền vững (Offline-First)**: Thông báo trong `NotificationRepositoryImpl` sẽ bị reset khi tắt app vì chưa được lưu vào Room DB.
  - **Chưa có Android System Notification Channels & Heads-up**: Chưa có service hiển thị thông báo đẩy trên thanh thông báo của điện thoại khi shipper cập nhật trạng thái đơn hàng.
  - **Chưa có Firebase Cloud Messaging (FCM) Service**: Chưa cấu hình Service nhận remote message trong `:app` kèm Deep Link vào thẳng chi tiết đơn hàng.

### 1.2 Mục Tiêu Đạt Chuẩn Enterprise 100%
1. Xây dựng **WebSocket Client chuẩn Enterprise** trong `:core:network` hỗ trợ WSS, tự động kết nối lại (Exponential Backoff Reconnect), và Mock WebSocket Simulator khi ở chế độ phát triển/demo.
2. Nâng cấp Room Database trong `:core:database` với `NotificationEntity` và `NotificationDao` (Offline-First), lưu trữ vĩnh viễn lịch sử thông báo trên máy người dùng.
3. Cập nhật `NotificationRepositoryImpl` trong `:core:data` kết nối trực tiếp với Room DB.
4. Tích hợp **Android Notification Engine** trong `:app`:
   - Hỗ trợ Android 13+ Notification Permission (`POST_NOTIFICATIONS`).
   - Tạo các Notification Channels chuyên biệt (`order_updates`, `promotions`).
   - Hiển thị Heads-up Notification kèm Deep-link `PendingIntent` nhảy thẳng tới `TrackingScreen`.
5. Tạo `BiteFastFirebaseMessagingService` sẵn sàng nhận Remote Message từ FCM.
6. Toàn bộ Unit Test mới đều đạt 100% pass trên cả 11 modules.

---

## 🏛️ 2. ĐỒ THỊ ẢNH HƯỞNG MODULE (MODULE IMPACT GRAPH)

```
[UI / Presentation Layer]
  ├── :feature:tracking      --> Nhận Live GPS Stream từ Domain qua WebSocket
  └── :feature:notification  --> Hiển thị lịch sử thông báo từ Room DB
           │
           ▼ (Inversion of Control)
[:core:domain]
  ├── GetOrderTrackingUseCase (Stream Live Order)
  └── Notification UseCases (Get, MarkRead, Delete)
           │
           ▼
[:core:data]
  ├── OrderRepositoryImpl     <── Kết hợp Room DB + WebSocket Stream
  └── NotificationRepositoryImpl <── Đọc/Ghi qua NotificationDao
           │
           ├────────────────────────────┐
           ▼                            ▼
[:core:network]                [:core:database]
  ├── BiteFastWebSocketClient   └── NotificationEntity & NotificationDao
  └── MockOrderTrackingSocket
           │
           ▼
[:app] (Trung tâm tích hợp)
  ├── BiteFastNotificationManager (NotificationCompat, Channels, DeepLinks)
  └── BiteFastFirebaseMessagingService (FCM Service)
```

---

## 📋 3. PHÂN RÃ CÔNG VIỆC (TASK BREAKDOWN)

Chia thành 7 tasks kích thước **S** hoặc **M** thực hiện tuần tự Bottom-Up:

### 🔹 TASK 1: Xây Dựng WebSocket Client & Event Stream Trong `:core:network` [Kích thước: M] - ✅ ĐÃ HOÀN THÀNH
- **Mô tả:** 
  - Tạo model sự kiện thời gian thực `OrderLiveTrackingEvent` (orderId, status, driverLat, driverLng, etaMinutes, progressPercent).
  - Xây dựng `BiteFastWebSocketClient` sử dụng `OkHttpClient.newWebSocket(...)` quản lý vòng đời kết nối, tự động gửi Heartbeat Ping/Pong, và reconnect với Exponential Backoff.
  - Xây dựng `MockOrderTrackingSocket` để giả lập chuyển động GPS shipper mượt mà khi chạy ở chế độ MOCK/DEV.
  - Đăng ký Provider trong `NetworkModule.kt`.
- **Tiêu chí nghiệm thu:** Kết nối WSS ổn định, phát ra `Flow<OrderLiveTrackingEvent>`, có fallback Simulator khi offline.
- **Bước xác minh:** Viết Unit Test `BiteFastWebSocketClientTest` pass 100%.
- **Danh sách file tác động:**
  - `core/network/src/main/kotlin/com/bitefast/core/network/websocket/OrderLiveTrackingEvent.kt`
  - `core/network/src/main/kotlin/com/bitefast/core/network/websocket/BiteFastWebSocketClient.kt`
  - `core/network/src/main/kotlin/com/bitefast/core/network/websocket/MockOrderTrackingSocket.kt`
  - `core/network/src/main/kotlin/com/bitefast/core/network/di/NetworkModule.kt`

---

### 🔹 TASK 2: Nâng Cấp Room Database Cho Thông Báo Bền Vững Trong `:core:database` [Kích thước: M] - ✅ ĐÃ HOÀN THÀNH
- **Mô tả:**
  - Tạo `NotificationEntity` (id, userId, title, message, type, orderId, isRead, createdAt).
  - Tạo `NotificationDao` với các hàm `getNotifications()`, `insertNotifications()`, `markAsRead()`, `markAllAsRead()`, `deleteNotification()`, `clearAll()`.
  - Cập nhật `BiteFastDatabase.kt`: Bổ sung entity, tăng version DB từ 2 lên 3 và cung cấp `notificationDao()`.
  - Cung cấp `provideNotificationDao` trong `DatabaseModule.kt`.
- **Tiêu chí nghiệm thu:** Room DB biên dịch sạch với KAPT, dữ liệu notification được lưu trữ bền vững.
- **Bước xác minh:** `./gradlew :core:database:compileDebugKotlin` ✅ BUILD SUCCESSFUL.
- **Danh sách file tác động:**
  - `core/database/src/main/kotlin/com/bitefast/core/database/entity/NotificationEntity.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/dao/NotificationDao.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/BiteFastDatabase.kt`
  - `core/database/src/main/kotlin/com/bitefast/core/database/di/DatabaseModule.kt`

---

### 🔹 TASK 3: Tầng Data - Đồng Bộ Notification Dao & Live Order Tracking Trong `:core:data` [Kích thước: M] - ✅ ĐÃ HOÀN THÀNH
- **Mô tả:**
  - Viết Mapper 2 chiều giữa `NotificationEntity` và Domain `Notification`.
  - Nâng cấp `NotificationRepositoryImpl` kết nối trực tiếp `NotificationDao` (Offline-First), khởi tạo dữ liệu mẫu lần đầu vào Room DB nếu database trống.
  - Nâng cấp `OrderRepositoryImpl`: Tích hợp `OrderTrackingSocketClient` vào luồng `getOrderStream(orderId)` để phát trực tiếp tọa độ tài xế thời gian thực.
- **Tiêu chí nghiệm thu:** Toàn bộ thao tác đọc/ghi thông báo đều phản hồi qua Room Flow; `getOrderStream` cập nhật tọa độ GPS real-time.
- **Bước xác minh:** `./gradlew :core:data:testDebugUnitTest` ✅ BUILD SUCCESSFUL (100% PASS).
- **Danh sách file tác động:**
  - `core/data/src/main/kotlin/com/bitefast/core/data/mapper/NotificationMappers.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/NotificationRepositoryImpl.kt`
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/OrderRepositoryImpl.kt`

---

### 🔹 TASK 4: Nâng Cấp Tracking ViewModel Kết Nối Real-time Stream Trong `:feature:tracking` [Kích thước: S] - ✅ ĐÃ HOÀN THÀNH
- **Mô tả:**
  - `TrackingViewModel` lắng nghe luồng Live Tracking từ `GetOrderTrackingUseCase` kết nối WebSocket stream.
  - Cập nhật mượt mà `driverLat`, `driverLng`, `progressPercent`, `etaMinutes` trên giao diện người dùng theo luồng socket.
- **Tiêu chí nghiệm thu:** UI map và polyline phản hồi tức thời theo luồng socket; unit test `TrackingViewModelTest` chạy pass 100%.
- **Bước xác minh:** `./gradlew :feature:tracking:testDebugUnitTest` ✅ BUILD SUCCESSFUL (100% PASS).
- **Danh sách file tác động:**
  - `feature/tracking/src/main/kotlin/com/bitefast/feature/tracking/TrackingViewModel.kt`
  - `feature/tracking/src/test/kotlin/com/bitefast/feature/tracking/TrackingViewModelTest.kt`

---

### 🔹 TASK 5: Xây Dựng Hệ Thống Notification & FCM Trong `:app` [Kích thước: M] - ✅ ĐÃ HOÀN THÀNH
- **Mô tả:**
  - Xây dựng `BiteFastNotificationManager`:
    - Tạo 2 Notification Channels: `bitefast_order_updates` (Độ ưu tiên cao - High Priority, có chuông & rung) và `bitefast_promotions` (Default).
    - Tạo hàm `showOrderNotification(orderId, title, message)` tạo `NotificationCompat.Builder` kèm `PendingIntent` Deep-link mở `MainActivity` nhảy thẳng vào màn hình theo dõi đơn hàng `TrackingScreen`.
  - Xây dựng `BiteFastFirebaseMessagingService`:
    - Kế thừa `FirebaseMessagingService`.
    - Xử lý `onNewToken(token)` lưu token vào Datastore.
    - Xử lý `onMessageReceived(remoteMessage)`: Phân tích payload, lưu vào `NotificationRepository` (Room DB), và gọi `BiteFastNotificationManager` hiển thị Heads-up Notification.
  - Cập nhật `AndroidManifest.xml` xin quyền `POST_NOTIFICATIONS` và khai báo Service.
  - Khởi tạo Notification Channels trong `BiteFastApplication`.
- **Tiêu chí nghiệm thu:** Hệ thống thông báo hệ thống hoạt động chuẩn mực trên Android 13+; click thông báo điều hướng chính xác.
- **Bước xác minh:** `./gradlew :app:assembleDebug` ✅ BUILD SUCCESSFUL.
- **Danh sách file tác động:**
  - `app/src/main/kotlin/com/bitefast/app/notification/BiteFastNotificationManager.kt`
  - `app/src/main/kotlin/com/bitefast/app/notification/BiteFastFirebaseMessagingService.kt`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/kotlin/com/bitefast/app/BiteFastApplication.kt`

---

### 🔹 TASK 6: Bổ Sung Unit Tests Cho Real-Time & Notification Components [Kích thước: M] - ✅ ĐÃ HOÀN THÀNH
- **Mô tả:**
  - Viết unit test cho `BiteFastWebSocketClientTest`: Test kết nối, nhận tin nhắn, chuyển đổi trạng thái, và kịch bản reconnect.
  - Viết unit test cho `NotificationRepositoryImplTest`: Test lưu Room DB, đánh dấu đã đọc, xóa thông báo.
  - Bổ sung test socket stream vào `OrderRepositoryTest`.
- **Tiêu chí nghiệm thu:** Pass 100% với MockK & Turbine.
- **Bước xác minh:** `./gradlew :core:network:testDebugUnitTest :core:data:testDebugUnitTest` ✅ BUILD SUCCESSFUL.
- **Danh sách file tác động:**
  - `core/network/src/test/kotlin/com/bitefast/core/network/websocket/BiteFastWebSocketClientTest.kt`
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/NotificationRepositoryImplTest.kt`
  - `core/data/src/test/kotlin/com/bitefast/core/data/repository/OrderRepositoryTest.kt`

---

### 🔹 TASK 7: Tổng Hợp & Đánh Giá Nghiệm Thu 100% Dự Án (Final Milestone) [Kích thước: S] - ✅ ĐÃ HOÀN THÀNH
- **Mô tả:**
  - Chạy `./gradlew testDebugUnitTest` toàn diện trên cả 11 modules.
  - Chạy `./gradlew assembleDebug` đảm bảo build APK thành công.
  - Cập nhật tiến độ dự án trong `docs/plans/plan_completion_roadmap.md` đạt mốc **100% Hoàn Thành Toàn Diện ✅**.
  - Báo cáo kết quả và giữ nguyên trạng thái Git sạch.
- **Tiêu chí nghiệm thu:** Toàn bộ test pass 100% (492 tasks), build APK hoàn tất (438 tasks).
- **Bước xác minh:** `./gradlew testDebugUnitTest && ./gradlew assembleDebug` ✅ TẤT CẢ BUILD SUCCESSFUL.
- **Danh sách file tác động:**
  - `docs/plans/plan_completion_roadmap.md`
  - `docs/plans/README.md`

---

## ⚠️ 4. RỦI RO & BIỆN PHÁP GIẢM THIỂU (RISKS & MITIGATIONS)

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| **Thiếu file `google-services.json` thật của Firebase** | Trung bình | `BiteFastNotificationManager` được thiết kế độc lập để phát Local Notification và mô phỏng thông báo ngay trong app; FCM Service chỉ kích hoạt khi có token hợp lệ, giúp app hoạt động 100% không crash kể cả khi chưa gắn Firebase project thật. |
| **Android 13+ (API 33) Runtime Permission `POST_NOTIFICATIONS`** | Trung bình | Xử lý xin quyền thông báo lúc khởi động hoặc khi người dùng đặt hàng thành công để đảm bảo Heads-up Notification luôn hiển thị. |
| **Room Database Migration khi nâng version lên 3** | Thấp | Cấu hình `fallbackToDestructiveMigration()` trong `DatabaseModule` cho môi trường phát triển để tránh crash khi thay đổi schema entity. |
| **Độ trễ và rò rỉ bộ nhớ từ WebSocket Listener** | Thấp | Sử dụng `callbackFlow` với `awaitClose { webSocket.cancel() }` để tự động giải phóng kết nối khi ViewModel bị hủy. |

---

## ❓ 5. CÂU HỎI THẢO LUẬN & ĐỀ XUẤT PHƯƠNG ÁN (OPEN QUESTIONS)

### ⚖️ ĐỀ XUẤT CÁC PHƯƠNG ÁN XỬ LÝ:

#### 🔹 Phương án 1 (Khuyến nghị - Recommended): Kiến Trúc Real-Time Kép Chuẩn Enterprise (WebSocket WSS + Mock Simulator + Dual Notification FCM/Local Room)
- **Mô tả:** 
  - Tầng mạng hỗ trợ đầy đủ WebSocket chuẩn WSS kèm bộ giả lập `MockOrderTrackingSocket` để test và demo thời gian thực ngay lập tức.
  - Hệ thống thông báo kết hợp cả Push FCM (sẵn sàng cho production) và `BiteFastNotificationManager` (tạo Android System Heads-Up Notification + lưu trữ Room DB vĩnh viễn).
- **Ưu điểm:**
  - Ứng dụng hoạt động sống động và đầy đủ 100% ngay trên Emulator/Device mà **không bị phụ thuộc vào việc phải có server backend thật hay tài khoản Firebase đang active**.
  - Khi backend thật sẵn sàng, chỉ cần truyền URL WSS và file `google-services.json` là hoạt động ngay không cần sửa code.
  - Đưa dự án đạt **100% hoàn hảo** trên mọi khía cạnh kiến trúc.
- **Nhược điểm:** Cần triển khai đầy đủ cả WebSocket Client và Local Notification Manager (7 tasks).
- **Lý do khuyến nghị:** BiteFast đã đạt 98% chuẩn Enterprise. Triển khai theo phương án này sẽ biến dự án thành một sản phẩm hoàn chỉnh, trình diễn được trọn vẹn cả GPS thời gian thực và thông báo hệ thống trên điện thoại.

#### 🔹 Phương án 2: Triển Khai Tối Giản (Chỉ Sử Dụng Polling + In-Memory Notification)
- **Mô tả:** Sử dụng polling HTTP lặp lại mỗi vài giây để cập nhật tọa độ tài xế, giữ thông báo trong RAM mà không lưu vào Room DB hay hiển thị notification hệ thống.
- **Ưu điểm:** Viết ít code hơn, hoàn thành nhanh hơn.
- **Nhược điểm:** Không đạt chuẩn Real-time đích thực, tốn pin, mất dữ liệu khi tắt app, không có thông báo đẩy trên thanh trạng thái điện thoại.

👉 **Bạn muốn chúng ta thực hiện theo Phương án 1 hay Phương án 2?**
