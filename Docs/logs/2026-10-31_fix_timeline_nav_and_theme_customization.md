# 📋 Nhật Ký Thực Thi: Sửa điều hướng Timeline và hoàn thiện cài đặt Dark Light Theme

- **Ngày thực hiện**: 2026-10-31 14:03:00
- **Agent phụ trách**: `lead-architect`, `compose-ui-specialist`, `storage-data-specialist`, `qa-perf-specialist`
- **Yêu cầu gốc**: Bỏ thao tác vào timeline từ Thư viện gây ra không back được. Sửa lỗi giao diện theme dark và light cho đúng chuẩn Apple Music và có cài đặt chuyển đổi theme.
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)

### 1.1. Vấn đề 1: Lỗi điều hướng Timeline từ Thư viện không back được
- **Hiện trạng**: Trong `LibraryScreen.kt`, mục "Hoạt động 24h (Timeline)" được đặt trong danh sách menu của Thư viện. Khi người dùng bấm vào, `navController.navigate("timeline")` đẩy màn hình Timeline vào stack đè lên Thư viện. Tuy nhiên, `TimelineScreen` vốn được thiết kế là một Tab chính cấp 1 (đã có trên thanh BottomNavBar), không có nút mũi tên Back trên TopAppBar. Người dùng cảm thấy bị mắc kẹt không quay lại Thư viện được.
- **Giải pháp**:
  1. Loại bỏ hoàn toàn mục "Hoạt động 24h (Timeline)" khỏi danh sách menu trong `LibraryScreen.kt`. Timeline 24h được định vị chuẩn mực và độc quyền tại thanh điều hướng đáy (Bottom Navigation Bar) như một tính năng khám phá độc lập.
  2. Bổ sung `BackHandler` thông minh tại `MainActivity.kt`: Khi người dùng đang ở tab Timeline hoặc Cài đặt (những tab cấp 1 không phải Library), nhấn nút Back của Android hoặc vuốt Back sẽ tự động chuyển hướng mượt mà về tab gốc "Thư viện" (`popUpTo(startDestination)`).

### 1.2. Vấn đề 2: Lỗi hiển thị Dark/Light Theme & Thiếu cài đặt Theme
- **Hiện trạng**:
  1. Thiếu tính năng tùy chọn Theme: Ứng dụng chỉ phụ thuộc thụ động vào `isSystemInDarkTheme()`, không có mục cài đặt trong Settings để người dùng chủ động chọn "Theo hệ thống", "Sáng", "Tối".
  2. Màu sắc bị gán cứng (Hardcoded): Trong `SettingsScreen.kt` và `TimelineScreen.kt`, các thẻ `SettingsGroupCard`, `TimelineOverviewCard`, `SelectedPointInspectorCard`, `ActivitySessionItem` bị gán cứng `color = DarkSurface` (`#1C1C1E`) và divider `Color.White.copy(0.08f)`. Khi chuyển sang chế độ Sáng (Light Theme), nền màn hình là Trắng nhưng các Card lại có màu đen, kèm theo chữ `onSurface` màu đen dẫn đến hiện tượng chữ đen trên nền đen (tàng hình/không nhìn thấy chữ).
  3. `MiniPlayer.kt` đọc trực tiếp `isSystemInDarkTheme()` thay vì lắng nghe trạng thái theme được thiết lập.
- **Giải pháp**:
  1. Xây dựng `ThemeMode` (SYSTEM, LIGHT, DARK) trong domain model.
  2. Lưu trữ cài đặt Theme trong Jetpack DataStore qua `SettingsRepository` & `SettingsRepositoryImpl`.
  3. Quan sát `themeMode` tại `MainActivity.kt` và áp dụng trực tiếp vào `MusicPlayerTheme`.
  4. Chuẩn hóa hệ màu Apple Music trong `Color.kt` & `Theme.kt`:
     - **Dark Theme**: Nền `#000000`, Card `#1C1C1E`, Elevated `#2C2C2E`, Chữ `#FFFFFF`, Phụ `#8E8E93`.
     - **Light Theme**: Nền `#FFFFFF`, Card `#F2F2F7` (chuẩn iOS grouped cards), Elevated `#E5E5EA`, Chữ `#000000`, Phụ `#8E8E93`.
  5. Thay thế toàn bộ `DarkSurface` và `DarkSurfaceVariant` thành `MaterialTheme.colorScheme.surface` và `surfaceVariant`.
  6. Xây dựng thành phần điều khiển Apple Segmented Control 3 chế độ (Hệ thống / Sáng / Tối) ngay trong màn hình Cài đặt.

---

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)

