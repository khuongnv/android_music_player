# 🎵 Apple Music UI/UX Style Guide & Design System

Hướng dẫn quy chuẩn thiết kế giao diện và trải nghiệm người dùng dành cho ứng dụng **Android Offline Music Player**, được chuẩn hóa theo phong cách thiết kế của **Apple Music**.

---

## 📌 1. Triết Lý Thiết Kế & Nhận Diện Thị Giác

Ứng dụng hướng đến trải nghiệm cao cấp, sang trọng, tinh tế và tập trung tuyệt đối vào âm nhạc của người dùng:

```text
┌────────────────────────────────────────────────────────────────────────┐
│  🍎 THẨM MỸ APPLE MUSIC TRONG JETPACK COMPOSE                          │
│                                                                        │
│  1. Pure AMOLED Black (#000000) & Clean White (#FFFFFF)                │
│  2. Chữ Tiêu Đề Rất Lớn (Large Titles 34sp Bold)                       │
│  3. Điểm Nhấn Sắc Đỏ Hồng Tươi Tắn (#FA2D55)                           │
│  4. Kính Mờ Bán Trong Suốt (Frosted Glass / Blurs)                     │
│  5. Chuyển Động Vật Lý Có Độ Đàn Hồi (Spring Physics)                  │
│  6. Bo Góc Tự Nhiên (Corner Radius 8dp - 24dp)                         │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🎨 2. Hệ Thống Màu Sắc (Color Tokens)

Mọi màu sắc sử dụng trong ứng dụng đều được khai báo trong [`app/src/main/java/com/apple/android/music/ui/theme/Color.kt`](../app/src/main/java/com/apple/android/music/ui/theme/Color.kt):

### Bảng màu chính
| Tên Biến | Giá Trị Mã Màu | Mục Đích Sử Dụng |
| :--- | :--- | :--- |
| `AppleRed` | `0xFFFA2D55` | Màu nhận diện thương hiệu, Active Tab, Primary Action, Playhead |
| `DarkBackground` | `0xFF000000` | Nền đen sâu AMOLED cho chế độ tối |
| `DarkSurface` | `0xFF1C1C1E` | Nền Card, thanh MiniPlayer, Menu popup (iOS `.secondarySystemBackground`) |
| `DarkSurfaceVariant` | `0xFF2C2C2E` | Nền các thành phần phụ, hover (iOS `.tertiarySystemBackground`) |
| `LightBackground` | `0xFFFFFFFF` | Nền trắng tinh khiết cho chế độ sáng |
| `LightSurfaceVariant` | `0xFFF2F2F7` | Nền Card và ô tìm kiếm chế độ sáng (iOS `.systemGray6`) |
| `Gray93` | `0xFF8E8E93` | Màu chữ phụ (Nghệ sĩ, album, thời lượng, Inactive Tab) |
| `GrayLight` | `0xFFC7C7CC` | Đường kẻ phân cách (Hairline Divider), Track slider chưa phát |

---

## 🔤 3. Quy Chuẩn Kiểu Chữ (Typography Scale)

Dựa trên chuẩn **Apple Human Interface Guidelines (HIG)** trong [`Type.kt`](../app/src/main/java/com/apple/android/music/ui/theme/Type.kt):

| Style Compose | Cỡ Chữ (sp) | Độ Đậm (Weight) | Khoảng Cách Chữ (Letter Spacing) | Vị Trí Áp Dụng |
| :--- | :--- | :--- | :--- | :--- |
| `displayLarge` | 34.sp | **Bold** (700) | 0.37.sp | Tiêu đề chính màn hình (Large Title) |
| `headlineLarge` | 22.sp | **SemiBold** (600) | 0.35.sp | Tiêu đề nhóm, phân mục lớn |
| `titleLarge` | 20.sp | **SemiBold** (600) | 0.38.sp | Tên bài hát trong màn hình Now Playing |
| `bodyLarge` | 17.sp | **Normal** (400) | -0.41.sp | Tên bài hát trong danh sách thư viện |
| `bodyMedium` | 15.sp | **Normal** (400) | -0.24.sp | Tên nghệ sĩ/album trong danh sách |
| `labelMedium` | 13.sp | **Normal** (400) | -0.08.sp | Đồng hồ đếm thời gian phát, chú thích nhỏ |

---

## 📱 4. Giải Phẫu Các Thành Phần Cốt Lõi (Component Anatomy)

### 4.1. MiniPlayer (Thanh điều khiển nổi)
```text
┌─────────────────────────────────────────────────────────────┐
│  [Artwork]   Tên bài hát đang phát                 (▶) (⏭)  │
│  [ 44x44 ]   Tên ca sĩ biểu diễn                            │
│  ═════════════════════════════════════════════════════════  │ <- Hairline Progress (2dp)
└─────────────────────────────────────────────────────────────┘
```
- **Bo góc**: `14.dp`.
- **Chiều cao**: `64.dp`.
- **Nền**: Translucent với blur (Dark: `#1C1C1E` 90% alpha + blur; Light: `#FFFFFF` 90% alpha + shadow).
- **Hairline Progress**: Thanh chạy đỏ `AppleRed` dày 2dp ở chân MiniPlayer.
- **Cử chỉ**:
  - `Swipe Up` hoặc `Tap`: Mở full `NowPlayingScreen`.
  - `Swipe Left/Right`: Chuyển bài với hiệu ứng đàn hồi.

