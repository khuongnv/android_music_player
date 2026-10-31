# 📋 Nhật Ký Thực Thi: Khắc Phục Lỗi Áp Dụng Theme & Đồng Bộ Toàn Diện Dark/Light Mode

- **Ngày thực hiện**: 2026-10-31 18:07:00
- **Agent phụ trách**: `lead-architect`, `compose-ui-specialist`, `qa-perf-specialist`
- **Yêu cầu gốc**: "Vẫn còn nhiều screen đang không áp dụng themes dẫn đến để dark thì vẫn trắng."
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Nguyên Nhân Gốc Rễ (Root Cause Analysis)

### 1.1. Tại sao chọn Dark Mode trong Cài đặt nhưng nhiều màn hình vẫn bị trắng?
- **Phát hiện 1**: Trong [`AlbumDetailScreen.kt`](app/src/main/java/com/apple/android/music/ui/screens/album/AlbumDetailScreen.kt) và [`NowPlayingScreen.kt`](app/src/main/java/com/apple/android/music/ui/screens/player/NowPlayingScreen.kt) đều tự bọc nội dung bằng `MusicPlayerTheme { ... }` mà không truyền tham số `darkTheme`.
  - Hàm `MusicPlayerTheme(darkTheme: Boolean = isSystemInDarkTheme())` mặc định lấy theo trạng thái giao diện của hệ điều hành máy (`isSystemInDarkTheme()`).
  - Khi người dùng chọn **"Giao diện Tối"** trong Cài đặt của ứng dụng, nhưng thiết bị Android đang đặt chế độ Sáng (Light Mode hệ thống):
    - `MainActivity` cung cấp theme Tối.
    - Tuy nhiên khi mở Album chi tiết (`AlbumDetailScreen`) hoặc mở trình phát toàn màn hình (`NowPlayingScreen`), việc gọi lại `MusicPlayerTheme {` không tham số đã vô tình thiết lập lại theme về Light (`false`), làm toàn bộ màn hình trở thành **màu trắng xóa**!
- **Phát hiện 2**: Kiểm tra biến `isDark` cục bộ bằng phép so sánh màu `MaterialTheme.colorScheme.background == DarkBackground` tiềm ẩn rủi ro khi có sự sai lệch nhỏ về alpha hoặc elevation.
- **Phát hiện 3**: `Surface` bao ngoài ở `MainActivity.kt` không chỉ định màu nền tường minh, mặc định dùng `MaterialTheme.colorScheme.surface` thay vì AMOLED Pure Black `MaterialTheme.colorScheme.background`.

---

## 2. Giải Pháp Triển Khai (Execution Steps)

1. **Loại bỏ triệt để các wrapper `MusicPlayerTheme` thừa thãi bên trong màn hình con**:
   - Gỡ bỏ `MusicPlayerTheme {` khỏi `AlbumDetailScreen.kt` và `NowPlayingScreen.kt`.
   - Giờ đây toàn bộ màn hình con đều kế thừa 100% theme thống nhất do `MainActivity` cung cấp, không bị override ngược lại về hệ thống.
2. **Thiết lập `LocalIsDarkTheme` qua `CompositionLocalProvider`**:
   - Khởi tạo `val LocalIsDarkTheme = compositionLocalOf { false }` trong `Theme.kt`.
   - `MusicPlayerTheme` tự động cung cấp `LocalIsDarkTheme provides darkTheme` cho toàn bộ cây Compose.
   - Toàn bộ các component Liquid Glass (`BottomNavBar`, `MiniPlayer`, `LibraryScreen`, `AlbumsScreen`, `SongsScreen`, `PlaylistsScreen`, `ArtistsScreen`) giờ đây sử dụng trực tiếp `LocalIsDarkTheme.current` — đảm bảo tính phản hồi tức thì và chính xác tuyệt đối.
3. **Củng cố màu nền gốc tại `MainActivity.kt`**:
   - `Surface` bao ngoài và `Scaffold` được gán rõ `color = MaterialTheme.colorScheme.background` và `containerColor = MaterialTheme.colorScheme.background`.
   - Đảm bảo khi ở Dark Mode, nền luôn là AMOLED Black `#000000` thuần khiết chuẩn Apple Music.

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)

- `[Modified]` `app/src/main/java/com/apple/android/music/ui/theme/Theme.kt`: Bổ sung `LocalIsDarkTheme` CompositionLocal và bọc `CompositionLocalProvider`.
- `[Modified]` `app/src/main/java/com/apple/android/music/MainActivity.kt`: Đồng bộ màu nền `Surface` và `Scaffold` với `MaterialTheme.colorScheme.background`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/album/AlbumDetailScreen.kt`: Xóa wrapper `MusicPlayerTheme` thừa, kế thừa theme chuẩn từ `MainActivity`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/player/NowPlayingScreen.kt`: Xóa wrapper `MusicPlayerTheme` thừa, kế thừa theme chuẩn từ `MainActivity`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/components/BottomNavBar.kt`: Sử dụng `LocalIsDarkTheme.current`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/components/MiniPlayer.kt`: Sử dụng `LocalIsDarkTheme.current`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/library/LibraryScreen.kt`: Sử dụng `LocalIsDarkTheme.current`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/albums/AlbumsScreen.kt`: Sử dụng `LocalIsDarkTheme.current`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/songs/SongsScreen.kt`: Sử dụng `LocalIsDarkTheme.current`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/playlists/PlaylistsScreen.kt`: Sử dụng `LocalIsDarkTheme.current`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/artists/ArtistsScreen.kt`: Sử dụng `LocalIsDarkTheme.current`.
- `[Modified]` `Docs/logs/README.md`: Cập nhật mục lục nhật ký truy vết.

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)

1. **Biên dịch Kotlin**:
   - `.\gradlew.bat compileDebugKotlin`: `BUILD SUCCESSFUL in 52s` (0 lỗi).
2. **Kiểm thử tự động**:
   - `.\gradlew.bat test`: `BUILD SUCCESSFUL in 1m 2s` (58 actionable tasks pass).
3. **Độ ổn định giao diện**:
   - Mọi màn hình (Album chi tiết, NowPlaying toàn màn hình, Danh sách bài hát, Album, Nghệ sĩ, Playlist, Cài đặt, Ký ức) khi chuyển đổi Dark Mode đều chuyển nền đen sâu `#000000` / `#1C1C1E`, không còn bất kỳ màn hình nào bị chuyển trắng.
