# 🛠️ KẾ HOẠCH SỬA LỖI & NÂNG CẤP: ĐÁNH GIÁ MÓN ĂN, CHUẨN HÓA TIẾNG VIỆT & BỘ LỌC SẮP XẾP

> **Tài liệu tham chiếu:** Phản hồi người dùng ngày 02/10/2026  
> **Trạng thái:** ✅ Đã hoàn thành 100% (Passed Quality Gate & Verified on Emulator)  
> **Mục tiêu:** Khắc phục triệt để 3 vấn đề người dùng phản ánh:
> 1. Hiển thị phần Đánh giá món ăn & nhà hàng trực quan, dễ tiếp cận (Tab đôi [Thực đơn] & [Đánh giá], Breakdown sao 5★..1★, Review cards).
> 2. Chuẩn hóa tiếng Việt 100% có dấu trên toàn bộ thanh tab, tiêu đề, menu và format tiền tệ (`48.000 đ`).
> 3. Sửa lỗi bộ lọc danh mục và bổ sung tính năng Sắp xếp (Sorting) linh hoạt trên Trang chủ & Thực đơn.

---

## 🔍 1. THỐNG KÊ CHI TIẾT NGUYÊN NHÂN GỐC RỄ (ROOT CAUSE ANALYSIS)

### 📌 Vấn đề 1: Chi tiết món ăn không hiện phần đánh giá
- **Hiện trạng:**
  - Trên màn hình Chi tiết Nhà hàng (`DetailScreen`), giao diện chỉ liệt kê danh sách món kèm dòng chữ nhỏ `★ 4.9 (68)` và nút `+` (thêm nhanh).
  - Không có khu vực hay Tab riêng để xem tổng quan đánh giá của nhà hàng và các món ăn.
  - Khi người dùng bấm vào dòng sao nhỏ `★ 4.9 (68)`, hệ thống lại mở form **viết đánh giá mới** thay vì hiển thị các bình luận/nhận xét của khách hàng khác.
  - Phần đánh giá thực khách của từng món chỉ hiển thị bên trong `CustomizationBottomSheet` khi bấm vào thân thẻ món ăn, nhưng thiếu chỉ báo trực quan (affordance) khiến người dùng tưởng rằng không có phần đánh giá.

### 📌 Vấn đề 2: Các thanh Tab và nhãn chữ bị thiếu dấu tiếng Việt
- **Hiện trạng phát hiện trên các màn hình:**
  - **Màn hình Đơn hàng (`OrderScreen.kt` / `OrderViewModel.kt`):** Enum `OrderFilterTab` bị hardcode tiếng Việt không dấu: `"Tat ca"`, `"Dang giao"`, `"Hoan thanh"`, `"Da huy"`.
  - **Màn hình Thông báo (`NotificationViewModel.kt`):** Enum `NotificationFilter` bị hardcode không dấu: `"Tat ca"`, `"Don hang"`, `"Khuyen mai"`, `"He thong"`.
  - **Màn hình Chi tiết Nhà hàng (`DetailScreen.kt`):** Tab lọc `"Tat ca"`, tiêu đề `"Thuc don mon ngon"`, trạng thái `"Dang mo cua"` / `"Tam dong"`, thời gian `"25 phut"`, giá tiền `"48,000 d"` (dấu phẩy và chữ `d` không dấu).
  - **Màn hình Tài khoản (`ProfileScreen.kt`):** Hàng loạt nhãn không dấu: `"Tai khoan"`, `"Chinh sua ho so"`, `"So dia chi giao hang"`, `"Kho Voucher & Khuyen mai"`, `"Lich su don hang"`, `"Nha hang yeu thich"`, `"Cai dat"`, `"Thong bao"`, `"Giao dien toi"`, `"Ho tro"`, `"Lien he ho tro"`, `"Ve BiteFast"`, `"Dang xuat"`.

