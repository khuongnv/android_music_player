# 🎨 Apple Music UI/UX Design System & Rules (Android Offline Music Player)

Tài liệu này là quy tắc chuẩn hóa giao diện người dùng (UI/UX Rules) bắt buộc áp dụng khi thiết kế, triển khai composables và màn hình trong dự án **Android Offline Music Player**.
Mọi giao diện phải tái hiện trọn vẹn tinh thần, sự tinh tế và chuyển động cao cấp của **Apple Music**.

---

## 🌟 1. Triết Lý Thiết Kế Cốt Lõi (Core Principles)

1. **Content-First (Tôn vinh âm nhạc)**:
   - Ảnh bìa album (Artwork) là tâm điểm thị giác.
   - Các nút điều khiển phải thanh thoát, kích thước cân đối, không được lấn át nội dung bài hát.
2. **Depth & Translucency (Kính mờ & Chiều sâu)**:
   - Sử dụng nền kính mờ (Frosted Glass / Blurs) cho Bottom Bar, MiniPlayer và nền NowPlayingScreen.
   - Tránh dùng các mảng màu đục (flat opaque solid colors) khô cứng khi có thể áp dụng hiệu ứng mờ bán trong suốt.
3. **Fluid Motion & Apple Spring Physics (Chuyển động có tính đàn hồi)**:
   - Không dùng chuyển động tuyến tính (Linear / Flat Easing).
   - Sử dụng `spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)` cho hiệu ứng phóng to/thu nhỏ artwork khi Play/Pause.
4. **Bold Hierarchy & Apple Typography**:
   - Phân cấp thông tin cực kỳ rõ nét: Tiêu đề màn hình rất lớn (`displayLarge` 34sp Bold), văn bản phụ màu xám trung tính (`#8E8E93`).

---

## 🎨 2. Hệ Màu & Bảng Token (Color System)

Tất cả màu sắc phải tham chiếu trực tiếp từ [`Color.kt`](../../app/src/main/java/com/apple/android/music/ui/theme/Color.kt):

| Token | Giá trị Hex | Ứng Dụng |
| :--- | :--- | :--- |
| `AppleRed` | `#FA2D55` | Điểm nhấn chính (Primary Accent), icon active, slider track, button CTA |
| `DarkBackground` | `#000000` | Nền đen thuần AMOLED cho Dark Theme |
| `DarkSurface` | `#1C1C1E` | Nền card, bottom bar, surface phụ (tương đương iOS `.secondarySystemBackground`) |
| `DarkSurfaceVariant` | `#2C2C2E` | Nền elevated / item hover / border phụ (`.tertiarySystemBackground`) |
| `LightBackground` | `#FFFFFF` | Nền trắng thuần cho Light Theme |
| `LightSurfaceVariant` | `#F2F2F7` | Nền card, search bar xám nhẹ (`.systemGray6`) |
| `Gray93` | `#8E8E93` | Văn bản phụ (Tên nghệ sĩ, thời lượng, icon inactive) |
| `GrayLight` | `#C7C7CC` | Đường viền (Hairline Dividers), thanh track slider chưa chạy |

> [!CAUTION]
> **Quy tắc cấm kỵ về màu sắc**:
> - Không dùng các màu accent khác (như xanh dương Material Blue hay tím Indigo) cho các thành phần điều hướng hoặc phát nhạc; màu chủ đạo duy nhất là `AppleRed` (`#FA2D55`).
> - Không dùng nền xám nhờ nhờ ở Dark mode; nền chính của Dark mode phải là đen sâu `#000000`.

---

## 📐 3. Quy Chuẩn Thành Phần Giao Diện (Component Specifications)

### 3.1. MiniPlayer (Thanh Phát Nhạc Nổi)
- **Vị trí & Dạng thức**: Floating card, neo cố định ngay phía trên thanh điều hướng đáy.
- **Kích thước**: Chiều cao `60.dp` – `64.dp`, bo góc `14.dp`.
- **Khoảng cách (Padding)**: Cách 2 mép màn hình `12.dp` – `16.dp`, cách mép đáy `8.dp` (nằm trên navigation bar).
- **Nền**: Bán trong suốt với hiệu ứng mờ:
  - Dark: `#1C1C1E` (alpha 0.90) kết hợp blur.
  - Light: `#FFFFFF` (alpha 0.90) kết hợp blur và drop shadow nhẹ (`elevation = 6.dp`).
- **Nội dung hiển thị**:
  - Thumbnail ảnh bìa: Kích thước `44.dp x 44.dp`, bo góc `8.dp`.
  - Tiêu đề & Nghệ sĩ: Title (15sp, SemiBold), Subtitle (13sp, Gray93), giới hạn 1 dòng, cắt đuôi `Ellipsis`.
  - Nút điều khiển: Chỉ hiển thị nút **Play/Pause** và **Next** (hoặc Favorite).
- **Hairline Progress**: Thanh tiến trình siêu mảnh (`2.dp`) chạy sát mép đáy của MiniPlayer với màu `AppleRed`.
- **Cử chỉ (Gestures)**:
  - **Chạm (Tap)**: Mở full `NowPlayingScreen`.
  - **Vuốt lên (Swipe Up)**: Mở `NowPlayingScreen` với animation trượt mượt mà.
  - **Vuốt ngang (Swipe Left/Right)**: Chuyển bài kế tiếp hoặc bài trước kèm hiệu ứng bounce.

### 3.2. NowPlayingScreen (Màn Hình Phát Toàn Phần)
- **Nền (Background)**:
  - Lấy màu từ ảnh bìa album (Dynamic Palette/Dominant Color) tạo gradient chuyển mờ hoặc hiệu ứng Gaussian Blur chiều sâu.
  - Phía dưới có lớp phủ mờ tối (Vignette) để tôn chữ và controls màu trắng.
