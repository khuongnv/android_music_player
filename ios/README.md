# 🍏 iOS Offline Music Player (SwiftUI)

Ứng dụng nghe nhạc ngoại tuyến chất lượng cao dành cho iOS, được xây dựng thuần túy bằng **Swift** và **SwiftUI**, lấy cảm hứng từ ngôn ngữ thiết kế **Apple Music x Liquid Glass**.

## 📱 Điểm Nổi Bật & Tính Năng
- **Kiến trúc Native thuần túy**: Tận dụng tối đa hiệu năng của `AVFoundation` và `MediaPlayer`.
- **Giao diện Liquid Glass & Apple Music**: Thiết kế kính mờ tự nhiên với `.ultraThinMaterial`, màu nhấn `#FA2D55` (Apple Red), Dark AMOLED mode 100%.
- **Chạy nền chuyên nghiệp (Background Audio)**: Hỗ trợ đầy đủ `MPNowPlayingInfoCenter` (Dynamic Island, Lock Screen, Apple Watch) và `MPRemoteCommandCenter`.
- **Nhập nhạc đa dạng trên iOS**:
  - Hỗ trợ **iTunes / Finder File Sharing** và ứng dụng **Files (Tệp)** (`UIFileSharingEnabled`).
  - Hỗ trợ quét nhạc sẵn có từ thư viện máy thông qua `MPMediaQuery`.
- **Phát hành nội bộ Enterprise**: Hỗ trợ cơ chế cập nhật không dây OTA (`itms-services://`).

## 🛠 Cấu Trúc Dự Án
```text
ios/MusicPlayer/
├── App/             # Entry point & App Lifecycle
├── Core/
│   ├── Audio/       # AudioPlayerService (AVQueuePlayer, AudioSession)
│   ├── Storage/     # Quét tệp từ Documents & MediaLibrary
│   ├── Theme/       # ColorTokens, LiquidGlassStyle, Animations
│   └── Update/      # In-House OTA updater
├── Models/          # Song, Album, Artist, Playlist
├── ViewModels/      # State Management & UseCases
├── Views/
│   ├── Components/  # MiniPlayer, SongItem, LiquidGlassCard
│   └── Screens/     # LibraryView, NowPlayingView, TimelineView, SettingsView
└── Resources/       # Info.plist & Assets.xcassets
```

## 🚀 Hướng Dẫn Mở Dự Án & Ký Số Doanh Nghiệp (Enterprise In-House)

1. **Mở dự án trong Xcode**:
   - Mở thư mục `ios` bằng **Xcode 15+** trên macOS.
2. **Cấu hình Signing & Capabilities**:
   - **Bundle Identifier**: Đặt chính xác là `com.dhtthni.dhxe`
   - **Team**: Chọn Team có ID `JPMH34L85K` (`VIETNAM POSTS AND TELECOMMUNICATIONS GROUP - HO CHI MINH CITY TELECOM BRANCH`).
   - **Certificate**: Cài đặt file chứng chỉ [`docs/dev-ios/VNPT_Enterprise_iOS Distribution_18082029.p12`](../docs/dev-ios/VNPT_Enterprise_iOS%20Distribution_18082029.p12) vào macOS Keychain (Hạn dùng: 18/08/2029).
   - **Provisioning Profile**: Chọn cấu hình Manual Signing và trỏ tới file profile [`docs/dev-ios/Prov_HNI_comdhtthnidhxe.mobileprovision`](../docs/dev-ios/Prov_HNI_comdhtthnidhxe.mobileprovision) (Name: `Prov_HNI_com.dhtthni.dhxe`, UUID: `309d9c0d-3f81-40ab-919f-e9c4a6405773`).
3. **Chạy ứng dụng**:
   - Chọn thiết bị iPhone thật hoặc Simulator (iOS 15.0 trở lên) và nhấn **Run** (Cmd + R).
4. **Đóng gói phát hành In-House OTA**:
   - Archive và Export ứng dụng chọn hình thức **Enterprise Distribution**.
   - Sử dụng mẫu [`docs/dev-ios/manifest.plist.example`](../docs/dev-ios/manifest.plist.example) để cấu hình đường dẫn cài đặt không dây OTA qua `itms-services://`.

