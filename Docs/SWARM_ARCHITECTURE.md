# 🎼 Kiến Trúc Swarm Agent - Android Offline Music Player

Tài liệu này mô tả chi tiết mô hình **Swarm Agent** (Đội ngũ Agent đa tác vụ thông minh) được thiết lập riêng cho dự án phát triển ứng dụng **Android Offline Music Player**.

---

## 1. Mục Tiêu Thiết Kế Swarm

Một ứng dụng Music Player offline đòi hỏi sự kết hợp phức tạp giữa:
- **Audio Engine cấp thấp**: Quản lý bộ đệm, giải mã âm thanh, Audio Focus, Service chạy nền không bị OS tắt.
- **Giao diện người dùng cao cấp**: Cử chỉ vuốt, animation mượt mà, theme sang trọng lấy cảm hứng từ Apple Music.
- **Hệ thống dữ liệu cục bộ**: MediaStore scanning nhanh chóng, bảo mật quyền bộ nhớ (Scoped Storage), Room DB lưu trữ playlist.
- **Độ tin cậy & tối ưu pin**: Không rò rỉ RAM (Bitmap OOM), không hao pin khi phát nhạc nền trong trạng thái Doze Mode.

Do đó, kiến trúc Swarm Agent chia nhỏ dự án thành **5 thực thể chuyên biệt**:

```text
                                  ┌───────────────────────────────┐
                                  │       lead-architect          │
                                  │  (Orchestrator & Architecture)│
                                  └──────────────┬────────────────┘
                                                 │
                 ┌───────────────────────────────┼───────────────────────────────┐
                 │                               │                               │
  ┌──────────────▼──────────────┐ ┌──────────────▼──────────────┐ ┌──────────────▼──────────────┐
  │  media-playback-specialist  │ │    compose-ui-specialist    │ │   storage-data-specialist   │
  │    (Media3 ExoPlayer &      │ │ (Jetpack Compose & Material3│ │  (MediaStore, Room Database │
  │    Background Service)      │ │   Apple Music Aesthetic)    │ │   & Jetpack DataStore)      │
  └──────────────┬──────────────┘ └──────────────┬──────────────┘ └──────────────┬──────────────┘
                 │                               │                               │
                 └───────────────────────────────┼───────────────────────────────┘
                                                 │
                                  ┌──────────────▼──────────────┐
                                  │      qa-perf-specialist     │
                                  │ (Testing, Battery & Memory) │
                                  └─────────────────────────────┘
```

---

## 2. Bảng Phân Công & Danh Mục Phụ Trách

| Agent | Module / Thư Mục Phụ Trách | Trách Nhiệm Cốt Lõi |
| :--- | :--- | :--- |
| **`lead-architect`** | `domain/`, `di/`, `ui/navigation/`, `AGENTS.md` | - Điều phối toàn bộ swarm.<br>- Giữ vững quy tắc Clean Architecture & MVVM.<br>- Định hình cấu trúc Interface, Use Case và Hilt Modules. |
| **`media-playback-specialist`** | `player/`, `util/MediaItemExtensions.kt` | - `MusicService`: Vận hành AndroidX Media3 (`ExoPlayer`, `MediaSessionService`).<br>- `MusicServiceConnection`: Cầu nối giữa UI/ViewModel với Player Service.<br>- Xử lý Audio Focus (cuộc gọi đến, rút tai nghe).<br>- Tính năng: Sleep Timer, Shuffle, Repeat, Equalizer. |
| **`compose-ui-specialist`** | `ui/screens/`, `ui/components/`, `ui/theme/` | - Thiết kế giao diện Compose theo chuẩn Apple Music.<br>- `MiniPlayer`: Thanh điều khiển nổi với cử chỉ vuốt và chuyển cảnh sang `NowPlayingScreen`.<br>- Tối ưu tốc độ khung hình (60/120fps), chống recomposition lãng phí.<br>- Tải ảnh bìa album tối ưu qua Coil. |
| **`storage-data-specialist`** | `data/`, `util/MediaScannerUtil.kt`, `util/AudioArtworkFetcher.kt` | - `MediaStoreDataSource` & `MusicScanner`: Quét và trích xuất nhạc nội bộ.<br>- Xử lý quyền Scoped Storage (`READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`).<br>- `MusicDatabase` (Room): Quản lý bảng Playlist, Song, Album, Artist.<br>- `Jetpack DataStore`: Ghi nhớ vị trí phát cuối, chế độ phát và cài đặt. |
| **`qa-perf-specialist`** | `test/`, `androidTest/`, ProGuard, Gradle config | - Rà soát rò rỉ bộ nhớ (Bitmap leaks, unreleased listeners).<br>- Tối ưu pin khi chạy ngầm (Foreground Service, Wakelock).<br>- Viết Unit Test và Compose UI Test.<br>- Đảm bảo Gradle build sạch sẽ, không có warning nghiêm trọng. |

---

## 3. Cách Sử Dụng Swarm Trong Thực Tế

Khi phát triển thêm tính năng hoặc sửa lỗi, hệ thống sẽ tự động hoặc cho phép gọi trực tiếp các agent chuyên trách:

- **Khi cần tạo tính năng mới (ví dụ: Equalizer, Lời bài hát Karaoke, Hẹn giờ thông minh)**:
  `lead-architect` sẽ lập kế hoạch, sau đó phân việc cho `media-playback-specialist` (xử lý audio engine) và `compose-ui-specialist` (dựng giao diện), cùng `storage-data-specialist` (lưu cài đặt).
- **Khi cần fix lỗi giật lag, đơ khi cuộn danh sách hàng ngàn bài hát**:
  `storage-data-specialist` tối ưu phân trang Cursor/Room + `compose-ui-specialist` tối ưu `LazyColumn` key.
- **Khi cần kiểm tra ứng dụng trước khi build release**:
  `qa-perf-specialist` thực hiện kiểm tra pin, memory leak, lint và test coverage.

---

## 4. Quy Ước Bắt Buộc: Ghi Nhật Ký Truy Vết (Audit Logs)

Nhằm phục vụ truy vết lịch sử phát triển, giải trình kỹ thuật và hỗ trợ bảo trì dài hạn, toàn bộ các bước thực thi của các Swarm Agent **bắt buộc phải được ghi chép vào thư mục `Docs/logs/` dưới định dạng Markdown (.md)**:

1. **Vị trí lưu trữ**: `Docs/logs/YYYY-MM-DD_<ten_tac_vu>.md`
2. **Chỉ mục tổng hợp**: `Docs/logs/README.md`
3. **Nội dung bắt buộc trong mỗi log**:
   - Thời gian thực hiện & Danh sách Agent tham gia.
   - Yêu cầu và mục tiêu kỹ thuật.
   - Chi tiết từng bước thực thi (kèm giải thích kỹ thuật).
   - Danh sách các tệp tin đã tạo mới hoặc thay đổi.
   - Kết quả xác minh, kiểm thử (Build, Tests, Memory, Battery).
4. **Quy tắc nghiệm thu**: Mọi task chưa có file log tương ứng trong `Docs/logs/` sẽ bị xem là chưa hoàn thành.

---
*Tài liệu được khởi tạo và quản lý bởi Antigravity Swarm System.*
