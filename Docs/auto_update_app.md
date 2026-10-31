# Hướng Dẫn Kỹ Thuật: Dọn Dẹp Release & Triển Khai Tính Năng Auto-Update Qua GitHub Releases

Tài liệu này đóng vai trò là đặc tả kỹ thuật (Technical Specification & Prompt Context) dành cho các AI Agent hoặc lập trình viên thực hiện hai nhiệm vụ:
1. **Dọn dẹp triệt để** các Release và Git Tag cũ bị lộn xộn trên GitHub repository.
2. **Thiết lập quy chuẩn Versioning** và **triển khai tính năng Auto-Update** (tự động kiểm tra, tải về và cập nhật ứng dụng).

---

## PHẦN 1: DỌN DẸP RELEASES VÀ TAGS CŨ

Trước khi chuẩn hóa quy trình phân phối mới, cần loại bỏ toàn bộ release và git tag không đúng chuẩn hiện tại.

### Cách 1: Sử dụng GitHub CLI (`gh`) (Khuyến nghị - Nhanh và sạch nhất)

Yêu cầu cài đặt GitHub CLI và đăng nhập (`gh auth login`). Chạy lần lượt các lệnh sau trên terminal tại thư mục gốc của repository:

```bash
# 1. Xóa toàn bộ Releases hiện có trên GitHub
gh release list --limit 100 --json tagName -q ".[].tagName" | while read -r tag; do
    echo "Đang xóa Release: $tag"
    gh release delete "$tag" --yes --cleanup-tag
done

# 2. Xóa các Git Tag còn sót lại trên Remote (GitHub)
git fetch --tags
git tag -l | while read -r tag; do
    echo "Đang xóa Remote Tag: $tag"
    git push --delete origin "$tag"
done

# 3. Xóa toàn bộ Local Tags trên máy
git tag -d $(git tag -l)
```

### Cách 2: Xóa thủ công qua GitHub Web UI
1. Truy cập: `https://github.com/<OWNER>/<REPO>/releases`
2. Bấm vào từng bản release cũ -> Chọn **Edit** (biểu tượng bút chì) -> Chọn **Delete this release**.
3. Chuyển sang tab **Tags** (`https://github.com/<OWNER>/<REPO>/tags`) -> Bấm biểu tượng 3 chấm `...` -> Chọn **Delete tag**.
4. Trên máy local, dọn dẹp cache tag:
   ```bash
   git fetch --prune --prune-tags
   ```

---

## PHẦN 2: QUY CHUẨN ĐẶT VERSION & RELEASE ASSETS

### 1. Chuẩn Semantic Versioning (SemVer)
Bắt đầu lại từ phiên bản nền tảng đầu tiên: `v1.0.0`
* **Format**: `v<MAJOR>.<MINOR>.<PATCH>` (ví dụ: `v1.0.0`, `v1.0.1`, `v1.1.0`)
  * `MAJOR`: Thay đổi lớn phá vỡ tính tương thích ngược (breaking changes).
  * `MINOR`: Bổ sung tính năng mới, tương thích ngược.
  * `PATCH`: Vá lỗi (bug fixes), tương thích ngược.

### 2. Quy chuẩn đặt tên Asset đính kèm Release
Tên file binary/installer đính kèm cần nhất quán để code Auto-Update có thể tìm kiếm regex hoặc pattern chính xác:

* **Windows**:
  * Installer: `<app_name>-<version>-setup.exe` (ví dụ: `MyApp-v1.0.0-setup.exe`)
  * Portable: `<app_name>-<version>-windows-x64.zip`
* **macOS**:
  * `<app_name>-<version>-macos-universal.dmg`
* **Linux**:
  * `<app_name>-<version>-linux-x86_64.AppImage`

---

## PHẦN 3: ĐẶC TẢ LUỒNG LOGIC AUTO-UPDATE (CLIENT SPEC)

Mọi module Auto-Update triển khai trong ứng dụng phải tuân theo luồng logic sau:

```
+-------------------------------------------------------------+
|                     Client App Khởi động                    |
+-------------------------------------------------------------+
                              │
                              ▼
        [Kiểm tra cờ Check Update: Tự động hoặc Thủ công]
                              │
                              ▼
       [Gửi GET request tới GitHub Releases API Endpoint]
                              │
               ┌──────────────┴──────────────┐
               ▼                             ▼
       [HTTP 200 OK]                 [HTTP Lỗi / Rate Limit]
               │                             │
               ▼                             ▼
   [Parse JSON: 'tag_name']         [Ghi Log, bỏ qua/báo lỗi nhẹ]
               │
               ▼
   [So sánh: Remote Version vs Current App Version]
               │
        ┌──────┴─────────────────────────────────┐
        ▼                                        ▼
[Remote <= Current]                      [Remote > Current]
        │                                        │
        ▼                                        ▼
[Không làm gì/Báo mới nhất]          [Lấy link Asset tương ứng OS]
                                                 │
                                                 ▼
                                     [Hiển thị Dialog xác nhận]
                                     (Tùy chọn: Bắt buộc/Tự nguyện)
                                                 │
                                                 ▼
                                     [Tải file vào thư mục Temp]
                                                 │
                                                 ▼
                                     [Chạy Installer & Thoát App]
```

