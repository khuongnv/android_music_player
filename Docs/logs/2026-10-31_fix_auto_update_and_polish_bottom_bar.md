# 📋 Nhật Ký Thực Thi: Sửa lỗi Auto-Update từ phiên bản thấp và nâng cấp UI/UX thanh menu Ký ức

- **Ngày thực hiện**: 2026-10-31 16:55:00
- **Agent phụ trách**: `lead-architect`, `qa-perf-specialist`, `compose-ui-specialist`, `storage-data-specialist`
- **Yêu cầu gốc**: Sửa lỗi auto update khi cài từ version thấp (v1.0.1) lên bản mới nhất. Xem lại UI/UX thanh menu (đang bị lạc tông so với app), đổi "Timeline 24h" thành "Ký ức".
- **Trạng thái**: 🔄 Đang hoàn tất chuẩn bị phát hành bản sửa lỗi

---

## 1. Phân Tích & Nguyên Nhân Gốc Rễ (Root Cause Analysis)

### 1.1. Tại sao cập nhật từ phiên bản thấp (v1.0.1) lên bản mới vẫn bị lỗi?
- **Nguyên nhân cốt lõi phát hiện được**:
  1. Khi build cục bộ (Local Windows), Gradle sử dụng `~/.android/debug.keystore` của máy nhà phát triển, có chữ ký:
     `SHA-256: E1:8C:40:8C:52:C5:66:4F:3A:67:E8:F6:71:F6:3E:BF:CB:FE:0C:41:4B:AD:8E:49:FF:AA:2D:D2:50:08:2E:E2`
     (Đây chính là chữ ký của file APK `v1.0.0` và `v1.0.1` đã cài đặt trên điện thoại người dùng).
  2. Khi đưa lên GitHub Actions (Ubuntu runner), do runner là môi trường máy ảo tạm thời (ephemeral VM) không có sẵn keystore cố định, Gradle trên runner đã tự động sinh một `debug.keystore` mới ngẫu nhiên cho mỗi lần build:
     - `v1.1.1`: `SHA-256: F1:83:2A:49...`
     - `v1.2.0`: `SHA-256: 07:26:95:D3...`
  3. Hệ điều hành Android kiểm tra cực kỳ nghiêm ngặt: Khi APK tải về có chữ ký khác với bản đang cài trên máy, PackageInstaller sẽ từ chối cài đặt với mã lỗi `INSTALL_FAILED_UPDATE_INCOMPATIBLE` ("Chữ ký không khớp / Gói không hợp lệ").
- **Giải pháp dứt điểm**:
  - Đưa trực tiếp tệp keystore chuẩn `app/keystore/debug.keystore` vào mã nguồn dự án (Git).
  - Cấu hình `signingConfigs` trong `app/build.gradle.kts` trỏ cố định vào `keystore/debug.keystore`.
  - Mọi bản build (dù ở local hay trên GitHub Actions) từ nay về sau đều sử dụng 100% cùng một chứng chỉ ký số duy nhất (`E1:8C:40:8C...`), giải quyết triệt để lỗi chữ ký không tương thích.

### 1.2. Nâng cấp UI/UX thanh menu đáy (Bottom Navigation Bar)
- **Vấn đề thanh menu "lạc tông"**:
  - Mặc định của Compose Material 3 `NavigationBar` có chiều cao quá lớn (`80.dp`), hiệu ứng màu tím/tint từ tonalElevation không ăn nhập với nền đen sâu `#000000` / `#1C1C1E` và nền trắng `#FFFFFF` chuẩn Apple Music.
  - Tên "Timeline 24h" mang tính kỹ thuật, khô cứng, chưa toát lên tinh thần nghệ thuật của Apple Music.
- **Giải pháp tái thiết kế**:
  - Đổi tên tab "Timeline 24h" thành **"Ký ức"** (Memories).
  - Sử dụng icon `Icons.Filled.AutoAwesome` (chùm sao lấp lánh - biểu tượng đặc trưng của Apple Memories).
  - Chuẩn hóa layout Tab Bar theo tỷ lệ chuẩn iOS: chiều cao thanh lịch `52.dp`, đường viền tóc (hairline border `0.5.dp`) sắc sảo ở cạnh trên, loại bỏ bóng mờ tonal, phông chữ SF Pro tinh tế với điểm nhấn `AppleRed` (`#FA2D55`).
  - Đồng bộ màn hình `TimelineScreen.kt` thành trang **"Ký ức"** với phụ đề nghệ thuật và phân mục đồng nhất.

---

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)

- **Bước 1**: Sao chép keystore gốc từ máy phát triển vào `app/keystore/debug.keystore`.
- **Bước 2**: Khai báo khối `signingConfigs` cố định trong `app/build.gradle.kts` cho cả `release` và `debug`.
- **Bước 3**: Gia cố `ApkInstaller.kt` với các cờ `FLAG_GRANT_READ_URI_PERMISSION`, `FLAG_ACTIVITY_NEW_TASK`, `EXTRA_NOT_UNKNOWN_SOURCE`.
- **Bước 4**: Tối ưu `SettingsViewModel.kt`: Giữ trạng thái `ReadyToInstall` khi yêu cầu cấp quyền cài đặt để người dùng có thể bấm "Cài đặt lại" ngay sau khi bật quyền mà không bị văng lỗi.
- **Bước 5**: Tái thiết kế toàn diện `BottomNavBar.kt`:
  - Đổi tên `"Timeline 24h"` -> **`"Ký ức"`**.
  - Đổi icon sang `AutoAwesome`.
  - Giảm chiều cao xuống `52.dp`, bổ sung hairline divider phía trên.
- **Bước 6**: Cập nhật tiêu đề màn hình `TimelineScreen.kt` thành **"Ký ức"**.
- **Bước 7**: Kiểm tra đóng gói release cục bộ bằng `assembleRelease` và xác minh chứng chỉ SHA-256 qua `apksigner`.

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)
- `[Created]` `app/keystore/debug.keystore`: Keystore cố định cho toàn bộ quy trình build.
- `[Modified]` `app/build.gradle.kts`: Cấu hình signingConfigs sử dụng keystore cố định.
- `[Modified]` `app/src/main/java/com/apple/android/music/data/local/updater/ApkInstaller.kt`: Bổ sung cờ Intent an toàn.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsViewModel.kt`: Tối ưu luồng xử lý quyền cài đặt.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/components/BottomNavBar.kt`: Tái thiết kế Apple Music Tab Bar chuẩn mực, đổi tên "Ký ức".
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/timeline/TimelineScreen.kt`: Cập nhật tiêu đề và phong cách màn hình Ký ức.

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)
- Kiểm tra chữ ký bằng `apksigner`:
  - `Signer #1 certificate SHA-256 digest: e18c408c52c5664f3a67e8f671f63ebfcbfe0c414bad8e49ffaa2dd250082ee2`
  - Khớp 100% với phiên bản `v1.0.0` và `v1.0.1`.
- Kiểm tra build Gradle `assembleRelease`: BUILD SUCCESSFUL.