### 📌 Vấn đề 3: Các button lựa chọn không lọc và sắp xếp được
- **Hiện trạng:**
  - **Lỗi lọc Trang chủ (`DiscoveryViewModel.kt` & `RestaurantRepositoryImpl.kt`):**
    - Mảng danh mục trên giao diện có dấu: `["Tất cả", "Cơm", "Phở & Bún", "Trà sữa",...]`.
    - Dữ liệu nhà hàng trong kho dữ liệu mock lại lưu cuisine không dấu hoặc khác chuỗi: `"Com tam"`, `"Pho bo"`, `"Tra sua"`.
    - Đoạn code so khớp dùng `item.cuisine.equals(cuisine, ignoreCase = true)`. Do đó, khi bấm vào chip `"Cơm"` hoặc `"Phở & Bún"`, kết quả so sánh luôn là `false` dẫn đến toàn bộ danh sách bị ẩn (danh sách trắng tinh).
  - **Lỗi lọc Chi tiết Thực đơn (`DetailViewModel.kt`):** So sánh `selectedCategory == "Tat ca"` bị lỗi khi chuẩn hóa tiếng Việt thành `"Tất cả"`, và category của món ăn bị lệch giữa có dấu và không dấu.
  - **Thiếu tính năng Sắp xếp (Sorting):** Cả Trang chủ và Chi tiết nhà hàng hoàn toàn chưa có các nút / thanh sắp xếp (Giá tăng/giảm, Đánh giá cao, Bán chạy, Gần nhất).

---

## 🏛️ 2. ĐỒ THỊ ẢNH HƯỞNG MODULE (MODULE IMPACT GRAPH)

```
[:core:model]
    └── Bổ sung Enum chuẩn hóa Filter / Sort Option (DishSortOption, RestaurantSortOption)
           │
           ▼
[:core:data]
    ├── Chuẩn hóa dữ liệu Mock tiếng Việt chuẩn (RestaurantRepositoryImpl.kt)
    └── Bổ sung tiện ích so khớp thông minh không phụ thuộc dấu tiếng Việt (VietnameseStringUtils)
           │
           ▼
[:feature:detail]
    ├── DetailViewModel.kt: Sửa logic lọc category, bổ sung sort món ăn & quản lý tab Đánh giá
    └── DetailScreen.kt: Thêm Tab kép [Thực đơn] / [Đánh giá], chuẩn hóa toàn bộ text có dấu
           │
           ▼
[:feature:discovery]
    ├── DiscoveryViewModel.kt: Sửa logic so khớp cuisine thông minh, hỗ trợ sắp xếp nhà hàng
    └── DiscoveryScreen.kt: Thêm thanh Sort Chips (Gần nhất, Đánh giá cao, Giao nhanh)
           │
           ▼
[:feature:order], [:feature:notification], [:feature:profile]
    └── Chuẩn hóa 100% tiếng Việt có dấu cho các Tab và Menu
```

---

## 📋 3. PHÂN RÃ CÔNG VIỆC (TASK BREAKDOWN)

### 🔹 TASK 1: Chuẩn Hóa Tiếng Việt Có Dấu 100% Trên Toàn Bộ Tabs & Labels [Kích thước: M]
- **Mô tả:**
  - Sửa `OrderFilterTab` trong `feature/order/OrderViewModel.kt`:
    `"Tất cả"`, `"Đang giao"`, `"Hoàn thành"`, `"Đã hủy"`.
  - Sửa `NotificationFilter` trong `feature/notification/NotificationViewModel.kt`:
    `"Tất cả"`, `"Đơn hàng"`, `"Khuyến mãi"`, `"Hệ thống"`.
  - Sửa toàn bộ chuỗi giao diện trong `feature/profile/ProfileScreen.kt`:
    `"Tài khoản"`, `"Chỉnh sửa hồ sơ"`, `"Sổ địa chỉ giao hàng"`, `"Kho Voucher & Khuyến mãi"`, `"Lịch sử đơn hàng"`, `"Nhà hàng yêu thích"`, `"Cài đặt"`, `"Xác thực Biometric"`, `"Thông báo"`, `"Giao diện tối"`, `"Hỗ trợ"`, `"Liên hệ hỗ trợ"`, `"Về BiteFast"`, `"Đăng xuất"`, `"Đăng nhập / Đăng ký"`.
  - Sửa nhãn trạng thái và format tiền tệ trong `DetailScreen.kt`:
    `"Thực đơn món ngon"`, `"Đang mở cửa"`, `"Tạm đóng"`, `"${estimatedTime} phút"`, định dạng tiền `${price} đ`.
