# 📋 Nhật Ký Thực Thi: Xây dựng Epics chuyển đổi sang Flutter hỗ trợ iOS & Android kèm Khuyến cáo Kiến trúc

- **Ngày thực hiện**: 2026-11-14 23:25:00
- **Agent phụ trách**: `lead-architect`
- **Yêu cầu gốc**: Điều chỉnh dự án thay vì chỉ làm cho Android thì phát hành cho cả iOS, chuyển sang viết bằng Flutter, tạo bộ Epics chuyển đổi vào trong docs và đưa ra các khuyến cáo kiến trúc quan trọng.
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)

### Bối cảnh & Mục tiêu kỹ thuật:
- Dự án hiện tại là ứng dụng **Android Native** (100% Kotlin) phát triển trên nền tảng Jetpack Compose, AndroidX Media3 (ExoPlayer), Room Database và Scoped Storage (MediaStore).
- Yêu cầu chiến lược mới: Chuyển đổi sang **Flutter** để phát hành đồng thời trên cả **iOS** và **Android** từ một cơ sở mã nguồn duy nhất (Single Codebase).
- Yêu cầu chất lượng: Giữ nguyên vẹn thiết kế thẩm mỹ **Apple Music x Liquid Glass**, khả năng phát nhạc offline độ trễ thấp, phát nền ổn định, tiết kiệm pin, và thích ứng với môi trường iOS cực kỳ khắt khe về bảo mật sandbox tệp tin và App Store Review.

### Các tầng kiến trúc liên quan:
1. **Kiến trúc mã nguồn**: Chuyển đổi Clean Architecture Kotlin (Domain - Data - UI) sang Clean Architecture Dart (BLoC/Riverpod + Drift + GetIt).
2. **Audio Pipeline**: Thay thế AndroidX Media3 Service bằng kiến trúc `just_audio` + `audio_service`, kết nối sâu với `AVAudioSession` (iOS) và `MediaSessionCompat` (Android).
3. **Quản lý dữ liệu & Tệp tin**: Thích ứng giữa cơ chế tự động quét MediaStore (Android) và cơ chế Sandbox Documents Sharing / Document Picker (iOS).
4. **UI & Design System**: Chuyển đổi Jetpack Compose Modifier sang Flutter Custom Widgets, tối ưu hiệu năng `BackdropFilter` trên Impeller Engine.

---

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)

- **Bước 1**: Phân tích toàn bộ mã nguồn Android Native hiện tại (`app/src/main/java/com/apple/android/music/`), bao gồm các UseCase, Repository, Player Service, UI Screens và Tiêu chuẩn thiết kế Liquid Glass.
- **Bước 2**: Thiết kế tài liệu Epics chuyển đổi toàn diện [`Docs/EPICS_FLUTTER_MIGRATION.md`](../EPICS_FLUTTER_MIGRATION.md) bao gồm:
  - Bảng ánh xạ công nghệ từ Android Native sang Flutter.
  - Phân rã 8 Epics lớn với các User Stories / Tasks kỹ thuật chi tiết từ Core Architecture, Audio Engine, Storage & iOS Sandbox, UI Design System, Core Screens, Advanced Audio, Performance & QA, đến CI/CD & Store Release.
  - Sơ đồ phụ thuộc giữa các Epics và Lộ trình triển khai (Phased Roadmap 10 tuần).
- **Bước 3**: Nghiên cứu và tổng hợp các khuyến cáo chuyên sâu về kỹ thuật, pháp lý bản quyền Apple Store, hiệu năng và chiến lược tổ chức kho mã nguồn.
- **Bước 4**: Cập nhật tài liệu chỉ mục và nhật ký truy vết hệ thống.

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)

- `[Created]`: [`Docs/EPICS_FLUTTER_MIGRATION.md`](../EPICS_FLUTTER_MIGRATION.md) - Tài liệu phân rã chi tiết 8 Epics chuyển đổi sang Flutter.
- `[Created]`: [`Docs/logs/2026-11-14_flutter_cross_platform_migration_epics.md`](./2026-11-14_flutter_cross_platform_migration_epics.md) - Nhật ký thực thi nhiệm vụ của lead-architect.
- `[Modified]`: [`Docs/logs/README.md`](./README.md) - Cập nhật chỉ mục nhật ký truy vết.

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)

- **Tính khả thi kỹ thuật**:
  - `just_audio` + `audio_service` là giải pháp đã được chứng minh trong hệ sinh thái Flutter cho ứng dụng nghe nhạc nền, hỗ trợ cả Android Auto và Apple CarPlay.
  - Khả năng render kính lỏng (Liquid Glass) bằng `BackdropFilter` và `CustomPainter` trên Impeller Engine đạt 60-120 FPS nếu áp dụng `RepaintBoundary` và giới hạn kích thước phân giải ảnh bìa.
- **Rà soát rủi ro nền tảng iOS**:
  - iOS Sandbox không cho phép truy cập ngẫu nhiên toàn bộ bộ nhớ máy; đã xác định 3 giải pháp nhập nhạc (Files app Sharing, Document Picker, và MPMediaQuery không DRM).
  - Vấn đề pháp lý: Nhận diện rủi ro bản quyền thương hiệu "Apple Music" và Bundle ID `com.apple.*` khi nộp lên Apple App Store.

---

## 5. Kết Luận & Hướng Dẫn Tiếp Theo (Conclusion & Next Steps)

- Đã hoàn thành bộ tài liệu Epics chuyển đổi đa nền tảng và bộ khuyến cáo kiến trúc chuyên sâu.
- **Đề xuất bước tiếp theo**:
  1. Thống nhất chiến lược cấu trúc thư mục (Monorepo hay tạo nhánh/repo mới).
  2. Đổi tên ứng dụng (Rebranding) và Bundle Identifier để tránh rủi ro vi phạm chính sách của Apple.
  3. Khởi tạo dự án Flutter theo cấu trúc Epic 1.
