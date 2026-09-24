# 🍔 BITEFAST — FOOD DELIVERY & LIVE TRACKING (ANDROID NATIVE)

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-8.7.0-green.svg?logo=android)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/Compose_BOM-2024.12.01-brightgreen.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI%2FUDF-orange.svg)](https://developer.android.com/topic/architecture)
[![Security Level](https://img.shields.io/badge/Security-OWASP%20Mobile%20Top%2010-red.svg)](https://owasp.org)
[![A11y Standard](https://img.shields.io/badge/Accessibility-WCAG%202.1%20AA-purple.svg)](https://www.w3.org/WAI/standards-guidelines/wcag/)

> **BiteFast** là ứng dụng di động đặt đồ ăn và theo dõi đơn hàng theo thời gian thực (Real-time Order Tracking) được xây dựng theo chuẩn **Enterprise Grade (Full 10/10 Standard)**. Dự án kết hợp công nghệ hiện đại nhất trên nền tảng Android Native với kiến trúc phân tầng đa module nghiêm ngặt, cơ chế bảo mật cấp ngân hàng và khả năng tiếp cận toàn diện cho mọi đối tượng người dùng.

---

## 🏛️ 1. TỔNG QUAN KIẾN TRÚC HỆ THỐNG (SYSTEM ARCHITECTURE)

Dự án áp dụng mô hình **Enterprise Clean Architecture** kết hợp luồng dữ liệu đơn hướng **MVI / UDF (Unidirectional Data Flow)** nghiêm ngặt:

```
┌─────────────────────────────────────────────────────────────────┐
│               PRESENTATION LAYER (Jetpack Compose M3)           │
│   • Stateless Composables • Shimmer Skeleton • TalkBack Semantics│
│   • MVI ViewModels: StateFlow<UiState> + Channel<UiEffect>      │
└────────────────────────────────┬────────────────────────────────┘
                                 │ Observes State & Dispatches Events
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│           DOMAIN LAYER (Pure Kotlin JVM - Zero Android SDK)     │
│   • 35+ UseCases • Domain Models • Repository Interfaces        │
└────────────────────────────────▲────────────────────────────────┘
                                 │ Implements Contracts
                                 │
┌────────────────────────────────┴────────────────────────────────┐
│               DATA LAYER (Single Source of Truth)               │
│   • Repository Implementations với Mutex bảo vệ Race-Condition  │
│   ┌─────────────────────────────┬─────────────────────────────┐ │
│   │   Local Encrypted Storage   │     Remote API Services     │ │
│   │ • Room DB with SQLCipher    │ • Retrofit 2.11 + OkHttp    │ │
│   │ • AES-256 GCM DataStore     │ • Certificate Pinning       │ │
│   └─────────────────────────────┴─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📁 2. CẤU TRÚC MULTI-MODULE (MODULARIZATION STRUCTURE)

Hệ thống được module hóa theo phương pháp **Feature-by-Layer + Core Sharing**, quản lý tập trung bằng Gradle Convention Plugins (`build-logic` Kotlin DSL):

```
BiteFast/
├── build-logic/                          # Gradle Convention Plugins (Kotlin DSL)
│   └── convention/                       # Application, Library, Compose, Hilt, Jvm Plugins
├── gradle/                               # Version Catalog tập trung (libs.versions.toml)
├── app/                                  # Application Class, Single Activity & Central NavHost
│
├── core/                                 # Các phân hệ chia sẻ dùng chung
│   ├── model/                            # Data classes & Entities nghiệp vụ thuần khiết
│   ├── domain/                           # Pure Kotlin UseCases & Repository Interfaces (Zero Android)
│   ├── data/                             # Cài đặt Repositories & Mappers 2 chiều (DTO ↔ Entity ↔ Domain)
│   ├── database/                         # Room Database mã hóa toàn phần với SQLCipher
│   ├── network/                          # OkHttp Certificate Pinning, AuthInterceptor, TokenAuthenticator
│   ├── datastore/                        # Encrypted Preferences lưu trữ token & cấu hình phiên
│   ├── designsystem/                     # M3 Theme, Custom Shimmer Brush, Accessible Components
│   ├── common/                           # Coroutine Dispatchers, Result sealed interface, BaseViewModel
│   └── testing/                          # Test Doubles, Fake Repositories & Shared Test Rules
│
├── feature/                              # Các phân hệ tính năng giao diện độc lập (Zero Cross-Feature Coupling)
│   ├── auth/                             # Đăng nhập, Đăng ký, Chế độ Khách (Guest Mode) & Login Gate
│   ├── discovery/                        # Trang chủ, Danh mục món ăn, Tìm kiếm debounced & Shimmer
│   ├── detail/                           # Chi tiết nhà hàng, Chọn size, Topping BottomSheet
│   ├── cart/                             # Giỏ hàng, Stepper 48dp, Xử lý xung đột nhà hàng
│   ├── checkout/                         # Xác thực địa chỉ, Áp mã Voucher, Thanh toán bảo vệ vân tay
│   ├── tracking/                         # Bản đồ Google Maps Compose, Shipper Polyline Simulation
│   ├── order/                            # Lịch sử đơn hàng có bộ lọc & Tính năng Smart Re-Order 1 chạm
│   ├── profile/                          # Thông tin cá nhân, Sổ địa chỉ giao hàng & Cài đặt app
│   ├── rating/                           # Đánh giá 1-5 sao, Chọn tag nhanh & Đánh giá ẩn danh
│   └── notification/                     # Quản lý thông báo đẩy Firebase Cloud Messaging (FCM)
│
└── docs/                                 # Tài liệu dự án
    └── plans/                            # Thư mục lưu trữ toàn bộ các kế hoạch thực thi chi tiết
```

---

## 🚀 3. TÍNH NĂNG NGHIỆP VỤ NỔI BẬT (CORE CAPABILITIES)

1. **Khám Phá & Tìm Kiếm Thông Minh (Discovery & Smart Search):**
   - Lọc món ăn theo danh mục dạng Carousel mượt mà.
   - Thanh tìm kiếm tối ưu hóa Debounce 300ms giảm tải truy vấn.
   - Hiệu ứng tải khung xương (Skeleton Shimmer) phủ ánh sáng tuyến tính 1200ms.
2. **Chế Độ Khách & Cổng Đăng Nhập (Guest Mode & Login Gate):**
   - Cho phép khách vãng lai tự do duyệt món, thêm vào giỏ hàng cục bộ mà không bị ép buộc đăng nhập.
   - Tự động bật `LoginGateBottomSheet` khi thực hiện thao tác nhạy cảm (Thanh toán, Đánh giá, Xem hồ sơ).
   - Bảo toàn Deep Link: Đăng nhập xong tự động đưa người dùng trở lại đúng màn hình thao tác trước đó.
3. **Phòng Chống Xung Đột Giỏ Hàng (Restaurant Conflict Resolution):**
   - Cảnh báo rõ ràng khi người dùng cố thêm món từ nhà hàng khác: Cho phép chọn *"Tạo giỏ hàng mới"* hoặc *"Giữ giỏ hàng hiện tại"*.
   - Khóa tuần tự hóa `Mutex` tại Repository chống triệt để tình trạng lệch dữ liệu khi bấm tăng/giảm dồn dập (Race-condition free).
4. **Thanh Toán Bảo Vệ Sinh Trắc Học (Biometric Checkout):**
   - Tích hợp `BiometricPrompt` cấp độ `BIOMETRIC_STRONG` (Keystore phần cứng) cho các đơn hàng giá trị cao hoặc thanh toán thẻ tín dụng.
5. **Theo Dõi Đơn Hàng Thời Gian Thực (Live Polyline Tracking):**
   - Tích hợp Google Maps Compose vẽ tuyến đường giao hàng.
   - Mô phỏng tài xế di chuyển trơn tru theo thuật toán nội suy tọa độ (Spherical Linear Interpolation).
6. **Đặt Lại Đơn Cũ 1 Chạm (Smart Re-Order):**
   - Tự động kiểm tra quán cũ có đang mở cửa không, các món và topping có còn hàng không trước khi nạp lại giỏ.

---

## 🔒 4. KIẾN TRÚC BẢO MẬT DOANH NGHIỆP (SECURITY 10/10)

- **Mã Hóa Dữ Liệu Tại Chỗ (Data at Rest Encryption):**
  - Room SQLite Database được mã hóa toàn bộ bằng **SQLCipher AES-256**. Passphrase được sinh ngẫu nhiên và lưu an toàn trong phần cứng thiết bị (Android Keystore Hardware TEE / StrongBox).
  - Encrypted DataStore sử dụng Jetpack Security `MasterKey` chuẩn `AES256_GCM`.
- **Bảo Vệ Kết Nối Mạng (Network Security in Transit):**
  - **Certificate Pinning (HPKP/SPKI SHA-256):** Khóa cứng mã băm chứng chỉ SSL của máy chủ, ngăn chặn 100% các cuộc tấn công nghe lén Man-in-the-Middle (MITM) qua Proxy (Charles, Burp Suite).
  - `network_security_config.xml` chặn tuyệt đối mọi lưu lượng không an toàn (HTTP Cleartext Traffic).
- **Quản Lý Phiên Ngầm An Toàn (Silent Token Renewal):**
  - OkHttp `TokenAuthenticator` kết hợp `Mutex` giải quyết triệt để tình trạng bão refresh token khi nhiều request đồng thời nhận mã 401 Unauthorized.
- **Che Giấu Khóa Nhạy Cảm Bằng NDK C++:**
  - Ẩn toàn bộ API Key nhạy cảm trong thư viện native C++ JNI với thuật toán XOR Masking, chống decompile ngược bytecode.

---

## ♿ 5. HỆ THỐNG TIẾP CẬN TOÀN DIỆN (ACCESSIBILITY WCAG 2.1 AA)

- **Hợp Nhất Ngữ Nghĩa (Semantics Merging):** Áp dụng `Modifier.clearAndSetSemantics` trên các thẻ món ăn và nhà hàng để TalkBack đọc một câu mô tả hoàn chỉnh, tránh đọc vụn vặt từng phần tử văn bản.
- **Custom Accessibility Actions:** Giúp người khiếm thị vuốt lên/xuống để tăng giảm số lượng món trên `QuantitySelector` mà không cần dò tìm nút bấm nhỏ.
- **Touch Target Chuẩn:** Mọi nút bấm, chip và checkbox đều áp dụng diện tích chạm tối thiểu **48dp × 48dp**.
- **Dynamic Text Scaling 200%:** Hỗ trợ phóng to cỡ chữ hệ thống lên 200% không làm tràn viền hay vỡ bố cục giao diện.
- **Hỗ Trợ Reduce Motion:** Tự động tắt hiệu ứng chuyển động vô hạn đối với người dùng mắc hội chứng tiền đình.

---

## 🧪 6. CHIẾN LƯỢC KIỂM THỬ (TEST PYRAMID & COVERAGE TARGETS)

```
        / \
       /   \        10% UI Tests (Compose UI Semantics & Robot Tests)
      /-----\
     /       \      20% Integration Tests (MockWebServer, Room Encrypted In-Memory)
    /---------\
   /           \    70% Unit Tests (Pure Kotlin JVM: UseCases, ViewModels, Repositories)
  /-------------\
```
- **Domain Layer:** JUnit 5 + MockK + Turbine ➔ Cam kết độ bao phủ **≥ 95%**.
- **Data & ViewModel Layer:** JUnit 5 + MockWebServer ➔ Cam kết độ bao phủ **≥ 85%**.
- **Chốt chặn CI/CD:** Hệ thống Kover tự động từ chối build nếu không đạt ngưỡng tối thiểu 85%.

---

## 🛠️ 7. HƯỚNG DẪN CÀI ĐẶT & BIÊN DỊCH (GETTING STARTED)

### Yêu Cầu Hệ Thống:
- **JDK:** OpenJDK 17 hoặc 21 LTS.
- **Android Studio:** Ladybug (2024.2.1+) hoặc Koala.
- **Android SDK:** Compile SDK 35, Min SDK 24.

### Các Lệnh Biên Dịch Cơ Bản:
```bash
# Clone dự án về máy
git clone <repository_url>
cd BiteFast

# Kiểm tra cú pháp và chạy toàn bộ Unit Tests
./gradlew test

# Chạy báo cáo độ bao phủ mã nguồn Kover
./gradlew koverHtmlReport

# Biên dịch gói cài đặt Debug APK
./gradlew assembleDebug

# Biên dịch gói phát hành Release tối ưu hóa ProGuard/R8
./gradlew assembleRelease
```

---

## 📚 8. HỆ THỐNG TÀI LIỆU DỰ ÁN (DOCUMENTATION)

Mọi kế hoạch phát triển và tài liệu thiết kế đều được quản lý tại:
- 📋 [docs/plans/README.md](docs/plans/README.md) — Mục lục và quy ước quản lý kế hoạch.
- 🎯 [docs/plans/plan_master_execution_roadmap.md](docs/plans/plan_master_execution_roadmap.md) — Lộ trình thực thi chi tiết 6 giai đoạn.
- 🎨 [docs/plans/plan_ui_design_specs_architecture.md](docs/plans/plan_ui_design_specs_architecture.md) — Kế hoạch hệ thống file thiết kế giao diện đồng bộ.
- 📐 [bitefast_project_plan.md](bitefast_project_plan.md) — Bản đặc tả kỹ thuật kiến trúc gốc 10/10 Enterprise.

---

> **BiteFast Engineering Team** — *Xây dựng với niềm đam mê về Code sạch, Kiến trúc vững chắc và Trải nghiệm người dùng đỉnh cao.*