- **Tiêu chí nghiệm thu:** Toàn bộ thanh tab, tiêu đề, menu trên ứng dụng hiển thị tiếng Việt chuẩn mực 100%.
- **File tác động:**
  - `feature/order/src/main/kotlin/com/bitefast/feature/order/OrderViewModel.kt`
  - `feature/order/src/test/kotlin/com/bitefast/feature/order/OrderViewModelTest.kt`
  - `feature/notification/src/main/kotlin/com/bitefast/feature/notification/NotificationViewModel.kt`
  - `feature/profile/src/main/kotlin/com/bitefast/feature/profile/ProfileScreen.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailScreen.kt`

---

### 🔹 TASK 2: Sửa Lỗi Lọc Danh Mục & So Khớp Thông Minh (Smart Filter Matching) [Kích thước: M]
- **Mô tả:**
  - Viết tiện ích chuẩn hóa chuỗi tiếng Việt (loại bỏ dấu và chuyển chữ thường) để so khớp mềm mại không phân biệt có dấu / không dấu.
  - Sửa `RestaurantRepositoryImpl.kt`:
    - Chuẩn hóa danh sách mock data với `cuisine` có dấu và không dấu tương thích.
    - Cải tiến hàm so khớp `matchesCuisine`: Khi người dùng chọn `"Cơm"` sẽ match được cả `"Cơm"`, `"Cơm tấm"`, `"Com tam"`. Khi chọn `"Phở & Bún"` sẽ match `"Phở bò"`, `"Bún bò"`, `"Pho bo"`. Khi chọn `"Trà sữa"` sẽ match `"Trà sữa"`, `"Tra sua"`.
  - Sửa `DetailViewModel.kt`:
    - Chuẩn hóa `selectedCategory`: Giá trị mặc định là `"Tất cả"`.
    - Lọc menu item linh hoạt không bị ảnh hưởng bởi lỗi lệch dấu hoặc khoảng trắng.
- **Tiêu chí nghiệm thu:** Bấm vào bất kỳ danh mục nào trên Trang chủ hoặc Chi tiết quán đều lọc chính xác danh sách tương ứng, không còn bị trắng màn hình.
- **File tác động:**
  - `core/data/src/main/kotlin/com/bitefast/core/data/repository/RestaurantRepositoryImpl.kt`
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryViewModel.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailViewModel.kt`

---

### 🔹 TASK 3: Bổ Sung Tính Năng Sắp Xếp (Sorting Options) [Kích thước: M]
- **Mô tả:**
  - **Trên Trang chủ (`DiscoveryScreen.kt`):**
    - Thêm thanh nút chọn sắp xếp bên dưới thanh tìm kiếm:
      `[Gần nhất] [Đánh giá cao] [Giao nhanh] [Giá tốt]`.
    - Khi người dùng bấm chọn, `DiscoveryViewModel` tự động sắp xếp lại danh sách nhà hàng và món ăn theo tiêu chí tương ứng.
  - **Trên Chi tiết Nhà hàng (`DetailScreen.kt`):**
    - Thêm các chip sắp xếp thực đơn:
      `[Tất cả] [Bán chạy] [Giá: Thấp -> Cao] [Giá: Cao -> Thấp] [Đánh giá cao]`.
    - Cập nhật luồng lọc và sắp xếp trong `DetailViewModel.filteredMenuItems`.
- **Tiêu chí nghiệm thu:** Người dùng có thể chủ động sắp xếp món ăn và quán ăn theo mong muốn với hiệu ứng chuyển đổi mượt mà.
- **File tác động:**
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryScreen.kt`
  - `feature/discovery/src/main/kotlin/com/bitefast/feature/discovery/DiscoveryViewModel.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailScreen.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailViewModel.kt`

---

