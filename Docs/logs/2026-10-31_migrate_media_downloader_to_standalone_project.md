# 📋 Nhật Ký Thực Thi: Chuyển Giao & Tách Project Tải Nhạc Sang `download-ytb-mp3`

- **Ngày thực hiện**: 2026-10-31 21:44:00
- **Agent phụ trách**: `lead-architect` & `storage-data-specialist`
- **Yêu cầu gốc**: Chuyển toàn bộ việc download và thư mục nhạc đã tải sang `D:\PROJECT\github.com\download-ytb-mp3` để tổ chức thành project chuyên quản về download; tạo sẵn quy trình làm việc (SOP, AGENTS.md, Skill, scripts) để phục vụ cho các lần tải sau.
- **Trạng thái**: Hoàn thành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)
- **Mục tiêu**:
  - Giữ cho repository `android-music-player` thuần khiết về mã nguồn ứng dụng Android (Clean Architecture + Compose + Media3), không bị lẫn các công cụ tải ngoại vi (`ffmpeg.exe`, `yt-dlp.exe`) và dữ liệu âm thanh dung lượng lớn (~1.9 GB).
  - Tách thư mục `D:\PROJECT\github.com\download-ytb-mp3` thành một dự án độc lập, có quy trình chuẩn (SOP), các script CLI tự động hóa, tài liệu hướng dẫn và Antigravity Skill để bất kỳ agent nào cũng có thể tải, làm giàu metadata và tạo playlist nhanh chóng.

---

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)

- **Bước 1: Di chuyển dữ liệu & bộ công cụ**:
  - Di chuyển toàn bộ thư mục `Tuyệt thế dược thần/` (~1.9 GB) từ `android-music-player` sang `download-ytb-mp3/Tuyệt thế dược thần/`.
  - Di chuyển `ffmpeg.exe` và `yt-dlp.exe` sang `download-ytb-mp3/`.
- **Bước 2: Khởi tạo kiến trúc dự án `download-ytb-mp3`**:
  - Khởi tạo Git repository (`git init -b main`).
  - Thiết lập `.gitignore` loại trừ các tệp nhị phân (`.exe`), tệp âm thanh (`.mp3`), ảnh và thư mục tạm.
  - Tạo `package.json` định nghĩa các lệnh rút gọn (`npm run download`, `npm run batch`, `npm run tag`, `npm run playlist`).
- **Bước 3: Xây dựng bộ công cụ tự động hóa (`scripts/`)**:
  - `scripts/download_single.js`: Tải 1 tập/bài hát, tự động trích xuất thumbnail làm ảnh bìa vuông 800x800, nhúng thẻ ID3v2.3 (Title, Artist, Album, Track, Genre, Year).
  - `scripts/download_batch.js`: Tải hàng loạt theo danh sách JSON và tự động cập nhật playlist M3U khi kết thúc.
  - `scripts/tag_folder.js`: Nhúng thẻ ID3v2 và ảnh bìa cho các thư mục đã có file MP3 sẵn.
  - `scripts/make_playlist.js`: Tạo danh sách phát `.m3u` chuẩn UTF-8, sắp xếp số tự nhiên.
  - `scripts/pipeline.js`: Trung tâm điều phối CLI.
- **Bước 4: Chuẩn hóa tài liệu & Antigravity Skill**:
  - `Docs/WORKFLOW.md`: Quy trình 5 bước tải nhạc chất lượng cao chuẩn công nghiệp.
  - `AGENTS.md`: Quy tắc hoạt động của AI Agent trong dự án.
  - `.agents/skills/youtube-audio-pipeline/SKILL.md`: Skill tích hợp sẵn cho Antigravity Swarm.
  - `README.md`: Hướng dẫn sử dụng và cấu trúc tổng quan.

---

## 3. Danh Sách Tệp & Vị Trí Lưu Trữ

- **Dự án `android-music-player`**:
  - Đã được dọn dẹp sạch sẽ, không còn file nhị phân hay tệp audio chiếm dung lượng.
- **Dự án mới `D:\PROJECT\github.com\download-ytb-mp3`**:
  - `[Created]`: `package.json`, `.gitignore`, `README.md`, `AGENTS.md`
  - `[Created]`: `Docs/WORKFLOW.md`
  - `[Created]`: `scripts/download_single.js`, `scripts/download_batch.js`, `scripts/tag_folder.js`, `scripts/make_playlist.js`, `scripts/pipeline.js`
  - `[Created]`: `.agents/skills/youtube-audio-pipeline/SKILL.md`
  - `[Moved]`: `ffmpeg.exe`, `yt-dlp.exe`
  - `[Moved]`: `Tuyệt thế dược thần/` (19 tập MP3 + cover.jpg + Tuyệt thế dược thần.m3u)

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)
- Kiểm tra tính toàn vẹn của thư mục `Tuyệt thế dược thần` tại vị trí mới: 19 file MP3, ảnh bìa `cover.jpg` và `Tuyệt thế dược thần.m3u` hoạt động bình thường.
- Kiểm tra Git commit trên `download-ytb-mp3`: commit khởi tạo thành công (11 files, clean working tree).
- Kiểm tra Git status trên `android-music-player`: working tree sạch sẽ.

---

## 5. Kết Luận & Hướng Dẫn Tiếp Theo (Conclusion & Next Steps)
- Người dùng khi cần tải thêm tập (ví dụ từ tập 121 trở đi) chỉ cần yêu cầu trong `download-ytb-mp3` hoặc chạy lệnh:
  `node scripts/download_single.js --url "..." --dir "Tuyệt thế dược thần" --filename "Tập 121.mp3" ...`
- Thư mục nhạc đã sẵn sàng để copy vào thiết bị Android hoặc máy nghe nhạc bất cứ lúc nào.
