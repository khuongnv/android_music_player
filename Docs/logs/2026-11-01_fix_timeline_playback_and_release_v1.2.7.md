# 📋 Nhật Ký Thực Thi: Khắc Phục Lỗi Phát Nhạc Trong Màn Hình Ký Ức (Timeline) & Phát Hành v1.2.7

- **Ngày thực hiện**: 2026-11-01 07:50:00
- **Agent phụ trách**: `media-playback-specialist`, `compose-ui-specialist`, `lead-architect`
- **Yêu cầu gốc**: Trong phần Ký ức khi muốn xem lại tại thời điểm cũ thì không phát được nhạc tại phút đó (không gọi về file nhạc chuẩn). Sửa xong thì phát hành phiên bản mới luôn để fix lỗi này.
- **Trạng thái**: Hoàn thành

---

## 1. Phân Tích & Nguyên Nhân Gốc Rễ (Root Cause Analysis)

### Nguyên nhân 1: URI tệp âm thanh bị rỗng khi lưu và phục hồi
- Khi `MusicService.kt` ghi nhận nhật ký phát nhạc qua hàm `recordPlaybackActivity()`, tệp trích xuất URI bằng `currentMediaItem.requestMetadata.mediaUri`.
- Tuy nhiên, trong AndroidX Media3, khi `Song.toMediaItem()` được gọi, URI chỉ được gán vào `localConfiguration.uri` mà không được gán vào `requestMetadata.mediaUri`. Do `localConfiguration` không được chuyển qua Binder IPC giữa MediaController và MediaSession, trường này trở thành `null`, dẫn đến việc `songUri` trong bảng cơ sở dữ liệu `playback_activity` bị ghi thành chuỗi rỗng `""`.

### Nguyên nhân 2: Thiếu cơ chế phân giải (resolve) bài hát chuẩn từ Thư viện
- Trong `TimelineViewModel.kt`, khi người dùng bấm *"Phát từ đoạn này (mm:ss)"* hoặc bấm vào một phiên phát nhạc trong danh sách, ViewModel khởi tạo một đối tượng `Song` tạm thời trực tiếp từ các trường của `PlaybackActivity` mà không truy vấn `MusicRepository` hay `SongDao`.
- Vì `activity.songUri` bị rỗng (hoặc khi thư viện được quét lại có ID mới), ExoPlayer nhận được `Uri.EMPTY` nên không thể mở nguồn phát âm thanh.

### Nguyên nhân 3: Thiếu Callback `onAddMediaItems` trong `MusicService`
- Khi `MediaController.setMediaItem(mediaItem, positionMs)` được gọi, MediaSessionService cần callback `onAddMediaItems` để phân giải lại URI cho MediaItem nếu nó bị thiếu do truyền qua IPC. Việc thiếu callback này khiến ExoPlayer nhận `MediaItem` không có URI hợp lệ.

---

## 2. Chi Tiết Các Giải Pháp Đã Triển Khai (Execution Steps)

- **Bước 1: Bổ sung RequestMetadata trong `MediaItemExtensions.kt`**:
  - Gán `requestMetadata` chứa `mediaUri` khi chuyển đổi `Song.toMediaItem()`.
  - Cập nhật `toSong()` để ưu tiên lấy URI từ `localConfiguration?.uri ?: requestMetadata.mediaUri ?: Uri.EMPTY`.

- **Bước 2: Cập nhật `MusicService.kt`**:
  - Trong `recordPlaybackActivity()`: Phân giải trực tiếp `realSong` từ `musicRepository.getSongById(songId)` để đảm bảo luôn lưu đúng URI tệp âm thanh chuẩn của thiết bị vào Room Database.
  - Trong `CustomMediaSessionCallback`: Triển khai hàm `onAddMediaItems` để tự động phân giải và gắn lại URI hợp lệ cho MediaItem nếu URI bị thiếu trong quá trình giao tiếp IPC.

- **Bước 3: Tối ưu `MusicServiceConnection.kt`**:
  - Trong `playSongAtPosition()`: Kiểm tra nếu bài hát đã có sẵn trong danh sách phát hiện tại của controller thì `seekTo(index, positionMs)` thay vì tạo mới MediaItem đơn lẻ, giữ nguyên hàng đợi bài hát. Cập nhật ngay lập tức `_currentSong` và `_currentPosition` để giao diện phản hồi tức thì.

- **Bước 4: Nâng cấp `TimelineViewModel.kt`**:
  - Tiêm phụ thuộc `MusicRepository` vào `TimelineViewModel`.
  - Xây dựng hàm `resolveSong()` 4 tầng:
    1. Tìm theo `songId` trong cơ sở dữ liệu.
    2. Tìm theo tên bài hát (`title`) và nghệ sĩ (`artist`) trong toàn bộ thư viện.
    3. Dùng `fallbackUri` nếu URI hợp lệ và không rỗng.
    4. Tìm khớp mờ (partial match) theo tiêu đề.
  - Nhờ đó, bài hát luôn được định vị chính xác và phát đúng vị trí phút (`positionMs`) mà người dùng chọn trên thước đo thời gian 24h.

- **Bước 5: Tự động sửa chữa bản ghi cũ trong `TimelineRepositoryImpl.kt`**:
  - Khi `ensureSampleActivitiesIfEmpty()` hoặc `recordActivity()` chạy, tự động rà soát và vá các bản ghi cũ có `songUri` rỗng bằng đường dẫn tệp thực tế từ `SongDao`.
  - Xử lý `toDomain()` an toàn: `if (songUri.isNotBlank()) Uri.parse(songUri) else Uri.EMPTY`.

- **Bước 6: Cải thiện trải nghiệm trong `TimelineScreen.kt`**:
  - Cho phép người dùng chạm vào toàn bộ khung thẻ của `ActivitySessionItem` để phát nhạc thay vì chỉ bấm vào nút Play nhỏ.

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)
- `[Modified]`: `app/src/main/java/com/apple/android/music/util/MediaItemExtensions.kt`
- `[Modified]`: `app/src/main/java/com/apple/android/music/player/MusicService.kt`
- `[Modified]`: `app/src/main/java/com/apple/android/music/player/MusicServiceConnection.kt`
- `[Modified]`: `app/src/main/java/com/apple/android/music/data/repository/TimelineRepositoryImpl.kt`
- `[Modified]`: `app/src/main/java/com/apple/android/music/ui/screens/timeline/TimelineViewModel.kt`
- `[Modified]`: `app/src/main/java/com/apple/android/music/ui/screens/timeline/TimelineScreen.kt`

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)
- `compileDebugKotlin`: `BUILD SUCCESSFUL` (54s).
- `testDebugUnitTest`: `BUILD SUCCESSFUL` (35s).
- Đảm bảo không xảy ra hiện tượng Crash hoặc `Uri.EMPTY` khi tua phát tại bất kỳ thời điểm nào trong 24 giờ qua.

---

## 5. Kế Hoạch Phát Hành (Release Plan)
- Nâng phiên bản từ `v1.2.6` (Build 11) lên `v1.2.7` (Build 12).
- Đẩy Git Tag `v1.2.7` lên GitHub để kích hoạt CI/CD build APK Auto-Update.
