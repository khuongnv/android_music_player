# 🎵 Offline Music Player (Dual-Native Android & iOS)

A premium, high-performance offline music player for **Android** and **iOS**, heavily inspired by the clean aesthetics of **Apple Music** and **Liquid Glass** materials. 

Structured as a **Dual-Native Monorepo**, the project delivers the highest performance (60/120 FPS), lowest audio latency, and authentic native platform integration on both operating systems.

---

![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS-brightgreen.svg)
![Architecture](https://img.shields.io/badge/Architecture-Dual--Native%20Monorepo-blue.svg)
![UI/UX](https://img.shields.io/badge/Design-Apple%20Music%20x%20Liquid%20Glass-red.svg)
![Android Stack](https://img.shields.io/badge/Android-Kotlin%20%7C%20Jetpack%20Compose%20%7C%20Media3-orange.svg)
![iOS Stack](https://img.shields.io/badge/iOS-Swift%20%7C%20SwiftUI%20%7C%20AVFoundation-purple.svg)
![Distribution](https://img.shields.io/badge/Distribution-GitHub%20OTA%20%7C%20VNPT%20Enterprise-success.svg)

---

## 🗂️ 1. Hành Trình & Kiến Trúc Tái Cấu Trúc (Project Restructuring)

### Bối cảnh tái cấu trúc
- **Giai đoạn ban đầu**: Dự án được khởi tạo như một ứng dụng đơn nền tảng thuần Android với toàn bộ mã nguồn nằm trực tiếp tại thư mục gốc (`app/`, `build.gradle.kts`, `gradle/`).
- **Nhu cầu mở rộng**: Nhằm phục vụ nhu cầu phát hành cho cả hệ sinh thái iOS trong môi trường doanh nghiệp nội bộ (Enterprise In-House), nhóm phát triển đã tiến hành khảo sát và đánh giá hai hướng tiếp cận:
  1. *Chuyển đổi toàn bộ sang Flutter*: Đa nền tảng một mã nguồn nhưng gặp hạn chế về độ trễ âm thanh cấp thấp, khó khai thác sâu các API chuyên biệt của Apple Music/Audio Engine và hạn chế hiệu ứng blur/material phức tạp ở tần số 120Hz.
  2. *Tái cấu trúc thành Dual-Native Monorepo (Được chọn)*: Tách biệt rõ ràng 2 nền tảng Native độc lập, dùng chung quy chuẩn thiết kế UI/UX, giao thức CI/CD và hệ thống Swarm Agent điều phối.

### Cấu trúc thư mục Monorepo hiện tại:
```text
android-music-player/
├── android/                         # Nền tảng Android Native
│   ├── app/                         # Mã nguồn Kotlin, Jetpack Compose, Resources
│   ├── gradle/                      # Gradle Wrapper
│   ├── build.gradle.kts             # Root Gradle build configuration
│   └── settings.gradle.kts          # Cấu hình module Android
│
├── ios/                             # Nền tảng iOS Native
│   ├── MusicPlayer/                 # Mã nguồn Swift & SwiftUI
│   │   ├── Core/                    # Audio Engine, Storage, Scanner, Updater
│   │   ├── Models/                  # Data Models (Song, Album, History, Playlist)
│   │   ├── Views/                   # SwiftUI Components, Screens, Navigation
│   │   └── Resources/               # Info.plist, Assets.xcassets, LaunchScreen
│   ├── build_mac.sh                 # Script tự động hóa build & code-sign trên Mac
│   └── project.yml / xcconfig       # Cấu hình dự án Xcode
│
├── .agents/                         # Hệ thống Swarm Multi-Agent & Kỹ năng CI/CD
│   ├── skills/
│   │   ├── deploy-app/              # Tự động hóa build, ký số và phát hành OTA
│   │   ├── offline-music-swarm/     # Điều phối và phân rã nhiệm vụ đa agent
│   │   └── swarm-dispatch/          # Phân công tác vụ chuyên môn
│   └── rules/                       # Quy tắc thiết kế UI/UX Apple Music bắt buộc
│
├── Docs/                            # Tài liệu kỹ thuật, Epics & Audit Logs
│   ├── dev-ios/                     # Hồ sơ chứng chỉ Enterprise VNPT, Mobileprovision
│   ├── logs/                        # Nhật ký thực thi chi tiết (Bắt buộc theo AGENTS.md)
│   │   ├── README.md                # Danh mục tổng hợp tất cả các phiên làm việc
│   │   └── YYYY-MM-DD_<task>.md     # Chi tiết từng tính năng / bản sửa lỗi
│   ├── UI_UX_STYLE_GUIDE.md         # Quy chuẩn thiết kế Apple Music x Liquid Glass
│   ├── EPICS_SWIFTUI_IOS_PROJECT.md # Danh sách Epics chuyển đổi & triển khai iOS
│   └── OTA_UPDATE_ARCHITECTURE.md   # Kiến trúc cập nhật OTA cho Android & iOS
│
├── releases/                        # Thư mục lưu trữ artifact build (.apk, .ipa, manifest, QR)
└── AGENTS.md                        # Quy ước vận hành Swarm Multi-Agent & Logging
```

---

## ✨ 2. Tính Năng Đồng Bộ Cả Hai Nền Tảng (Feature Parity)

| Tính Năng | Android (Kotlin / Compose) | iOS (Swift / SwiftUI) | Ghi Chú |
| :--- | :---: | :---: | :--- |
| **Phát nhạc Offline 100%** | ✅ | ✅ | Không yêu cầu Internet, tuyệt đối riêng tư |
| **Bộ quét nhạc cục bộ** | ✅ MediaStore API | ✅ AVURLAsset & MediaLibrary | Tự động phân loại Bài hát, Album, Nghệ sĩ |
| **Trích xuất Ảnh bìa (Cover Art)** | ✅ MediaStore / Coil | ✅ ID3 APIC & MPMediaItem | Lưu đĩa cache JPEG, đồng bộ Lockscreen |
| **Giao diện Apple Music x Liquid Glass** | ✅ AMOLED Black & Rim | ✅ AMOLED Black & UltraThin | Thiết kế dock nổi, hiệu ứng mờ kính sang trọng |
| **MiniPlayer & Now Playing Sheet** | ✅ Floating MiniPlayer | ✅ Floating MiniPlayer | Vuốt đóng dạng sheet, nút Chevron Down |
| **Dynamic Blurred Artwork** | ✅ RenderEffect Blur | ✅ `.blur(radius: 60)` | Làm mờ động theo màu sắc bìa bài hát đang phát |
| **Ký ức (24h Interactive Timeline)** | ✅ 24h Scrubber Bar | ✅ 24h Scrubber Bar | Thống kê % trong ngày, chạm/kéo tua và phát lại |
| **Hẹn giờ ngủ (Sleep Timer)** | ✅ Presets & Custom | ✅ Presets & Custom | Tích hợp trong Cài đặt và màn hình Now Playing |
| **Quản lý danh sách phát (Playlists)** | ✅ Room Database | ✅ Local JSON Storage | Tạo, thêm bài, xóa playlist ngoại tuyến |
| **Điều khiển ngoài màn hình khóa** | ✅ MediaSessionService | ✅ MPNowPlayingInfoCenter | Đầy đủ nút Play/Pause, Next, Prev, Thanh Seek |
| **Cập nhật OTA không qua App Store** | ✅ GitHub APK Auto-Update | ✅ Apple Enterprise OTA | Cài đặt trực tiếp qua Safari / Quét mã QR |

---

## 🛠️ 3. Ngăn Xếp Công Nghệ (Technology Stack)

### 🤖 Android Native Stack
- **Ngôn ngữ**: Kotlin 1.9+ (Coroutines, StateFlow, Flow).
- **Giao diện**: Jetpack Compose, Material 3, Animation Specs (Spring physics), Accompanist.
- **Audio Pipeline**: AndroidX Media3 1.2.1+ (`media3-exoplayer`, `media3-session`, `media3-ui`).
- **Lưu trữ & Dữ liệu**: Room Database, Jetpack DataStore Preferences, MediaStore ContentResolver.
- **Dependency Injection**: Hilt (Dagger-Hilt).
- **Tải ảnh & Caching**: Coil Compose, BitmapPool.

### 🍏 iOS Native Stack
- **Ngôn ngữ**: Swift 5.9+ (Concurrency `async/await`, `@MainActor`, Combine).
- **Giao diện**: SwiftUI (iOS 16.0+), UltraThin Material, Dynamic Spring Animations.
- **Audio Pipeline**: AVFoundation (`AVQueuePlayer`, `AVPlayerItem`), Audio Session (`AVAudioSessionCategoryPlayback`).
- **Now Playing & Lockscreen**: `MediaPlayer` framework (`MPNowPlayingInfoCenter`, `MPRemoteCommandCenter`).
- **Phân phối ứng dụng**: VNPT Enterprise In-House Signing (`com.dhtthni.dhxe`), Apple OTA Distribution (`itms-services://`).

---

## 🚀 4. Hướng Dẫn Biên Dịch & Phát Triển (Build & Run)

### Yêu cầu môi trường
- **Hệ điều hành**: Windows 11 / macOS Sonoma.
- **Android**: Android Studio Hedgehog+, JDK 17, Android SDK API 34.
- **iOS**: macOS với Xcode 15+, Swift 5.9+ (Hỗ trợ build từ xa qua SSH tới máy Mac M1 Pro).

### Biên dịch Android
```bash
cd android
./gradlew assembleDebug
```
Tệp APK đầu ra: `android/app/build/outputs/apk/debug/app-debug.apk`.

### Biên dịch iOS (Trực tiếp trên Mac hoặc từ xa)
Mã nguồn iOS hỗ trợ kịch bản biên dịch không phụ thuộc Xcode IDE thông qua `build_mac.sh`:
```bash
cd ios
bash build_mac.sh
```
Tệp IPA đầu ra đã ký số Enterprise VNPT: `ios/build/ipa/MusicPlayer.ipa`.

---

## 📦 5. Tự Động Hóa Phát Hành (Automated CI/CD Deployment)

Dự án tích hợp sẵn kỹ năng phát hành tự động thông qua PowerShell script tại `.agents/skills/deploy-app/scripts/deploy.ps1`.

### Phát hành Android (Bump patch / minor & đẩy GitHub Releases)
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -Platform android -BumpType patch -ReleaseNotes "Cập nhật tính năng mới"
```

### Phát hành iOS Enterprise In-House (Đồng bộ code sang Mac M1 Pro, Ký số VNPT, Sinh OTA & QR Code)
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -Platform ios -CustomVersion "1.0.4" -ReleaseNotes "Bổ sung Sleep Timer trong Cài đặt và cân đối thanh Menu Bar"
```

Sau khi chạy xong, kịch bản sẽ:
1. Đóng gói mã nguồn và đồng bộ sang máy Mac M1 Pro qua mạng Tailscale an toàn.
2. Nạp chứng chỉ VNPT Enterprise `.p12` và provisioning profile vào macOS Keychain.
3. Biên dịch binary Swift arm64, nạp Asset Catalog `Assets.car`, biên dịch `LaunchScreen.storyboard`.
4. Ký số `codesign` với Entitlements chuẩn doanh nghiệp.
5. Đóng gói `MusicPlayer.ipa` và kéo về máy host.
6. Sinh tệp `manifest.plist` cho Apple OTA.
7. Tạo ảnh QR Code độ phân giải cao cho liên kết `itms-services://`.
8. Tạo Git tag và phát hành GitHub Release.
9. Gửi thông báo hoàn tất kèm ảnh QR Code tới kênh Telegram HUB-LOG.

---

## 📲 6. Cài Đặt Bản Phát Hành Mới Nhất

### iOS (Enterprise In-House v1.0.4)
1. **Quét mã QR**: Mở ứng dụng Camera trên iPhone / iPad và quét mã dưới đây để cài đặt:
   
   ![QR Code iOS Release](https://raw.githubusercontent.com/khuongnv/android_music_player/main/releases/MusicPlayer-ios-1.0.4-qr.png)

2. **Cài đặt qua Safari**: Nhấp vào liên kết [👉 Cài đặt Music iOS qua Apple OTA](itms-services://?action=download-manifest&url=https://github.com/khuongnv/android_music_player/releases/download/ios-v1.0.4/manifest.plist).
3. **Tin cậy chứng chỉ doanh nghiệp** (Chỉ cần làm lần đầu):
   - Vào **Cài đặt (Settings)** > **Cài đặt chung (General)** > **Quản lý VPN & Thiết bị (VPN & Device Management)**.
   - Chọn mục **VIETNAM POSTS AND TELECOMMUNICATIONS GROUP** > Bấm **Tin cậy (Trust)**.

### Android
Tải tệp APK phát hành mới nhất tại mục [GitHub Releases](https://github.com/khuongnv/android_music_player/releases).

---

## 📝 7. Quy Chuẩn Ghi Nhật Ký Truy Vết (Audit Logging)

Tuân thủ nghiêm ngặt theo tôn chỉ tại [AGENTS.md](./AGENTS.md):
- Mọi thao tác bổ sung tính năng, tinh chỉnh kiến trúc hoặc sửa lỗi đều phải được ghi lại thành tài liệu Markdown chi tiết trong thư mục `Docs/logs/`.
- Tra cứu danh mục lịch sử thực thi tại: [`Docs/logs/README.md`](./Docs/logs/README.md).

---

*Phát triển với tinh thần kiến trúc sạch, trải nghiệm người dùng tinh tế và hiệu năng thuần bản địa.*