### 1. Endpoint tra cứu phiên bản mới nhất
* **Method**: `GET`
* **URL**: `https://api.github.com/repos/<OWNER>/<REPO>/releases/latest`
* **Headers bắt buộc**:
  ```http
  User-Agent: <AppName>-Updater
  Accept: application/vnd.github.v3+json
  ```
  *(Lưu ý: GitHub API bắt buộc phải có header `User-Agent`, nếu thiếu request sẽ bị từ chối 403 Forbidden).*

### 2. Định dạng dữ liệu cần trích xuất từ Response
Agent chỉ cần quan tâm các trường sau trong JSON trả về:

```json
{
  "tag_name": "v1.0.1",
  "name": "Bản phát hành v1.0.1",
  "body": "- Sửa lỗi đăng nhập\n- Cải thiện tốc độ khởi động",
  "published_at": "2026-03-31T04:00:00Z",
  "assets": [
    {
      "name": "MyApp-v1.0.1-setup.exe",
      "browser_download_url": "https://github.com/<OWNER>/<REPO>/releases/download/v1.0.1/MyApp-v1.0.1-setup.exe",
      "size": 45219840
    }
  ]
}
```

### 3. Logic so sánh phiên bản (SemVer Comparison)
1. Loại bỏ tiền tố `v` hoặc `V` ở `tag_name` và `current_version`.
2. Tách chuỗi thành mảng các số nguyên `[major, minor, patch]`.
3. So sánh theo thứ tự:
   - Nếu `remote.major > local.major` $\rightarrow$ Có bản cập nhật mới.
   - Nếu `remote.major == local.major` và `remote.minor > local.minor` $\rightarrow$ Có bản cập nhật mới.
   - Nếu `remote.major == local.major` và `remote.minor == local.minor` và `remote.patch > local.patch` $\rightarrow$ Có bản cập nhật mới.
   - Ngược lại $\rightarrow$ Ứng dụng đang ở phiên bản mới nhất.

### 4. Logic tải và thực thi cài đặt
1. Tạo thư mục tạm thời: `%TEMP%/<app_name>_update/` (trên Windows) hoặc `/tmp/<app_name>_update/` (Linux/macOS).
2. Tải file từ `browser_download_url` với progress bar (tiến trình tải).
3. Sau khi tải hoàn tất:
   * **Windows (EXE installer)**:
     - Chạy process installer (ví dụ: `MyApp-v1.0.1-setup.exe /SILENT` hoặc chạy bình thường để user bấm Next).
     - Đóng ứng dụng hiện tại (`exit(0)`) để installer có thể ghi đè file.
   * **Portable / Zip**:
     - Cần một file script trung gian (ví dụ `.bat` trên Windows) để: Đợi process chính tắt -> Giải nén ghi đè -> Khởi chạy lại process chính.

---

## PHẦN 4: THIẾT LẬP TỰ ĐỘNG PHÁT HÀNH BẰNG GITHUB ACTIONS (CI/CD)

Để tránh việc release thủ công gây sai lệch quy chuẩn, tạo file workflow tại `.github/workflows/release.yml`:

```yaml
name: Release App

on:
  push:
    tags:
      - 'v*'

jobs:
  build-and-release:
    runs-on: windows-latest # Đổi sang ubuntu-latest nếu là Linux

    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      # BƯỚC BUILD APP TÙY THEO NGÔN NGỮ (Điền lệnh build của app tại đây)
      # Ví dụ:
      # - name: Setup Node / .NET / Rust / Python
      #   run: ...
      # - name: Build App
      #   run: ...

      - name: Create GitHub Release & Upload Asset
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ github.ref_name }}
          name: "Release ${{ github.ref_name }}"
          generate_release_notes: true
          draft: false
          prerelease: false
          files: |
            path/to/build/output/MyApp-*-setup.exe
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

---

## PHẦN 5: CHỈ THỊ DÀNH CHO AI AGENT (IMPLEMENTATION INSTRUCTIONS)

Khi agent được giao nhiệm vụ tích hợp tính năng này vào codebase:

1. **Xác định ngôn ngữ & framework của project**:
   - Nếu là **C# (.NET / WPF / WinForms)**: Sử dụng `HttpClient` + `System.Version` hoặc thư viện `Velopack` / `AutoUpdater.NET`.
   - Nếu là **Electron**: Sử dụng `electron-updater` cấu hình `provider: "github"`.
   - Nếu là **Rust / Tauri**: Sử dụng plugin official `@tauri-apps/plugin-updater`.
   - Nếu là **Python (PyQt/Tkinter)**: Triển khai class `GithubUpdater` bằng `requests` hoặc `urllib.request`.
2. **Cấu hình Repo**:
   - Khai báo rõ hằng số cấu hình:
     ```
     GITHUB_OWNER = "<owner>"
     GITHUB_REPO  = "<repo>"
     CURRENT_VERSION = "1.0.0"
     ```
3. **Xử lý ngoại lệ (Error Handling)**:
   - Không được làm crash app chính nếu GitHub API bị lỗi mạng hoặc bị Rate Limit (403).
   - Kiểm tra mã hash (SHA256) của file sau khi tải nếu release có đính kèm file checksum để đảm bảo tính toàn vẹn.