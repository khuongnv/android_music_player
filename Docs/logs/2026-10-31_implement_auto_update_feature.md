# 📋 Nhật Ký Thực Thi: Xây Dựng Tính Năng Auto-Update Qua GitHub Releases

- **Ngày thực hiện**: 2026-10-31 11:16:53
- **Agent phụ trách**: `lead-architect`, `compose-ui-specialist`, `storage-data-specialist`, `qa-perf-specialist`
- **Yêu cầu gốc**: Dựa vào `auto_update_app.md` hãy xây dựng tính năng upgrade app.
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)

### 1.1. Bối cảnh & Yêu cầu từ auto_update_app.md
- **Repository Remote**: `https://github.com/khuongnv/android_music_player.git` (Owner: `khuongnv`, Repo: `android_music_player`).
- **Endpoint kiểm tra**: `GET https://api.github.com/repos/khuongnv/android_music_player/releases/latest`.
- **Headers bắt buộc**: `User-Agent: AppleMusic-Android-Updater`, `Accept: application/vnd.github.v3+json`.
- **So sánh phiên bản (SemVer)**: Phân tách và so sánh theo quy chuẩn `[major, minor, patch]`, loại bỏ tiền tố `v`/`V`.
- **Tải APK & Xử lý Redirect**: Asset tải về là file `.apk`. GitHub releases chuyển hướng tải (HTTP 302 Found) sang AWS S3 storage; client phải tự động chuyển hướng và stream trực tiếp vào bộ nhớ tạm thời `cacheDir/updates/update.apk`.
- **Cài đặt trên Android**:
  - Khai báo quyền `INTERNET` và `REQUEST_INSTALL_PACKAGES` (Android 8.0+ / API 26+).
  - Cấu hình `androidx.core.content.FileProvider` với `file_paths.xml` để chia sẻ file APK an toàn qua content URI.
  - Sử dụng Intent `ACTION_VIEW` kèm cờ `FLAG_GRANT_READ_URI_PERMISSION`.
- **Giao diện chuẩn Apple Music**:
  - Tích hợp vào `SettingsScreen`: Card thông tin phiên bản với huy hiệu đỏ `AppleRed` (`v1.0.0`).
  - Modal Dialog bo góc `20.dp` hiển thị Release Notes cuộn mượt mà, thanh tiến trình tải tuyến tính và các nút bấm theo phong cách Apple Music.
- **Tự động hóa phát hành (CI/CD)**:
  - Thiết lập workflow `.github/workflows/release.yml` kích hoạt khi đẩy tag `v*`.

---

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)

- **Bước 1 (Cấu hình Android System & Quyền hạn)**:
  - Tạo tệp `app/src/main/res/xml/file_paths.xml` cấu hình đường dẫn bộ nhớ tạm `<cache-path name="updates" path="updates/" />`.
  - Cập nhật `app/src/main/AndroidManifest.xml`: Bổ sung quyền `INTERNET`, `REQUEST_INSTALL_PACKAGES` và khai báo `FileProvider`.
  - Cập nhật `versionName = "1.0.0"` trong `app/build.gradle.kts` theo chuẩn SemVer.
- **Bước 2 (Xây dựng Tầng Domain - Clean Architecture)**:
  - Tạo model `AppUpdateInfo.kt` chứa thông tin phiên bản, tiêu đề, ghi chú phát hành, URL tải và kích thước asset.
  - Tạo model `UpdateDownloadProgress.kt` báo cáo tiến độ tải theo byte và tỷ lệ phần trăm.
  - Tạo sealed interface `UpdateStatus.kt` mô hình hóa các trạng thái: `Idle`, `Checking`, `UpdateAvailable`, `UpToDate`, `Downloading`, `ReadyToInstall`, `Error`.
  - Tạo interface `UpdateRepository.kt` và 2 UseCase: `CheckAppUpdateUseCase.kt`, `DownloadAppUpdateUseCase.kt`.
