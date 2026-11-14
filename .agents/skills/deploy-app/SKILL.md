---
name: deploy-app
description: >-
  Deploys and releases new versions of the Offline Music Player for Android (APK) and iOS (Enterprise IPA).
  Use this skill whenever the user asks to release, deploy, publish, or bump the app version,
  create a new Git release tag, build iOS IPA with VNPT Enterprise signing, generate Apple OTA manifests, or produce GitHub Releases.
---

# 🚀 Deploy App Skill - Quy Trình Phát Hành Đa Nền Tảng (Android & iOS Enterprise)

Skill này cung cấp quy trình tự động hóa toàn diện từ chuẩn bị, kiểm thử, nâng phiên bản (Semantic Versioning), sinh Release Notes, đóng gói APK (Android) hoặc IPA ký số VNPT Enterprise (iOS), sinh file Apple OTA Manifest (`manifest.plist`), tạo mã QR Code cài đặt và phát hành lên GitHub Releases kèm giám sát Telegram HUB-LOG.

---

## 🏷️ 1. Quy Chuẩn Phân Biệt Ứng Dụng Trên GitHub Releases

| Nền tảng | Thư mục | Git Tag Định Danh | Tệp Đính Kèm (Release Assets) | Cơ Chế Nâng Cấp Client |
| :--- | :--- | :--- | :--- | :--- |
| **Android** | `android/` | `vX.Y.Z` (hoặc `android-vX.Y.Z`) | `app-release.apk` | Tải APK & PackageInstaller |
| **iOS Enterprise** | `ios/` | `ios-vX.Y.Z` | `MusicPlayer-ios-X.Y.Z.ipa`<br>`manifest.plist`<br>`MusicPlayer-ios-X.Y.Z-qr.png` | Apple OTA: `itms-services://?action=download-manifest&url=...` |

---

## ⚡ 2. Hướng Dẫn Sử Dụng Lệnh Triển Khai (`deploy.ps1`)

Kịch bản điều phối trung tâm: `.agents/skills/deploy-app/scripts/deploy.ps1`

### 1. Phát hành cho Android:
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -Platform android -BumpType patch
```

### 2. Phát hành cho iOS Enterprise (Ký số VNPT qua Mac M1 Pro):
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -Platform ios -CustomVersion "1.0.0" -ReleaseNotes "Bản phát hành iOS Enterprise đầu tiên cho Music Player."
```
> Tự động nạp chứng chỉ VNPT Enterprise (`docs/dev-ios/`), sync source code, chạy `xcodebuild archive`, xuất `.ipa`, sinh file `manifest.plist` cho Apple OTA, tạo mã QR Code và đẩy GitHub Release `ios-v1.0.0`.

### 3. Phát hành đồng thời cả hai nền tảng:
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -Platform all -CustomVersion "1.1.0"
```
powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -BumpType patch -ReleaseTitle "Vá lỗi và cải thiện độ ổn định"
```

#### 2. Phát hành bản tính năng mới (Minor: 1.0.0 -> 1.1.0)
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -BumpType minor -ReleaseTitle "Bổ sung tính năng mới" -ReleaseNotes "- Thêm Equalizer 5 băng tần`n- Nâng cấp cử chỉ vuốt MiniPlayer"
```

#### 3. Chạy thử nghiệm không ghi đĩa (Dry Run)
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -BumpType patch -DryRun -SkipTest
```

#### Các tham số của script `deploy.ps1`:
| Tham số | Ý nghĩa | Mặc định |
| :--- | :--- | :--- |
| `-BumpType` | Loại nâng cấp: `patch`, `minor`, `major`, `custom` | `patch` |
| `-CustomVersion` | Phiên bản chỉ định thủ công khi dùng `custom` (VD: `1.2.0`) | `""` |
| `-ReleaseTitle` | Tiêu đề cho bản phát hành | Tự động |
| `-ReleaseNotes` | Nội dung mô tả chi tiết các thay đổi | Tự động trích xuất từ git log |
| `-DryRun` | Chạy mô phỏng, không ghi đè file, không commit hay push | `$false` |
| `-SkipTest` | Bỏ qua bước chạy kiểm tra biên dịch trước khi deploy | `$false` |
| `-SkipPush` | Chỉ commit và tạo tag cục bộ, không push lên GitHub | `$false` |

---

### Bước 4: Kiểm Tra Tự Động Hóa CI/CD & GitHub Releases
1. Khi script đẩy tag `v*` (ví dụ `v1.0.1`) lên GitHub:
   - Workflow [`.github/workflows/release.yml`](../../.github/workflows/release.yml) sẽ tự động kích hoạt trên GitHub Actions.
   - Hệ thống tự động build file APK và tải đính kèm vào mục **Releases** của repository: `https://github.com/khuongnv/android_music_player/releases`.
2. Kiểm tra tính năng Auto-Update trong ứng dụng:
   - Mở ứng dụng -> **Settings** -> Nhấn **Check for Updates**.
   - Hộp thoại Apple Music sẽ hiển thị bản cập nhật mới kèm Release Notes vừa phát hành.

---

### Bước 5: Đóng Nhật Ký Truy Vết & Báo Cáo
1. Cập nhật nội dung chi tiết vào `Docs/logs/YYYY-MM-DD_release_v<version>.md`:
   - Ghi nhận phiên bản cũ và mới (`versionCode`, `versionName`).
   - Ghi nhận nội dung Release Notes đã đính kèm.
   - Đánh dấu trạng thái `✅ Hoàn thành`.
2. Cập nhật bảng tổng hợp [`Docs/logs/README.md`](../../Docs/logs/README.md).
3. Báo cáo kết quả phát hành cho người dùng.