- **Ảnh Bìa Album (Hero Artwork)**:
  - Tỉ lệ: Vuông 1:1, kích thước linh động (chiếm ~65-70% chiều ngang màn hình).
  - Bo góc: Chuẩn `20.dp` – `24.dp`.
  - Đổ bóng: Soft colored shadow mềm mại lan tỏa quanh ảnh bìa.
  - **Dynamic Scale Physics**:
    - **Khi Đang Phát**: Scale = `1.0f`.
    - **Khi Tạm Dừng**: Scale co nhẹ lại = `0.85f` với `spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)`.
- **Thanh Tua Tiến Trình (Scrubbing Bar)**:
  - Track: Chiều cao `4.dp` – `6.dp` bo tròn 2 đầu.
  - Thumb: Bình thường ẩn hoặc chỉ là chấm nhỏ `6.dp`; khi người dùng chạm giữ (dragging) sẽ phóng lớn lên `14.dp` – `16.dp`.
  - Hiển thị thời gian: Bên trái là `MM:SS` (đã phát), bên phải là `-MM:SS` (thời lượng còn lại) với phông monospace hoặc số cân đối.
- **Hàng Nút Điều Khiển Chính (Transport Controls)**:
  - Play/Pause: Nút lớn ở giữa (size `56.dp` – `64.dp`).
  - Previous / Next: Hai bên nút chính (size `36.dp` – `40.dp`).
  - Shuffle & Repeat: Nằm ở 2 rìa đối xứng, kích thước `24.dp`. Khi active đổi màu `AppleRed`, khi inactive màu xám mờ.

### 3.3. Danh Sách Bài Hát (Song List & Items)
- **Kích thước dòng (Item Height)**: Chuẩn `60.dp` – `64.dp`.
- **Thumbnail**: `48.dp x 48.dp`, bo góc `8.dp`.
- **Text Alignment**:
  - Tên bài hát: `bodyLarge` (17sp, SemiBold), 1 dòng.
  - Tên ca sĩ: `bodyMedium` (15sp, `Gray93`), 1 dòng.
- **Hairline Divider (Đường kẻ phân cách)**:
  - Độ dày: `0.5.dp`.
  - Màu sắc: `#38383A` (Dark) / `#E5E5EA` (Light).
  - **Inset bắt buộc**: Divider bắt đầu từ vị trí x = `72.dp` (thẳng hàng với lề chữ), **tuyệt đối không kéo dài đè lên thumbnail**.

### 3.4. Lưới Album (Album Cards & Grid)
- **Bố cục**: 2 cột đối xứng (`LazyVerticalGrid(columns = GridCells.Fixed(2))`).
- **Card**:
  - Ảnh bìa 1:1, bo góc `12.dp` – `14.dp`.
  - Tên Album in đậm (15sp, SemiBold), tối đa 1 dòng.
  - Tên Nghệ sĩ in nhạt (13sp, `Gray93`), 1 dòng.
  - Khoảng cách giữa các cột: `16.dp`.

### 3.5. Thanh Điều Hướng Đáy (Bottom Navigation Bar)
- **Số lượng tab**: 3 - 4 tab (`Library`, `Playlists`, `Search`, `Settings`).
- **Nền**: Translucent Blur với độ mờ cao (`Modifier.blur`), độ cao thanh điều hướng `56.dp` (cộng thêm `WindowInsets.navigationBars`).
- **Biểu tượng**:
  - Đang chọn: Tô đầy (Filled icon) với màu `AppleRed`.
  - Chưa chọn: Viền nét (Outlined icon) với màu `Gray93`.

---

## ⚡ 4. Quy Chuẩn Hiệu Năng Jetpack Compose (Recomposition & Rendering)

1. **Lazy List & Grid Keys**:
   ```kotlin
   LazyColumn {
       items(songs, key = { it.id }) { song ->
           SongItem(song = song, ...)
       }
   }
   ```
   *Bắt buộc phải có `key` để tránh render lại toàn bộ danh sách khi có thay đổi nhỏ.*

2. **Tải Ảnh Bìa với Coil**:
   - Sử dụng `AsyncImage` kèm `crossfade(300)`.
   - Luôn định cấu hình ảnh placeholder dự phòng (nốt nhạc Apple Music trên nền xám).
   - Đặt `contentScale = ContentScale.Crop`.

3. **Tối Ưu Chuyển Động & Trạng Thái**:
   - Dùng `derivedStateOf` khi tính toán trạng thái từ Scroll State hoặc Slider scrubbing.
   - Các class UiState phải là `@Immutable` data class.
   - Thêm phản hồi rung nhẹ (**Haptic Feedback**) qua `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.LongPress)` khi người dùng nhấn nút chuyển bài hoặc Play/Pause.

---

## 🚫 5. Danh Sách Điều Cần Tránh (Anti-patterns)

- ❌ **Tránh**: Sử dụng thanh slider mặc định gồ ghề của Material 2/3 không tùy biến.
- ❌ **Tránh**: Sử dụng đường kẻ ngăn cách (Divider) tràn toàn màn hình cắt ngang qua thumbnail bài hát.
- ❌ **Tránh**: Nền đen xám nhợt nhạt ở chế độ tối thay vì đen tuyền AMOLED (`#000000`).
- ❌ **Tránh**: Bỏ qua animation co/giãn ảnh bìa khi Play/Pause ở màn hình phát nhạc.
- ❌ **Tránh**: Làm MiniPlayer đục ngầu, vuông góc cạnh thay vì bo góc nổi bán trong suốt.