- **Bước 1**: Tạo enum `ThemeMode` tại `domain/model/ThemeMode.kt` gồm 3 tùy chọn: `SYSTEM`, `LIGHT`, `DARK`.
- **Bước 2**: Mở rộng `SettingsRepository` và `SettingsRepositoryImpl` với `themeMode: Flow<ThemeMode>` và `setThemeMode(mode: ThemeMode)` sử dụng Jetpack DataStore Preferences (`PreferencesKeys.THEME_MODE`).
- **Bước 3**: Cập nhật `SettingsUiState` và `SettingsViewModel` để quản lý luồng dữ liệu thay đổi theme.
- **Bước 4**: Cập nhật `MainActivity.kt`: Lắng nghe `themeMode` thời gian thực, truyền cờ `isDarkTheme` vào `MusicPlayerTheme`, bổ sung `BackHandler` khi đứng ở tab Timeline/Settings để quay về Thư viện.
- **Bước 5**: Loại bỏ mục `Hoạt động 24h` khỏi `menuItems` của `LibraryScreen.kt`.
- **Bước 6**: Chuẩn hóa `Color.kt` và `Theme.kt` cho `LightSurface` (`#F2F2F7`) và `LightSurfaceVariant` (`#E5E5EA`).
- **Bước 7**: Refactor `TimelineScreen.kt`: Thay thế `DarkSurface` / `DarkSurfaceVariant` sang `MaterialTheme.colorScheme.surface / surfaceVariant`, sửa màu divider, tooltip và con trỏ scrubber tương thích cả nền sáng và tối.
- **Bước 8**: Refactor `SettingsScreen.kt`: Thay thế toàn bộ `DarkSurface` sang `MaterialTheme.colorScheme.surface`, bổ sung Section "GIAO DIỆN & CHỦ ĐỀ" với component `ThemeSegmentedPicker` phong cách Apple.
- **Bước 9**: Cập nhật `MiniPlayer.kt` đồng bộ màu nền và độ đổ bóng theo dynamic theme.

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)
- `[Created]` `app/src/main/java/com/apple/android/music/domain/model/ThemeMode.kt` - Enum định nghĩa chế độ theme.
- `[Modified]` `app/src/main/java/com/apple/android/music/domain/repository/SettingsRepository.kt` - Khai báo Flow & hàm lưu theme.
- `[Modified]` `app/src/main/java/com/apple/android/music/data/repository/SettingsRepositoryImpl.kt` - Triển khai lưu theme vào DataStore.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsUiState.kt` - Bổ sung thuộc tính `themeMode`.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsViewModel.kt` - Quản lý và cập nhật `themeMode`.
- `[Modified]` `app/src/main/java/com/apple/android/music/MainActivity.kt` - Điều phối dynamic theme và xử lý BackHandler.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/library/LibraryScreen.kt` - Bỏ menu Timeline gây lỗi kẹt điều hướng.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/theme/Color.kt` - Tinh chỉnh bảng màu LightSurface và LightSurfaceVariant.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/timeline/TimelineScreen.kt` - Chuẩn hóa màu theme cho toàn bộ card, track và con trỏ.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/screens/settings/SettingsScreen.kt` - Chuẩn hóa màu card, dialog và thêm bộ chọn Segmented Control.
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/components/MiniPlayer.kt` - Đồng bộ màu nền theo theme động.

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)
1. **Kiểm tra biên dịch Kotlin**:
   - Lệnh: `.\gradlew.bat compileDebugKotlin`
   - Kết quả: `BUILD SUCCESSFUL in 56s` (0 cảnh báo nghiêm trọng, không có lỗi cú pháp).
2. **Kiểm thử Unit Tests**:
   - Lệnh: `.\gradlew.bat testDebugUnitTest`
   - Kết quả: `BUILD SUCCESSFUL in 31s` (Tất cả test cases đều pass).
3. **Đóng gói kiểm tra APK Debug**:
   - Lệnh: `.\gradlew.bat assembleDebug`
   - Kết quả: `BUILD SUCCESSFUL in 29s`.

---

## 5. Kết Luận & Nghiệm Thu (Conclusion)
- Màn hình Thư viện (`LibraryScreen`) đã được tinh gọn, loại bỏ hoàn toàn lối vào Timeline gây kẹt Back.
- Tính năng Timeline 24h hoạt động độc lập qua BottomNavBar và hỗ trợ Back quay về Thư viện mượt mà.
- Hệ thống Dark & Light Theme đã được chuẩn hóa trọn vẹn theo triết lý Apple Music:
  - Hỗ trợ 3 tùy chọn: **Hệ thống**, **Sáng**, **Tối** với bộ điều khiển Segmented Control phản hồi tức thì.
  - Loại bỏ triệt để hiện tượng chữ chìm, chữ đen trên nền đen trong Light Theme.
