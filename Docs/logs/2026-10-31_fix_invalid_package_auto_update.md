# 📋 Nhật Ký Thực Thi: Khắc phục lỗi cập nhật "Gói không hợp lệ" (Thiếu chữ ký APK)

- **Ngày thực hiện**: 2026-10-31 13:25:00
- **Agent phụ trách**: `qa-perf-specialist`, `lead-architect`, `storage-data-specialist`
- **Yêu cầu gốc**: Rà soát và kiểm tra fix v1.0.1 không update lên được do báo "Gói không hợp lệ"
- **Trạng thái**: 🔄 Đang kiểm thử & chuẩn bị phát hành v1.1.1

---

## 1. Phân Tích & Nguyên Nhân Gốc Rễ (Root Cause Analysis)

### Hiện tượng
Khi người dùng đang sử dụng phiên bản `v1.0.1` nhấn **Cập nhật ngay** lên `v1.1.0`, Android PackageInstaller tải tệp APK về nhưng báo lỗi:
> **"Gói không hợp lệ"** (The package appears to be corrupt / `INSTALL_PARSE_FAILED_NO_CERTIFICATES`).

### Nguyên nhân gốc rễ
1. **APK không được ký số (Unsigned APK)**:
   - Trong `app/build.gradle.kts`, khối `buildTypes.release` chưa được cấu hình `signingConfig`.
   - Lệnh `./gradlew assembleRelease` mặc định tạo ra tệp `app-release-unsigned.apk`.
   - Android OS yêu cầu mọi tệp APK khi cài đặt hoặc nâng cấp đều phải có chữ ký hợp lệ (V1/V2 Scheme). Khi không có chữ ký, Android trả về mã lỗi `INSTALL_PARSE_FAILED_NO_CERTIFICATES`, tương ứng với thông báo giao diện "Gói không hợp lệ".
2. **Xung đột chứng chỉ cập nhật (Signature Mismatch)**:
   - Bản `v1.0.1` cài đặt trên máy người dùng được ký bằng `debug.keystore` (`SHA-256: e18c408c52c5664f3a67e8f671f63ebfcbfe0c414bad8e49ffaa2dd250082ee2`).
   - Việc cập nhật đè (In-place update) trên Android yêu cầu chữ ký của APK mới phải trùng khớp 100% với phiên bản hiện tại trên thiết bị (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).
3. **Quy trình CI/CD GitHub Actions bắt nhầm APK unsigned**:
   - Trong `.github/workflows/release.yml`, câu lệnh `find app/build/outputs/apk/ -name "*.apk" | head -n 1` đã nhặt trúng `app-release-unsigned.apk` thay vì APK đã ký.

---

## 2. Kế Hoạch & Chi Tiết Các Bước Thực Hiện (Execution Steps)

### Bước 1: Cấu hình ký số Release trong `app/build.gradle.kts`
- Gán `signingConfig = signingConfigs.getByName("debug")` cho `buildTypes.release`.
- Đảm bảo APK phát hành luôn được ký bởi cùng Keystore đồng nhất với bản đang chạy trên thiết bị người dùng.

### Bước 2: Gia cố `ApkInstaller.kt` & `file_paths.xml`
- Thêm cờ `apkFile.setReadable(true, false)` để file APK có quyền đọc toàn hệ thống trên Linux/Android filesystem.
- Tra cứu danh sách các Activity xử lý Package Installer trên các dòng máy tùy biến (Samsung OneUI, Xiaomi MIUI, ColorOS...) và gọi `grantUriPermission` rõ ràng kèm `FLAG_GRANT_READ_URI_PERMISSION`.
- Mở rộng cấu hình FileProvider trong `file_paths.xml` bao gồm `<cache-path>`, `<external-cache-path>`, `<files-path>`.

### Bước 3: Lọc APK đã ký trong `UpdateRepositoryImpl.kt`
- Ưu tiên chọn tệp APK không chứa từ khóa `"unsigned"` từ GitHub Releases assets.
- Ngăn ngừa tải nhầm file APK chưa ký nếu repository có nhiều assets.

### Bước 4: Tối ưu GitHub Actions Workflow `.github/workflows/release.yml`
- Loại bỏ cờ fallback mơ hồ, bắt buộc tìm đúng `app-release.apk` hoặc loại trừ `*unsigned*`.
- Dừng quy trình (exit 1) ngay nếu không tìm thấy APK đã ký.

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)
- `[Modified]` `app/build.gradle.kts`: Thêm `signingConfig = signingConfigs.getByName("debug")` cho release.
- `[Modified]` `app/src/main/res/xml/file_paths.xml`: Bổ sung đường dẫn cache, external cache, files.
- `[Modified]` `app/src/main/java/com/apple/android/music/data/local/updater/ApkInstaller.kt`: `setReadable(true, false)` và `grantUriPermission` đa package.
- `[Modified]` `app/src/main/java/com/apple/android/music/data/repository/UpdateRepositoryImpl.kt`: Bỏ qua asset có nhãn `unsigned`.
- `[Modified]` `.github/workflows/release.yml`: Khóa chặt logic đóng gói và upload APK đã ký.

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)
- Chạy unit tests: `./gradlew testDebugUnitTest`
- Build release APK: `./gradlew assembleRelease`
- Kiểm tra chữ ký bằng `apksigner`:
  - `APK Signature Scheme v2: true`
  - Digest SHA-256: `e18c408c52c5664f3a67e8f671f63ebfcbfe0c414bad8e49ffaa2dd250082ee2` (Khớp 100% với debug APK).
- Phát hành bản vá `v1.1.1` và kiểm tra GitHub Release asset.

---

## 5. Kết Luận & Hướng Dẫn Tiếp Theo (Conclusion & Next Steps)
- Bản cập nhật `v1.1.1` sẽ được đóng gói và ký đầy đủ, cho phép người dùng từ `v1.0.1` nâng cấp mượt mà trực tiếp trong ứng dụng.
