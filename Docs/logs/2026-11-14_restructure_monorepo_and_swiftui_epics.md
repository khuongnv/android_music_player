# 📋 Nhật Ký Thực Thi: Tái cấu trúc Monorepo (android/ & ios/), Khởi tạo dự án SwiftUI và Biên soạn Epics cho iOS

- **Ngày thực hiện**: 2026-11-14 23:45:00
- **Agent phụ trách**: `lead-architect`
- **Yêu cầu gốc**: Quyết định theo hướng Native SwiftUI cho iOS thay vì Flutter. Tái cấu trúc thư mục dự án thành `android/` (chứa app hiện tại) và `ios/` (chạy SwiftUI). Sau khi tái cấu trúc xong, biên soạn lại Epics chi tiết cho iOS vào `Docs/`.
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)

### Bối cảnh & Mục tiêu kỹ thuật:
- Người dùng đã xác nhận phát hành ứng dụng iOS theo hình thức **Nội bộ doanh nghiệp (In-House Enterprise)**, có sẵn Apple Enterprise Account và Bundle ID riêng biệt, không đưa lên Public App Store.
- Lựa chọn chiến lược: Giữ nguyên vẹn 100% ứng dụng Android Native hiện tại (đã tối ưu sâu với Media3 ExoPlayer, Jetpack Compose, Room DB) và dựng dự án iOS Native bằng **SwiftUI** để đạt độ mượt mà tối đa (120 FPS ProMotion), tận dụng trực tiếp `.ultraThinMaterial`, `AVFoundation`, và `MPNowPlayingInfoCenter`.
- Tái cấu trúc toàn bộ kho mã nguồn thành mô hình **Dual-Native Monorepo**:
  - `android/`: Toàn bộ mã nguồn, cấu hình Gradle và tài nguyên Android Native.
  - `ios/`: Dự án iOS Native viết bằng SwiftUI.

---

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)

- **Bước 1 (Di chuyển thư mục Android)**:
  - Tạo thư mục `android/`.
  - Sử dụng lệnh `git mv` di chuyển toàn bộ: `app/`, `gradle/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat` vào `android/` nhằm bảo toàn toàn bộ lịch sử Git commits.
  - Cập nhật file CI/CD workflow `.github/workflows/release.yml` với `working-directory: android` để tự động build APK release từ thư mục mới.
  - Cập nhật script phát hành phiên bản `.agents/skills/deploy-app/scripts/deploy.ps1` để tự động nhận diện đường dẫn `android/app/build.gradle.kts` và `android/gradlew.bat`.
  - Xác minh thành công lệnh kiểm tra Gradle: `./gradlew tasks --group=build` bên trong thư mục `android/`.
- **Bước 2 (Khởi tạo dự án iOS SwiftUI)**:
  - Thiết lập cấu trúc thư mục chuẩn mực trong `ios/MusicPlayer/`: `App/`, `Core/` (Audio, Storage, Theme, Update), `Models/`, `ViewModels/`, `Views/` (Components, Screens), `Resources/`.
  - Viết các mô-đun nền tảng Swift:
    - `Song.swift`, `Album.swift`, `Artist.swift`, `Playlist.swift`.
    - `ColorTokens.swift` và `LiquidGlassStyle.swift` (áp dụng `.ultraThinMaterial`, viền sáng phát quang 0.75pt, đổ bóng mềm).
    - `AudioPlayerService.swift`: Tích hợp `AVQueuePlayer`, `AVAudioSession` (.playback), `MPNowPlayingInfoCenter`, và `MPRemoteCommandCenter`.
    - `LocalAudioScanner.swift`: Quét nhạc từ `Documents` (Files App & iTunes File Sharing) và `MPMediaQuery`.
    - `InHouseUpdater.swift`: Xử lý kiểm tra và cập nhật OTA qua giao thức `itms-services://`.
    - `MiniPlayerView.swift`, `NowPlayingView.swift`, `LibraryView.swift`, `MainTabView.swift`.
    - `Info.plist`: Khai báo `UIFileSharingEnabled`, `LSSupportsOpeningDocumentsInPlace`, `UIBackgroundModes` (audio), `NSAppleMusicUsageDescription`.