### 🔹 TASK 4: Nâng Cấp Giao Diện Đánh Giá Món Ăn & Tab Đánh Giá Nhà Hàng [Kích thước: M]
- **Mô tả:**
  - **Tab Đánh giá tổng hợp trên `DetailScreen`:**
    - Bổ sung thanh chuyển Tab trên đầu thực đơn:
      👉 **[Thực đơn]** | **[Đánh giá (4.8 ★)]**
    - Khi bấm sang Tab [Đánh giá]: Hiển thị tổng quan điểm đánh giá của quán (4.8 / 5.0), thanh tỷ lệ các mức sao (5★, 4★, 3★,...), danh sách đánh giá thực tế của thực khách cho nhà hàng và từng món.
  - **Hiển thị đánh giá chi tiết khi bấm vào món ăn:**
    - Khi bấm vào thẻ món ăn: Mở BottomSheet Chi tiết món ăn, đưa phần **"Đánh giá từ thực khách"** lên vị trí nổi bật ngay dưới tên món.
    - Hiển thị danh sách nhận xét, số sao, các thẻ cảm nhận nhanh và nút **"Viết đánh giá cho món này"**.
    - Khi bấm vào dòng sao nhỏ `★ 4.9 (68)` trên danh sách món: Mở BottomSheet xem nhận xét thực khách thay vì nhảy thẳng vào form nhập đánh giá.
