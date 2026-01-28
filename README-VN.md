# Android Offline Music Player (Trình phát nhạc Offline)

Một ứng dụng phát nhạc offline cao cấp cho Android, được lấy cảm hứng mạnh mẽ từ phong cách thiết kế tinh tế và hiện đại của Apple Music. Ứng dụng này được thiết kế để mang lại trải nghiệm nghe nhạc mượt mà cho các tệp nhạc được lưu trữ cục bộ trên thiết bị của bạn, tập trung vào tính thẩm mỹ và các tương tác chuyển động tinh tế.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/language-Kotlin-purple.svg)

## 📱 Tổng quan dự án

**Android Offline Music Player** là một trình phát nhạc thuần offline. Ứng dụng không hỗ trợ các dịch vụ trực tuyến hoặc phát trực tuyến (streaming), đảm bảo quyền riêng tư tuyệt đối 100% và quyền kiểm soát hoàn toàn đối với thư viện nhạc cá nhân của bạn.

- **Mục tiêu**: Xây dựng một trình phát nhạc offline mạnh mẽ và đẹp mắt.
- **Triết lý thiết kế**: Tối giản, hiện đại, các góc bo tròn, ảnh bìa album lớn và hiệu ứng chuyển cảnh mượt mà.
- **Hệ điều hành hỗ trợ**: Android 8.0+ (API 26)

## ✨ Các tính năng chính

- **Đồng bộ thư viện nội bộ**: Tự động quét và sắp xếp nhạc từ bộ nhớ thiết bị của bạn.
- **Giao diện lấy cảm hứng từ Apple Music**: Trải nghiệm cao cấp với Jetpack Compose.
- **Phân loại linh hoạt**: Xem nhạc theo **Bài hát**, **Album**, **Nghệ sĩ** và **Danh sách phát**.
- **Điều khiển phát nhạc nâng cao**:
    - Phát nhạc nền với `MediaSessionService`.
    - Điều khiển thông qua thông báo (Notification) và màn hình khóa.
    - Chế độ Shuffle (Phát ngẫu nhiên) và Repeat (Lặp lại: Tắt / Một bài / Tất cả).
    - Tính năng tua (seek) mượt mà ngay cả khi đang tạm dừng.
- **Mini Player**: Trình phát nhỏ gọn luôn hiển thị phía trên thanh điều hướng để truy cập nhanh.
- **Lưu trữ trạng thái**: Ghi nhớ bài hát cuối cùng, vị trí phát và các chế độ phát bằng Jetpack DataStore.
- **Chế độ Sáng / Tối**: Hỗ trợ đầy đủ việc chuyển đổi giao diện theo hệ thống.
- **Quản lý Audio Focus**: Tự động tạm dừng khi có cuộc gọi đến và tiếp tục phát sau đó.

## 🛠 Công nghệ sử dụng

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Media Engine**: [AndroidX Media3 (ExoPlayer)](https://developer.android.com/guide/topics/media/media3)
- **Ngôn ngữ lập trình**: [Kotlin](https://kotlinlang.org/)
- **Lưu trữ dữ liệu**: 
    - [Room Database](https://developer.android.com/training/data-storage/room) (cho Danh sách phát)
    - [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (cho Trạng thái trình phát)
- **Core API**: MediaStore API (để quét tệp nội bộ)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)

## 🏗 Kiến trúc

Dự án tuân thủ các nguyên tắc của **Clean Architecture** kết hợp với mô hình **MVVM**:

- **UI Layer**: Các màn hình Compose và ViewModel.
- **Domain Layer**: Model, Interface Repository và Use Case.
- **Data Layer**: Triển khai Repository, xử lý MediaStore API và Room Database.

## 🚀 Bắt đầu

### Yêu cầu tiên quyết
- **Android Studio**: Hedgehog (2023.1.1) hoặc mới hơn.
- **JDK**: 17 hoặc 21.
- **Android SDK**: API level 34.

### Cài đặt
1. Clone repository hoặc tải mã nguồn về.
2. Mở dự án trong Android Studio.
3. Đồng bộ Gradle và đợi tải các thư viện cần thiết.
4. Kết nối thiết bị Android hoặc khởi động máy ảo (API 26+).
5. Nhấn **Run** (▷).

**Lưu ý**: Bạn cần cấp quyền **Nhạc và Âm thanh** trong lần đầu khởi chạy để ứng dụng có thể hoạt động.

## 📖 Hướng dẫn sử dụng

1. **Chuẩn bị nhạc**: Đảm bảo bạn có các tệp âm thanh (`.mp3`, `.m4a`, v.v.) trong thư mục `Music` hoặc `Download` của thiết bị.
2. **Khám phá thư viện**: Sử dụng tab **Library** để duyệt bộ sưu tập của bạn.
3. **Phát nhạc**: Nhấn vào bất kỳ bài hát nào để bắt đầu phát. Sử dụng **Mini Player** ở dưới cùng để mở rộng màn hình **Now Playing** đầy đủ.
4. **Quản lý danh sách phát**: Tạo danh sách phát tùy chỉnh trong tab Library để nhóm các bài hát yêu thích của bạn.

---

*Được phát triển với ❤️ truyền cảm hứng từ những thiết kế vượt thời gian.*
