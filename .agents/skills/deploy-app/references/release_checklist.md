# 📋 Bảng Kiểm Tra Quy Trình Phát Hành (Release Checklist)

Quy chuẩn kiểm tra bắt buộc trước và sau khi kích hoạt deploy phiên bản mới cho **Android Offline Music Player**.

---

## 1. Trước Khi Phát Hành (Pre-Release Verification)

- [ ] **Mã nguồn sạch sẽ**: Kiểm tra `git status`, không để sót các file rác hoặc commit dở dang.
- [ ] **Kiểm tra biên dịch**: Chạy `./gradlew.bat compileDebugKotlin` hoặc `./gradlew.bat testDebugUnitTest` đảm bảo 100% build thành công.
- [ ] **Xác nhận phiên bản SemVer**:
  - `versionName`: Định dạng chuẩn `X.Y.Z` (ví dụ `1.0.1`, `1.1.0`).
  - `versionCode`: Số nguyên tăng dần (+1 so với bản trước).
- [ ] **Chuẩn bị Release Notes**: Liệt kê đầy đủ các tính năng mới, lỗi đã fix và cải tiến hiệu năng/UI.
- [ ] **Khởi tạo Log**: Đã tạo file nhật ký thực thi phát hành trong `Docs/logs/`.

---

## 2. Kích Hoạt Phát Hành (Deploy Triggering)

1. Tăng `versionCode` và `versionName` trong `app/build.gradle.kts`.
2. Tạo commit phát hành:
   ```bash
   git commit -am "chore(release): bump version to vX.Y.Z"
   ```
3. Tạo annotated Git tag kèm ghi chú tóm tắt:
   ```bash
   git tag -a vX.Y.Z -m "Release vX.Y.Z: ..."
   ```
4. Đẩy commit và tag lên GitHub:
   ```bash
   git push origin <branch>
   git push origin vX.Y.Z
   ```

---

## 3. Sau Khi Phát Hành (Post-Release Verification)

- [ ] **Kiểm tra GitHub Actions**: Truy cập `https://github.com/<OWNER>/<REPO>/actions`, kiểm tra workflow `Release Android App` chạy thành công.
- [ ] **Kiểm tra GitHub Releases**: Truy cập `https://github.com/<OWNER>/<REPO>/releases`, đảm bảo tag `vX.Y.Z` có bản Release và file `.apk` đính kèm.
- [ ] **Kiểm tra Auto-Update trong ứng dụng**:
  - Mở app, vào **Settings** -> Bấm **Check for Updates**.
  - Xác nhận hộp thoại hiển thị phiên bản `vX.Y.Z` cùng Release Notes chuẩn xác.
- [ ] **Đóng Log**: Cập nhật trạng thái `✅ Hoàn thành` trong `Docs/logs/`.