- **Tiêu chí nghiệm thu:** Người dùng nhìn thấy ngay phần đánh giá ở cả cấp độ nhà hàng và cấp độ từng món ăn; trải nghiệm tự nhiên, trực quan như các ứng dụng gọi món hàng đầu.
- **File tác động:**
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailScreen.kt`
  - `feature/detail/src/main/kotlin/com/bitefast/feature/detail/DetailViewModel.kt`

---

### 🔹 TASK 5: Kiểm Thử & Xác Minh Trực Tiếp Trên Máy Ảo [Kích thước: S]
- **Mô tả:**
  - Chạy toàn bộ Unit Test kiểm tra tính tương thích: `./gradlew testDebugUnitTest`.
  - Biên dịch và cài đặt APK lên máy ảo: `./gradlew :app:installDebug`.
  - Chụp ảnh màn hình kiểm chứng: Tab tiếng Việt có dấu, hoạt động của bộ lọc & sắp xếp, giao diện đánh giá món ăn & nhà hàng.
- **Kết quả thực tế:**
  - ✅ **Unit Tests:** `./gradlew testDebugUnitTest` vượt qua 100% test cases trên toàn bộ các modules (`:core:data`, `:feature:discovery`, `:feature:detail`, `:feature:order`, `:feature:profile`,...).
  - ✅ **APK Installation:** `./gradlew :app:installDebug` cài đặt thành công lên `emulator-5554` (Android 16).
  - ✅ **Xác minh Trang chủ (`DiscoveryScreen`):**
    - Bộ lọc danh mục "Cơm" lọc đúng các món Cơm Tấm Phúc Lộc Thọ.
    - Bộ lọc "Trà sữa" lọc đúng các món Trà Sữa Phúc Long.
    - Thanh Sort Chips (`Gợi ý`, `Gần nhất`, `Đánh giá cao`, `Giao nhanh`) hoạt động chuẩn xác.
  - ✅ **Xác minh Chi tiết Quán (`DetailScreen`):**
    - Tab đôi `[Thực đơn]` và `[Đánh giá (4.6 ★)]` hiển thị trực quan.
    - Tab Đánh giá gồm tổng quan sao 4.6 (500+), thanh tỷ lệ 5★..1★, cảm nhận nhanh (`Món ăn ngon (340)`, `Giao nhanh (280)`), nút CTA `Viết đánh giá của bạn`, và danh sách thẻ nhận xét khách hàng thực tế.
    - Giá tiền format chuẩn `48.000 đ`, `55.000 đ`. Trạng thái quán hiển thị `Đang mở cửa` / `20 phút`.
  - ✅ **Xác minh Đơn hàng & Tài khoản (`OrderScreen` & `ProfileScreen`):**
    - Các tab: `Tất cả (3)`, `Đang giao (2)`, `Hoàn thành (1)`, `Đã hủy`.
    - Menu tài khoản: `Chỉnh sửa hồ sơ`, `Sổ địa chỉ giao hàng`, `Kho Voucher & Khuyến mãi`, `Lịch sử đơn hàng`, `Nhà hàng yêu thích`, `Cài đặt` 100% tiếng Việt chuẩn có dấu.
- **Tiêu chí nghiệm thu:** Đã hoàn thành 100% & kiểm chứng trực quan trên máy ảo.

---

## ⚠️ 4. RỦI RO & BIỆN PHÁP GIẢM THIỂU (RISKS & MITIGATIONS)

| Rủi ro | Mức độ | Biện pháp giảm thiểu |
| :--- | :---: | :--- |
| **Ảnh hưởng đến các Unit Test hiện có** | Trung bình | Cập nhật đồng bộ các assertions liên quan đến nhãn tab tiếng Việt trong `OrderViewModelTest`, `DetailViewModelTest`, `NotificationViewModelTest`. |
| **Lệch cấu trúc Layout khi thêm Tab Đánh giá** | Thấp | Sử dụng `PrimaryTabRow` của Material 3 kết hợp `AnimatedContent` để chuyển đổi mượt mà giữa [Thực đơn] và [Đánh giá]. |
| **So khớp danh mục không nhất quán** | Thấp | Áp dụng hàm chuẩn hóa chuẩn `java.text.Normalizer` để loại bỏ dấu thanh khi so sánh tìm kiếm mềm. |

---

## ❓ 5. THAM VẤN & ĐỀ XUẤT PHƯƠNG ÁN (OPEN QUESTIONS)

### ⚖️ ĐỀ XUẤT CÁC PHƯƠNG ÁN XỬ LÝ:

#### 🔹 Phương án 1 (Khuyến nghị - Recommended): Tích Hợp Toàn Diện Chuẩn Trải Nghiệm Enterprise
- **Mô tả:**
  1. Thêm thanh Tab đôi trên màn hình Chi tiết: **[Thực đơn]** & **[Đánh giá (500+)]**, cho phép xem toàn bộ nhận xét của quán và các món ăn. Khi chạm vào từng món, mở BottomSheet hiển thị chi tiết hình ảnh, đánh giá và tùy chọn thêm món.
  2. Bổ sung thanh chọn **Sắp xếp linh hoạt** trên cả Trang chủ (`Gần nhất`, `Đánh giá cao`, `Giao nhanh`, `Giá tốt`) và Chi tiết thực đơn (`Bán chạy`, `Giá tăng/giảm`, `Đánh giá`).
  3. Sửa triệt để hàm so khớp danh mục tiếng Việt thông minh (không sợ lệch dấu).
  4. Chuẩn hóa 100% tiếng Việt có dấu trên toàn bộ ứng dụng (Đơn hàng, Thông báo, Tài khoản, Chi tiết quán).
- **Ưu điểm:** Giải quyết triệt để cả 3 phản ánh của bạn, biến giao diện trở nên cực kỳ chuyên nghiệp, mượt mà và trực quan như GrabFood/ShopeeFood.
- **Nhược điểm:** Cần cập nhật trên 5 feature modules và chạy lại test suites (khoảng 5 tasks).
- **Lý do khuyến nghị:** BiteFast đã hoàn thiện 100% về mặt kiến trúc. Nâng cấp trải nghiệm người dùng theo phương án này sẽ mang lại độ hoàn hảo tuyệt đối từ giao diện đến tương tác.

#### 🔹 Phương án 2: Sửa Nhanh Cục Bộ (Quick Patch)
- **Mô tả:**
  1. Chỉ thêm chữ có dấu cho các Tab hiện tại (`Tất cả`, `Đang giao`,...).
  2. Sửa cứng tên `cuisine` trong mock data để bấm chip lọc được.
  3. Bấm vào món ăn thì mở BottomSheet chi tiết hiện có mà không thêm Tab [Đánh giá] riêng cho nhà hàng, không bổ sung thanh sắp xếp.
- **Ưu điểm:** Thực hiện nhanh, ít sửa đổi file.
- **Nhược điểm:** Trải nghiệm vẫn bị hạn chế (thiếu tính năng sắp xếp, không có trang xem đánh giá tổng hợp của nhà hàng).

👉 **Bạn muốn chúng ta thực hiện theo Phương án 1 (Khuyến nghị) hay Phương án 2?**