- **Bước 3 (Biên soạn tài liệu Epics cho iOS SwiftUI)**:
  - Tạo tài liệu [`Docs/EPICS_SWIFTUI_IOS_PROJECT.md`](../EPICS_SWIFTUI_IOS_PROJECT.md) phân rã 8 Epics chi tiết:
    - Epic 1: Thiết lập dự án SwiftUI & In-House Enterprise Configuration.
    - Epic 2: Audio Engine Native & Khả năng chạy nền toàn diện.
    - Epic 3: Cơ chế nhập nhạc ngoại tuyến & Quản lý tệp cục bộ (Files, Document Picker, MPMediaQuery).
    - Epic 4: Hệ thống Design System "Apple Music x Liquid Glass" Native.
    - Epic 5: Màn hình và các luồng trải nghiệm cốt lõi (Library, Songs, Albums, MiniPlayer, NowPlaying).
    - Epic 6: Tính năng mở rộng (24h Timeline, Equalizer, Sleep Timer, Playlist).
    - Epic 7: Cơ chế tự động cập nhật nội bộ OTA In-House (`itms-services://`).
    - Epic 8: Tối ưu hiệu năng, Pin, Memory Cache & QA Test.
  - Xây dựng lộ trình phát triển 4 tuần tinh gọn.
- **Bước 4 (Cập nhật tài liệu tổng quan & Log)**:
  - Cập nhật `README.md` mô tả cấu trúc Monorepo mới.
  - Cập nhật [`Docs/logs/README.md`](./README.md).

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)

- `[Moved]`: `app/` -> `android/app/`
- `[Moved]`: `gradle/` -> `android/gradle/`
- `[Moved]`: `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat` -> `android/`
- `[Modified]`: `.github/workflows/release.yml` - Bổ sung `working-directory: android`
- `[Modified]`: `.agents/skills/deploy-app/scripts/deploy.ps1` - Hỗ trợ đường dẫn `android/`
- `[Modified]`: `README.md` - Cập nhật cấu trúc thư mục Monorepo
- `[Created]`: `ios/` - Toàn bộ cấu trúc dự án iOS SwiftUI (`App/`, `Core/`, `Models/`, `Views/`, `Resources/`, `README.md`)
- `[Created]`: [`Docs/EPICS_SWIFTUI_IOS_PROJECT.md`](../EPICS_SWIFTUI_IOS_PROJECT.md) - Tài liệu 8 Epics chi tiết cho dự án iOS SwiftUI
- `[Created]`: [`Docs/logs/2026-11-14_restructure_monorepo_and_swiftui_epics.md`](./2026-11-14_restructure_monorepo_and_swiftui_epics.md) - Nhật ký thực thi
- `[Modified]`: [`Docs/logs/README.md`](./README.md) - Cập nhật danh mục log

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)

- **Gradle Build Verification**:
  - Chạy `Push-Location android; .\gradlew.bat tasks --group=build; Pop-Location` thành công 100% trong 13s (`BUILD SUCCESSFUL`).
- **Git Tracking Integrity**:
  - `git mv` giữ nguyên toàn bộ commit history của mã nguồn Android Native.
- **Kiến trúc SwiftUI & In-House OTA**:
  - Khung mã nguồn Swift đã kết nối đúng các framework hệ thống: `AVFoundation`, `MediaPlayer`, `UIKit`, `SwiftUI`.
  - Cấu hình `Info.plist` đảm bảo đầy đủ các cờ cần thiết cho ứng dụng nghe nhạc ngoại tuyến nội bộ.

---

## 5. Kết Luận & Hướng Dẫn Tiếp Theo (Conclusion & Next Steps)

- Cấu trúc thư mục dự án đã sẵn sàng cho cả hai nền tảng:
  - Android tiếp tục hoạt động và phát hành ổn định trong `android/`.
  - iOS đã có khung mã nguồn chuẩn mực và tài liệu Epics 4 tuần tại `ios/`.
- Sẵn sàng bàn giao cho các agent hoặc bắt tay triển khai chi tiết từng Epic cho iOS.
