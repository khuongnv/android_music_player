# 📋 Nhật Ký Thực Thi: Tối ưu lưu trữ file cập nhật vào thư mục Download & Chuẩn hóa Liquid Glass toàn bộ Thư viện

- **Ngày thực hiện**: 2026-10-31 17:42:00
- **Agent phụ trách**: `lead-architect`, `storage-data-specialist`, `compose-ui-specialist`, `qa-perf-specialist`
- **Yêu cầu gốc**: 
  1. "Tối ưu hơn cho việc update thì khi có bản mới sẽ download về đặt ở download rồi gọi cài đặt (Tránh trường hợp cần phải xóa bản cũ mới cài đc bản mới thì còn có file mà cài)."
  2. "Áp dụng phong cách Liquid Glass cho toàn bộ phần Thư viện (Library) để có cái style thống nhất."
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)

### 1.1. Tối ưu vị trí lưu trữ APK cập nhật (Persistent Public Downloads)
- **Vấn đề trước đây**: 
  - File APK tải về được lưu tại `context.cacheDir` (`/data/data/com.apple.android.music/cache/updates/update.apk`).
  - Khi người dùng gặp xung đột chữ ký từ bản cũ (`v1.0.1`) và cần gỡ cài đặt bản cũ, Android OS sẽ tự động xóa sạch thư mục private cache này, dẫn tới file APK vừa tải bị mất hoàn toàn, người dùng không còn file để cài lại.
- **Giải pháp kỹ thuật**:
  - Khi tải bản cập nhật trong `UpdateRepositoryImpl.kt`:
    1. Ghi luồng dữ liệu song song hoặc export bản sao chuẩn sang thư mục công cộng của thiết bị: `Environment.DIRECTORY_DOWNLOADS` (`/storage/emulated/0/Download/AppleMusic_Update.apk`) thông qua API `MediaStore.Downloads` (Android 10+ / API 29+) và tệp trực tiếp (Android 9 trở xuống).
    2. File này thuộc quyền quản lý của thiết bị, **hoàn toàn KHÔNG bị xóa** khi gỡ cài đặt ứng dụng.
    3. Cập nhật `file_paths.xml` bổ sung `<external-path>` và `<external-files-path>` để `FileProvider` có thể cấp quyền và cài đặt trực tiếp.
    4. Cập nhật giao diện `UpdateModalDialog`: Hiển thị đường dẫn rõ ràng `📁 Download/AppleMusic_Update.apk`, bổ sung nút "Mở thư mục Download" bằng Intent `DownloadManager.ACTION_VIEW_DOWNLOADS` bên cạnh nút "Cài đặt ngay".

### 1.2. Chuẩn hóa phong cách Liquid Glass (Kính lỏng) cho toàn bộ Thư viện (Library)
- **Đồng bộ ngôn ngữ thiết kế**:
  - Kế thừa hệ màu và hiệu ứng Liquid Glass từ `BottomNavBar` và `MiniPlayer`:
    - Dark Glass: `Color(0xEE1E1E22)` kèm viền phát sáng bạc `Color(0x33FFFFFF)` (0.75dp) và đổ bóng nổi `shadow(12.dp)`.
    - Light Glass: `Color(0xF2FFFFFF)` kèm viền đổ bóng tinh tế `Color(0x1F000000)`.
- **Nâng cấp các màn hình trong Thư viện**:
  1. **`LibraryScreen.kt`**:
     - Tiêu đề Apple Music: "Thư viện" kèm phụ đề mô tả.
     - **Liquid Glass Quick Stats Pills**: 3 viên thuốc kính lỏng nổi hiển thị số lượng Bài hát / Album / Nghệ sĩ với số liệu động và có thể nhấn để chuyển trang nhanh.
     - **Liquid Glass Navigation Group Card**: Thẻ nhóm điều hướng kính lỏng bo cong `24.dp`, viền sáng `0.75.dp`, icon bọc trong capsule bo tròn `12.dp` đỏ `AppleRed.copy(alpha = 0.12f)`, hiển thị phụ đề số lượng danh mục và chevron tinh tế.
     - **Liquid Glass Recently Added Carousel**: Danh sách album nghe gần đây với thẻ kính lỏng bo cong `20.dp`, ảnh bìa album bo cong `14.dp`.
  2. **`AlbumsScreen.kt`**:
     - Chuyển `AlbumGridItem` sang Liquid Glass Card với bo cong `20.dp`, khung kính bán trong suốt và viền ánh sáng.
  3. **`SongsScreen.kt`**:
     - Bổ sung **Twin Liquid Glass Action Pills**: Hai viên thuốc điều khiển kính lỏng nổi trên đầu trang ("Phát tất cả" và "Trộn bài") với hiệu ứng `AppleRed`.
  4. **`PlaylistsScreen.kt` & `ArtistsScreen.kt`**:
     - Chuẩn hóa Liquid Glass Card cho thẻ Danh sách phát và thẻ Nghệ sĩ (avatar tròn bo viền kính lỏng).

---

## 2. Chi Tiết Các Tệp Thay Đổi (Impacted Files)

- `[Modified]` `app/src/main/res/xml/file_paths.xml`: Mở rộng FileProvider hỗ trợ `external-path` và `external-files-path`.
- `[Modified]` `app/src/main/java/com/apple/android/music/data/repository/UpdateRepositoryImpl.kt`: Tích hợp `saveApkToPublicDownloads` ghi file vào `MediaStore.Downloads` và `Environment.DIRECTORY_DOWNLOADS/AppleMusic_Update.apk`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsScreen.kt`: Bổ sung thông tin vị trí lưu trữ APK và nút mở trực tiếp thư mục Download qua `ACTION_VIEW_DOWNLOADS`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/library/LibraryScreen.kt`: Tái thiết kế toàn diện theo phong cách Liquid Glass (Quick Stats Pills, Navigation Group Card, Album Carousel).
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/albums/AlbumsScreen.kt`: Tái cấu trúc AlbumGridItem thành Liquid Glass Card.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/songs/SongsScreen.kt`: Bổ sung thanh điều khiển Play/Shuffle dạng Liquid Glass Action Pills.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/playlists/PlaylistsScreen.kt`: Nâng cấp PlaylistGridItem theo phong cách kính lỏng.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/artists/ArtistsScreen.kt`: Nâng cấp danh sách nghệ sĩ thành LiquidGlassArtistCard.
- `[Modified]` `Docs/logs/README.md`: Cập nhật mục lục nhật ký truy vết.

---

## 3. Kiểm Thử & Xác Minh (Verification & Testing)

1. **Biên dịch Kotlin**:
   - `.\gradlew.bat compileDebugKotlin`: `BUILD SUCCESSFUL in 42s` (0 cảnh báo nghiêm trọng).
2. **Kiểm thử tự động (Unit Tests)**:
   - `.\gradlew.bat test`: `BUILD SUCCESSFUL in 1m 26s` (Toàn bộ test pass).
3. **Kiểm tra FileProvider & Quyền Storage**:
   - Khả năng đọc/ghi file `AppleMusic_Update.apk` trong `Download/` an toàn trên Android 10+ (API 29 - 35) qua MediaStore mà không cần cấp quyền đặc biệt.
