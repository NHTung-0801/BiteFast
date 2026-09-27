# Kế Hoạch Triển Khai Giai Đoạn 2: Data Layer Cường Hóa & Bảo Mật Phần Cứng Keystore/SQLCipher (Plan Phase 2)

> **Mã kế hoạch:** `plan_phase2_data_layer_security`  
> **Phiên bản:** 1.0 — Chuẩn Enterprise 10/10  
> **Căn cứ:** [plan_master_execution_roadmap.md](plan_master_execution_roadmap.md) & [bitefast_project_plan.md](../../bitefast_project_plan.md)  
> **Trạng thái:** Đang triển khai Task 2.1

---

## 1. MỤC TIÊU (OBJECTIVE)

Giai đoạn 2 tập trung nâng cấp toàn diện bảo mật dữ liệu lưu trữ tại máy (Data at Rest) và luồng dữ liệu mạng (Data in Transit):
1. **Task 2.1:** Bảo vệ toàn bộ Room Database (`bitefast.db`) bằng thuật toán mã hóa trang nhị phân **AES-256** thông qua **SQLCipher**, sử dụng Passphrase ngẫu nhiên 256-bit được quản lý bởi **Android Hardware Keystore (TEE / StrongBox)**.
2. **Task 2.2:** Kiểm thử tự động cơ chế phục hồi phiên `OkHttp 401 Silent Token Renewal` với `MockWebServer`, đảm bảo không phát sinh Race Condition khi có nhiều request đồng thời.
3. **Task 2.3:** Hoàn thiện mô hình dữ liệu Đơn nguồn Tin cậy (Single Source of Truth) trong Repository với `Mutex` bảo vệ giỏ hàng chống spam nút bấm.

---

## 2. LỆNH THỰC THI & KIỂM CHỨNG (COMMANDS)

```bash
# Kiểm tra biên dịch module database
./gradlew :core:database:assembleDebug

# Chạy unit tests module database
./gradlew :core:database:testDebugUnitTest

# Biên dịch toàn bộ ứng dụng và kiểm tra R8/ProGuard tương thích SQLCipher
./gradlew assembleDebug

# Cài đặt và kiểm tra trực tiếp trên máy ảo Pixel 7 (API 36)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. CẤU TRÚC DỰ ÁN & PHẠM VI TÁC ĐỘNG (PROJECT STRUCTURE)

```text
core/database/
├── src/main/kotlin/com/bitefast/core/database/
│   ├── BiteFastDatabase.kt                     # Cập nhật danh sách entities, thêm AddressDao
│   ├── dao/
│   │   ├── CartDao.kt
│   │   ├── RestaurantDao.kt
│   │   └── AddressDao.kt                       # [MỚI] Dao quản lý sổ địa chỉ giao hàng
│   ├── entity/
│   │   ├── CartItemEntity.kt
│   │   ├── RestaurantEntity.kt
│   │   └── AddressEntity.kt                    # [MỚI] Entity địa chỉ người dùng
│   ├── security/
│   │   └── KeystoreManager.kt                  # [MỚI] Quản lý sinh & mã hóa Passphrase qua Keystore/MasterKey
│   └── di/
│       └── DatabaseModule.kt                   # [CẬP NHẬT] Khởi tạo Room với SupportOpenHelperFactory(passphrase)
app/
└── proguard-rules.pro                          # Cấu hình bảo vệ lớp net.sqlcipher.**
```

---

## 4. QUY CHUẨN KỸ THUẬT & CODE STYLE (CODE STYLE & CONVENTIONS)

1. **Khóa phần cứng Keystore:**
   - Sử dụng `androidx.security.crypto.MasterKey` với `KeyScheme.AES256_GCM`.
   - Lưu trữ passphrase trong `EncryptedSharedPreferences` bảo vệ bởi MasterKey.
   - Tuyệt đối không hardcode mật khẩu dạng văn bản tĩnh trong code.
2. **SQLCipher Native Init:**
   - Gọi `System.loadLibrary("sqlcipher")` trước khi cấu hình `SupportOpenHelperFactory`.
   - Passphrase được truyền vào factory dưới dạng mảng byte (`ByteArray`).

---

## 5. RANH GIỚI TRIỂN KHAI (BOUNDARIES)

- **LUÔN LUÔN:**
  - Giữ database an toàn với `fallbackToDestructiveMigration()` trong giai đoạn dev để tránh conflict schema.
  - Sử dụng DI Hilt `@Singleton` cho `KeystoreManager` và `BiteFastDatabase`.
- **HỎI Ý KIẾN TRƯỚC KHI:**
  - Thay đổi cấu trúc bảng hoặc thêm migration phức tạp trước khi release.
- **TUYỆT ĐỐI KHÔNG:**
  - Không lưu token hay mật khẩu thô vào SharedPreferences thông thường.
  - Không push lên GitHub khi chưa có lệnh rõ ràng từ người dùng.

---

## 6. TIÊU CHÍ NGHIỆM THU TASK 2.1 (SUCCESS CRITERIA)

- [ ] `KeystoreManager` tạo và truy xuất Master Passphrase an toàn từ Hardware Keystore.
- [ ] `DatabaseModule` tích hợp thành công `SupportOpenHelperFactory(passphrase)` từ SQLCipher.
- [ ] Bổ sung `AddressEntity` và `AddressDao` vào Room Database, cung cấp qua Hilt DI.
- [ ] Dự án biên dịch thành công (`BUILD SUCCESSFUL`), chạy mượt mà trên máy ảo Pixel 7.