- **Bước 3 (Xây dựng Tầng Data & Logic Cài Đặt)**:
  - Tạo `ApkInstaller.kt`: Kiểm tra quyền `canRequestPackageInstalls()`, tạo Uri qua `FileProvider` và kích hoạt Package Installer Intent của Android.
  - Tạo `UpdateRepositoryImpl.kt`:
    - Gọi GitHub API Releases endpoint với đầy đủ headers `User-Agent`.
    - Phân tích cú pháp JSON bằng `org.json.JSONObject`.
    - Lọc asset có đuôi `.apk`.
    - Triển khai thuật toán so sánh SemVer an toàn.
    - Tải streaming với bộ đệm `ByteArray(8192)` trên `Dispatchers.IO`, tự động xử lý chuỗi HTTP Redirect (302/301/307).
  - Đăng ký Hilt Singleton binding trong `AppModule.kt`.
- **Bước 4 (Triển khai Giao diện UI/UX Apple Music)**:
  - Cập nhật `SettingsUiState.kt` bổ sung các trường trạng thái cập nhật và phiên bản hiện tại.
  - Cập nhật `SettingsViewModel.kt` quản lý luồng kiểm tra bản cập nhật, tải file ngầm bằng Coroutines Flow và kích hoạt cài đặt.
  - Cập nhật `SettingsScreen.kt`:
    - Thêm Card "App Updates" đồng bộ màu sắc `AppleRed`, `DarkSurface` và `Gray93`.
    - Thiết kế `UpdateModalDialog` hỗ trợ hiển thị ghi chú phát hành (What's New), tiến trình tải phần trăm + MB và trạng thái hoàn tất.
- **Bước 5 (Thiết lập CI/CD GitHub Actions)**:
  - Tạo `.github/workflows/release.yml` tự động build APK và phát hành Release khi gắn tag `v*`.
- **Bước 6 (Khởi tạo Gradle Wrapper & Biên dịch kiểm tra)**:
  - Sinh bộ script `gradlew` và `gradlew.bat` cho kho lưu trữ.
  - Chạy biên dịch kiểm thử `compileDebugKotlin` và KSP, xác nhận build thành công 100%.

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)

