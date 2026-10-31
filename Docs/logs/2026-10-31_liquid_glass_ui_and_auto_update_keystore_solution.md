# 📋 Nhật Ký Thực Thi: Thiết kế Liquid Glass Menu Bar & Giải Pháp Toàn Diện Cho Chữ Ký Auto-Update

- **Ngày thực hiện**: 2026-10-31 17:25:00
- **Agent phụ trách**: `lead-architect`, `compose-ui-specialist`, `qa-perf-specialist`
- **Yêu cầu gốc**: "Thanh menu bar hay học tập phong cách lequid glass để bo icon và bo menu bar cho đẹp hơn. Và update vẫn đang chưa thể được"
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Nguyên Nhân Gốc Rễ (Root Cause Analysis)

### 1.1. Bản chất mật mã học của lỗi "Signatures of the update package and the installed app do not match"
- **Đối chiếu thực tế qua `apksigner` trên từng bản phát hành GitHub Actions**:
  - `v1.0.0`: `SHA-256: 89361f3261c8dc6f5221a5809fc410cde9f1a05e29d9b095878fd74a3159d117` (Runner VM 1)
  - `v1.0.1`: `SHA-256: 9a78d54138c392158cb64c857e524718a2ca1ba7908d601a472062fca928f997` (Runner VM 2)
  - `v1.1.1`: `SHA-256: f1832a49a223b04ae703cea048cee40650b421ca3287c19d9971182ce83e081f` (Runner VM 3)
  - `v1.2.0`: `SHA-256: 072695d37f8f3602bbf6e968ad3798f5e4eeb5130a8a223beb902d2519f7f777` (Runner VM 4)
  - `v1.2.1` & `v1.2.2`: `SHA-256: e18c408c52c5664f3a67e8f671f63ebfcbfe0c414bad8e49ffaa2dd250082ee2` (Persistent Keystore cố định trong Git)
- **Cơ chế bảo mật của Android OS**:
  - Khi cập nhật một ứng dụng đã cài đặt, hệ điều hành Android (`PackageManagerService`) bắt buộc chữ ký số (Certificate Fingerprint) của file APK mới phải **trùng khớp 100%** với bản hiện có trên máy nhằm ngăn chặn tấn công giả mạo (app hijacking).
  - Do các bản `v1.0.0` -> `v1.2.0` trước đây được build trên máy ảo GitHub Actions tạm thời (ephemeral runner) mà chưa commit keystore cố định, mỗi lần CI/CD chạy nó tự sinh một keystore ngẫu nhiên.
  - Máy của người dùng hiện đang cài đặt phiên bản `v1.0.1` mang chữ ký `9a78d541...`. Khóa private key của bản này đã biến mất vĩnh viễn khi máy ảo GitHub đóng lại.
  - **Kết luận bắt buộc**: Người dùng cần gỡ cài đặt phiên bản cũ `v1.0.1` **duy nhất 1 lần** và cài đặt bản `v1.2.2` (hoặc `v1.2.1`). Bắt đầu từ bản này, vì keystore `app/keystore/debug.keystore` đã được cố định vĩnh viễn trong repo (`e18c408c...`), toàn bộ các bản cập nhật trong tương lai (`v1.2.3`, `v1.3.0`,...) sẽ khớp chữ ký 100% và cập nhật đè (in-place update) hoàn toàn trơn tru mà không bao giờ báo lỗi này nữa.

---

### 1.2. Tái thiết kế giao diện theo phong cách Liquid Glass (Kính lỏng)
- Theo yêu cầu của người dùng, thanh menu đáy và mini player được tái thiết kế theo trường phái **Liquid Glass / Apple Glassmorphism**:
  1. **Floating Dock Bo Cong Lớn**:
     - `BottomNavBar`: Biến thành một thanh dock nổi lơ lửng với bo góc lớn `RoundedCornerShape(26.dp)`.
     - `MiniPlayer`: Đồng bộ bo góc `RoundedCornerShape(20.dp)` và căn lề biên độ chuẩn (`horizontal = 16.dp`).
  2. **Vật liệu kính lỏng (Liquid Glass Material)**:
     - Chế độ Tối (Dark Mode): Kính khói bán trong suốt `Color(0xEE1E1E22)` kèm viền phát sáng bạc (luminous rim) `0.75.dp` `Color(0x33FFFFFF)`.
     - Chế độ Sáng (Light Mode): Kính pha lê trong trẻo `Color(0xF2FFFFFF)` kèm viền đổ bóng `Color(0x1F000000)`.
  3. **Bo tròn vùng Icon & Label (Capsule Pill Indicator)**:
     - Mục đang được chọn (Active Tab) được bọc trong một viên thuốc kính lỏng `RoundedCornerShape(20.dp)`.
     - Nền viên thuốc chuyển màu mượt mà bằng hiệu ứng Spring Animation với sắc đỏ đặc trưng `AppleRed.copy(alpha = 0.12f)` và viền `AppleRed.copy(alpha = 0.28f)`.
  4. **Nghệ thuật hóa Tab "Ký ức"**:
     - Sử dụng icon `AutoAwesome` (chùm sao lấp lánh biểu trưng cho Apple Memories).
     - Đồng bộ màn hình `TimelineScreen.kt` thành trang "Ký ức".

---

## 2. Chi Tiết Các Tệp Thay Đổi (Impacted Files)
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/components/BottomNavBar.kt`: Triển khai Liquid Glass dock nổi, capsule pill indicator bo cong cho icon, viền phát sáng, tab "Ký ức".
- `[Modified]` `app/src/main/java/com/apple/android/music/ui/components/MiniPlayer.kt`: Đồng bộ Liquid Glass nổi, viền ánh sáng 0.75dp, bo cong góc 20dp, căn lề 16dp.
- `[Modified]` `app/src/main/java/com/apple/android/music/MainActivity.kt`: Tinh chỉnh khoảng cách lề và padding của MiniPlayer khớp hoàn hảo với dock BottomNavBar.
- `[Modified]` `Docs/logs/README.md`: Cập nhật mục lục nhật ký truy vết.

---

## 3. Kiểm Thử & Xác Minh (Verification & Testing)
1. **Kiểm tra biên dịch Kotlin**:
   - `.\gradlew.bat compileDebugKotlin`: `BUILD SUCCESSFUL` (0 lỗi).
2. **Kiểm tra đóng gói Release APK**:
   - `.\gradlew.bat assembleRelease`: `BUILD SUCCESSFUL`.
3. **Kiểm tra chữ ký số bằng apksigner**:
   - Chữ ký SHA-256: `e18c408c52c5664f3a67e8f671f63ebfcbfe0c414bad8e49ffaa2dd250082ee2`.
   - Chuẩn APK Signature Scheme v2: `true`.
   - Keystore cố định từ `app/keystore/debug.keystore`.
