---
name: offline-music-swarm
description: Operational runbook and multi-agent coordination protocol for the Android Offline Music Player project.
---

# Android Offline Music Player - Swarm Coordination Skill

Skill này cung cấp cẩm nang vận hành và các kịch bản phối hợp chuyên sâu giữa các agent trong Swarm Agent dành cho dự án Android Offline Music Player.

## 👥 Danh sách Swarm Subagents & Vai trò

| Subagent Name | Lĩnh vực chuyên môn | Khi nào triệu hồi? |
| :--- | :--- | :--- |
| `media-playback-specialist` | AndroidX Media3, ExoPlayer, AudioFocus, Service, Equalizer | Khi chỉnh sửa `MusicService`, notification, sleep timer, audio engine, playback queue |
| `compose-ui-specialist` | Jetpack Compose, Material 3, Apple Music UI, Animations | Khi sửa giao diện, MiniPlayer, NowPlaying, Theme, Compose screens & components |
| `storage-data-specialist` | MediaStore scanner, Room DB, Jetpack DataStore, Repositories | Khi sửa quét bài hát cục bộ, playlist DB, lưu trạng thái, scoped storage permissions |
| `qa-perf-specialist` | Kiểm thử, Battery, Memory leak, Background limit, Gradle build | Khi chuẩn bị release, kiểm tra hiệu năng, viết test, debug lỗi crash, rà soát memory |
| `lead-architect` | Điều phối tổng thể, Clean Architecture, Hilt DI | Khi cần thiết kế tính năng lớn, tái cấu trúc hoặc phân chia subtask |

---

## 🛠 Kịch bản phối hợp mẫu (Standard Workflows)

### Kịch bản 1: Thêm tính năng Equalizer & Audio Effects
1. **`lead-architect`**:
   - Định nghĩa UseCase trong Domain layer (`GetEqualizerSettingsUseCase`, `SetEqualizerPresetUseCase`).
   - Thiết kế model `EqualizerBand`, `EqualizerPreset`.
2. **`media-playback-specialist`**:
   - Tích hợp `android.media.audiofx.Equalizer` hoặc Media3 AudioProcessor vào `MusicService`.
   - Lắng nghe audio session ID từ ExoPlayer.
3. **`compose-ui-specialist`**:
   - Tạo `EqualizerScreen` với Apple Music visual style (băng tần dạng slider, preset dropdown, visualizer preview).
4. **`storage-data-specialist`**:
   - Lưu cấu hình Equalizer vào DataStore Preferences.
5. **`qa-perf-specialist`**:
   - Kiểm tra giải phóng Equalizer khi destroy Service, đảm bảo không rò rỉ audio session.

### Kịch bản 2: Tối ưu hóa quét nhạc từ bộ nhớ (MediaStore Scanner)
1. **`storage-data-specialist`**:
   - Tối ưu hóa projection MediaStore, dùng Coroutine Flow phát ra từng đợt (batch) thay vì load hàng ngàn bài hát cùng lúc vào RAM.
   - Bổ sung `ContentObserver` để tự động cập nhật khi người dùng copy thêm nhạc vào máy.
2. **`compose-ui-specialist`**:
   - Hiển thị Skeleton loading / Shimmer khi đang quét thư viện.
3. **`qa-perf-specialist`**:
   - Stress test với thư mục chứa > 5.000 bài hát, kiểm tra memory allocation.

### Kịch bản 3: Nâng cấp MiniPlayer & cử chỉ vuốt (Gesture & Sheet)
1. **`compose-ui-specialist`**:
   - Nâng cấp `MiniPlayer` với swipe gesture (vuốt lên để mở `NowPlayingScreen`, vuốt ngang để Next/Previous bài).
   - Tích hợp Apple Music spring animation.
2. **`media-playback-specialist`**:
   - Cung cấp playback progress flow với độ mịn cao (16ms throttle) để slider trượt mượt mà.
3. **`qa-perf-specialist`**:
   - Đo FPS Recomposition để đảm bảo không rớt frame khi trượt slider.

---

## 📋 Bảng kiểm tra trước khi hoàn thành task (Definition of Done)
- [ ] **BẮT BUỘC**: Đã ghi chép đầy đủ nhật ký thực thi dạng Markdown vào `Docs/logs/YYYY-MM-DD_<ten_tac_vu>.md` và cập nhật `Docs/logs/README.md`.
- [ ] Không vi phạm Clean Architecture: UI không import trực tiếp Data/Service.
- [ ] Mọi thao tác I/O (File, DB, MediaStore) đều chạy trên `Dispatchers.IO`.
- [ ] Player chỉ được điều khiển từ Main Thread / MediaController.
- [ ] Foreground Service khai báo đúng type `mediaPlayback` và có notification điều khiển hợp lệ.
- [ ] Tránh memory leak: Đã unbind / unregister listener trong `onCleared` hoặc `onDestroy`.
- [ ] Đã chạy kiểm tra cú pháp và build thử nghiệm thành công.