| Thao tác | Đường dẫn tệp | Mô tả nội dung |
| :--- | :--- | :--- |
| `[Created]` | [`app/src/main/res/xml/file_paths.xml`](../../app/src/main/res/xml/file_paths.xml) | Cấu hình cache-path cho FileProvider chia sẻ APK |
| `[Modified]` | [`app/src/main/AndroidManifest.xml`](../../app/src/main/AndroidManifest.xml) | Bổ sung quyền INTERNET, INSTALL_PACKAGES và FileProvider |
| `[Modified]` | [`app/build.gradle.kts`](../../app/build.gradle.kts) | Cập nhật versionName lên chuẩn SemVer "1.0.0" |
| `[Created]` | [`app/src/main/java/com/apple/android/music/domain/model/AppUpdateInfo.kt`](../../app/src/main/java/com/apple/android/music/domain/model/AppUpdateInfo.kt) | Model lưu trữ thông tin bản cập nhật |
| `[Created]` | [`app/src/main/java/com/apple/android/music/domain/model/UpdateStatus.kt`](../../app/src/main/java/com/apple/android/music/domain/model/UpdateStatus.kt) | Sealed interface đại diện cho các trạng thái cập nhật |
| `[Created]` | [`app/src/main/java/com/apple/android/music/domain/model/UpdateDownloadProgress.kt`](../../app/src/main/java/com/apple/android/music/domain/model/UpdateDownloadProgress.kt) | Model báo cáo tiến độ tải file APK |
| `[Created]` | [`app/src/main/java/com/apple/android/music/domain/repository/UpdateRepository.kt`](../../app/src/main/java/com/apple/android/music/domain/repository/UpdateRepository.kt) | Interface trừu tượng tầng Domain cho việc cập nhật |
| `[Created]` | [`app/src/main/java/com/apple/android/music/domain/usecase/CheckAppUpdateUseCase.kt`](../../app/src/main/java/com/apple/android/music/domain/usecase/CheckAppUpdateUseCase.kt) | UseCase kiểm tra phiên bản mới từ GitHub |
| `[Created]` | [`app/src/main/java/com/apple/android/music/domain/usecase/DownloadAppUpdateUseCase.kt`](../../app/src/main/java/com/apple/android/music/domain/usecase/DownloadAppUpdateUseCase.kt) | UseCase tải APK với tiến trình stream |
| `[Created]` | [`app/src/main/java/com/apple/android/music/data/local/updater/ApkInstaller.kt`](../../app/src/main/java/com/apple/android/music/data/local/updater/ApkInstaller.kt) | Tiện ích kích hoạt cài đặt APK an toàn qua Intent |
| `[Created]` | [`app/src/main/java/com/apple/android/music/data/repository/UpdateRepositoryImpl.kt`](../../app/src/main/java/com/apple/android/music/data/repository/UpdateRepositoryImpl.kt) | Triển khai UpdateRepository với GitHub API và HTTP streaming |
| `[Modified]` | [`app/src/main/java/com/apple/android/music/di/AppModule.kt`](../../app/src/main/java/com/apple/android/music/di/AppModule.kt) | Đăng ký Hilt binding cho UpdateRepository |
| `[Modified]` | [`app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsUiState.kt`](../../app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsUiState.kt) | Bổ sung trạng thái cập nhật vào UI State |
| `[Modified]` | [`app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsViewModel.kt`](../../app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsViewModel.kt) | Xử lý logic cập nhật, tải file và gọi cài đặt |
| `[Modified]` | [`app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsScreen.kt`](../../app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsScreen.kt) | Giao diện Card cập nhật và Modal Dialog chuẩn Apple Music |
| `[Created]` | [`.github/workflows/release.yml`](../../.github/workflows/release.yml) | GitHub Actions tự động build và upload APK lên Release |
| `[Created]` | [`gradlew`](../../gradlew), [`gradlew.bat`](../../gradlew.bat) | Bộ Gradle Wrapper chuẩn cho dự án |
| `[Modified]` | [`Docs/logs/README.md`](README.md) | Cập nhật chỉ mục danh mục log hoàn thành |

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)

- **Biên dịch Kotlin & KSP**:
  - Đã chạy lệnh: `.\gradlew.bat compileDebugKotlin`
  - Kết quả: `BUILD SUCCESSFUL in 2m 13s` (18/18 actionable tasks executed).
  - Không có lỗi biên dịch (Compilation Errors).
- **Rà soát an toàn & Memory**:
  - File tải APK được lưu vào thư mục `cacheDir/updates/` đảm bảo không bị ô nhiễm bộ nhớ người dùng.
  - Sử dụng buffer stream 8KB ghi trực tiếp vào đĩa cứng, không nạp toàn bộ file APK dung lượng lớn vào RAM (tránh rủi ro OutOfMemoryError).
  - Xử lý ngoại lệ đầy đủ (mất mạng, GitHub Rate Limit 403, 404, không tìm thấy file APK) không làm crash ứng dụng.
- **Tuân thủ UI/UX**:
  - Đúng màu nhận diện `AppleRed` (`#FA2D55`), card bo góc mềm mại, hiển thị rõ ràng thông tin phiên bản.

---

## 5. Kết Luận & Hướng Dẫn Tiếp Theo (Conclusion & Next Steps)

- Tính năng Auto-Update qua GitHub Releases đã được tích hợp hoàn chỉnh và sẵn sàng sử dụng.
- **Để phát hành bản cập nhật mới trong tương lai**:
  1. Tăng `versionName` trong `app/build.gradle.kts` (ví dụ `1.0.1`).
  2. Tạo tag git theo chuẩn SemVer:
     ```bash
     git tag v1.0.1
     git push origin v1.0.1
     ```
  3. GitHub Actions sẽ tự động build APK và gắn vào GitHub Release `v1.0.1`. Người dùng trong ứng dụng bấm "Check for Updates" sẽ nhận được thông báo cập nhật ngay lập tức.