### 4.2. NowPlayingScreen (Màn hình phát nhạc toàn phần)
```text
┌─────────────────────────────────────────────────────────────┐
│                (⌄)         Đang Phát          (⋯)           │
│                                                             │
│                 ┌────────────────────────┐                  │
│                 │                        │                  │
│                 │      ALBUM ARTWORK     │                  │
│                 │       (Scale 1.0       │                  │
│                 │      khi đang phát)    │                  │
│                 │                        │                  │
│                 └────────────────────────┘                  │
│                                                             │
│   Tên Bài Hát Tuyệt Vời                            (♡)      │
│   Tên Nghệ Sĩ Trình Bày                                     │
│                                                             │
│   ──────────●────────────────────────────────────────────   │ <- Scrubbing Slider
│   01:24                                            -02:45   │
│                                                             │
│          (🔀)      (⏮)       ( ▶ )       (⏭)      (🔁)      │
│                                                             │
│          (🔈) ──────────────────●─────── (🔊)               │
└─────────────────────────────────────────────────────────────┘
```
- **Hiệu ứng thu/phóng Artwork**:
  - Đang phát: `scale = 1.0f`.
  - Tạm dừng: `scale = 0.85f` (sử dụng `animateFloatAsState` với `spring`).
- **Nền**: Ambient Artwork Blur (Gaussian blur gradient mờ lấy cảm hứng từ ảnh bìa).
- **Thanh Scrubbing Slider**:
  - Track mỏng thanh lịch (`4.dp`).
  - Thumb chỉ to lên khi đang chạm vuốt (Scrubbing feedback).
  - Thời gian hiển thị chuẩn: Bên trái `MM:SS`, bên phải `-MM:SS`.

### 4.3. Song Item (Mục bài hát trong thư viện)
```text
┌───┬─────────────────────────────────────────────────────┬───┐
│[■]│  Tên bài hát (17sp SemiBold)                        │(⋯)│
│48 │  Tên ca sĩ • 03:45 (15sp Gray93)                    │   │
└───┴─────────────────────────────────────────────────────┴───┘
       ───────────────────────────────────────────────────────  <- Inset Divider (từ x = 72dp)
```
- **Thumbnail**: `48.dp x 48.dp`, bo góc `8.dp`.
- **Hairline Divider**: Dày `0.5.dp`, lùi lề trái `72.dp` (không gạch ngang qua ảnh).

---

## 🏃 5. Quy Chuẩn Chuyển Động & Phản Hồi Rung (Motion & Haptics)

1. **Spring Physics (Độ đàn hồi tự nhiên)**:
   ```kotlin
   val artworkScale by animateFloatAsState(
       targetValue = if (isPlaying) 1f else 0.85f,
       animationSpec = spring(
           dampingRatio = Spring.DampingRatioMediumBouncy,
           stiffness = Spring.StiffnessLow
       ),
       label = "ArtworkScale"
   )
   ```

2. **Haptic Feedback (Phản hồi xúc giác)**:
   - Khi bấm **Play/Pause**, **Next**, **Previous**, **Favorite**: Kích hoạt `HapticFeedbackType.LongPress` hoặc `TextHandleMove` để tạo cảm giác cơ học tinh tế như phím bấm vật lý của Apple.

3. **Tối ưu tốc độ khung hình (120 FPS)**:
   - Tránh tính toán nặng trong quá trình layout.
   - Dùng `Modifier.graphicsLayer` để tận dụng GPU khi scale hoặc translate.

---

## 📋 6. Bảng Kiểm Tra Thiết Kế UI (UI Definition of Done)

Khi tạo màn hình hoặc component mới:
- [ ] Đã sử dụng mã màu từ `Color.kt` (chủ đạo `AppleRed`, nền đen `#000000` trên dark theme).
- [ ] Đã dùng bo góc chuẩn (`8.dp` cho item nhỏ, `14.dp` cho card/miniplayer, `20.dp` cho artwork lớn).
- [ ] Đường kẻ phân cách có inset lùi lề theo lề chữ (không gạch qua thumbnail).
- [ ] MiniPlayer nổi có bo góc và nền mờ bán trong suốt.
- [ ] Artwork có animation scale đàn hồi khi play/pause.
- [ ] Recomposition được tối ưu với `key` trong danh sách và `@Immutable` state.
