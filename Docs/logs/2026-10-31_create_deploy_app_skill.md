# 📋 Nhật Ký Thực Thi: Tạo Skill deploy-app Tự Động Phát Hành Phiên Bản Mới

- **Ngày thực hiện**: 2026-10-31 11:33:18
- **Agent phụ trách**: `lead-architect`, `qa-perf-specialist`
- **Yêu cầu gốc**: Tạo skill deploy-app khi phát hành version app mới và tự thực hiện việc đẩy Release cùng với note đầy đủ.
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)

### 1.1. Mục tiêu
- Xây dựng một Antigravity Skill chuyên nghiệp mang tên `deploy-app` đặt tại `.agents/skills/deploy-app/`.
- Cung cấp kịch bản tự động hóa (`scripts/deploy.ps1`) thực hiện trọn vẹn quy trình:
  1. Kiểm tra mã nguồn và biên dịch (`compileDebugKotlin`).
  2. Tự động đọc và nâng số hiệu phiên bản (`versionCode`, `versionName`) trong `app/build.gradle.kts` theo chuẩn Semantic Versioning (`patch`, `minor`, `major`).
  3. Tự động trích xuất lịch sử commit từ Git tag trước đó và cấu trúc thành Release Notes định dạng Apple Music (Tính năng mới, UI/UX, Sửa lỗi, Hiệu năng).
  4. Thực hiện `git commit` và tạo annotated Git tag `vX.Y.Z` với Release Notes.
  5. Đẩy commit và tag lên GitHub (`git push origin <branch>` và `git push origin vX.Y.Z`), qua đó tự động kích hoạt GitHub Actions (`.github/workflows/release.yml`) để build Release APK và xuất bản bản cập nhật.
  6. Tự động cập nhật nhật ký thực thi truy vết trong `Docs/logs/`.

### 1.2. Kế hoạch triển khai
1. Khởi tạo nhật ký qua `scripts/init_log.ps1`.
2. Tạo cấu trúc thư mục `.agents/skills/deploy-app/` (`scripts/`, `resources/`, `references/`).
3. Tạo mẫu Release Notes chuẩn tại `resources/release_notes_template.md`.
4. Tạo bảng kiểm tra phát hành tại `references/release_checklist.md`.
5. Viết script tự động hóa PowerShell `scripts/deploy.ps1` hỗ trợ các tham số: `-BumpType`, `-CustomVersion`, `-ReleaseTitle`, `-ReleaseNotes`, `-DryRun`, `-SkipTest`, `-SkipPush`.
6. Soạn thảo tệp chỉ thị `SKILL.md` chuẩn YAML frontmatter.
7. Liên kết quy trình phát hành vào `AGENTS.md`.
8. Chạy kiểm thử mô phỏng (`-DryRun`).

---

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)

- **Bước 1 (Khởi tạo nhật ký)**:
  - Sinh tệp log `Docs/logs/2026-10-31_create_deploy_app_skill.md` và đăng ký vào `Docs/logs/README.md`.
- **Bước 2 (Xây dựng Template Release Notes & Checklist)**:
  - Tạo `resources/release_notes_template.md`: Phân chia rõ ràng các mục: ✨ Tính Năng Mới, 🎨 UI/UX Apple Music, 🐛 Sửa Lỗi, ⚡ Tối Ưu Pin/Hiệu Năng.
  - Tạo `references/release_checklist.md`: Các điều kiện nghiệm thu trước và sau khi kích hoạt deploy.
- **Bước 3 (Xây dựng Script Tự Động deploy.ps1)**:
  - Đọc `versionCode` và `versionName` từ `app/build.gradle.kts`.
  - Hỗ trợ bump tự động theo `patch` (+0.0.1), `minor` (+0.1.0), `major` (+1.0.0), hoặc `custom`.
  - Tự động quét `git log` từ tag gần nhất đến `HEAD` để gom nhóm các commit vào Release Notes.
  - Hỗ trợ chế độ an toàn `-DryRun` và tự động kiểm tra `compileDebugKotlin` trước khi tag.
  - Thực hiện tạo tag và push lên remote repo.
- **Bước 4 (Soạn thảo SKILL.md)**:
  - Biên soạn quy trình 5 bước: Pre-flight check -> Tạo log -> Chạy deploy script -> Kiểm tra CI/CD -> Đóng log & Báo cáo.
- **Bước 5 (Cập nhật AGENTS.md)**:
  - Bổ sung quy định điều phối phát hành liên kết tới Skill `deploy-app`.
- **Bước 6 (Kiểm thử mô phỏng - Dry Run)**:
  - Chạy lệnh: `powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -BumpType patch -DryRun -SkipTest`.
  - Kết quả: Đọc đúng phiên bản hiện tại `1.0.0 (Build 1)`, tính toán chính xác phiên bản mới `1.0.1 (Build 2)`, Git Tag `v1.0.1` và sinh Release Notes thành công.

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)

| Thao tác | Đường dẫn tệp | Mô tả nội dung |
| :--- | :--- | :--- |
| `[Created]` | [`.agents/skills/deploy-app/SKILL.md`](../../.agents/skills/deploy-app/SKILL.md) | Chỉ thị chính của Skill deploy-app |
| `[Created]` | [`.agents/skills/deploy-app/scripts/deploy.ps1`](../../.agents/skills/deploy-app/scripts/deploy.ps1) | Script PowerShell tự động hóa nâng version, sinh notes, tạo tag và push |
| `[Created]` | [`.agents/skills/deploy-app/resources/release_notes_template.md`](../../.agents/skills/deploy-app/resources/release_notes_template.md) | Khung mẫu Release Notes chuẩn phân loại theo tính năng và UI |
| `[Created]` | [`.agents/skills/deploy-app/references/release_checklist.md`](../../.agents/skills/deploy-app/references/release_checklist.md) | Bảng kiểm tra trước và sau khi kích hoạt deploy |
| `[Modified]` | [`AGENTS.md`](../../AGENTS.md) | Bổ sung quy định kích hoạt Skill deploy-app |
| `[Modified]` | [`Docs/logs/README.md`](README.md) | Cập nhật chỉ mục danh mục log hoàn thành |
| `[Modified]` | [`Docs/logs/2026-10-31_create_deploy_app_skill.md`](./2026-10-31_create_deploy_app_skill.md) | Hoàn thiện toàn bộ nội dung nhật ký thực thi |

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)

- **Kiểm tra script**:
  - Đã chạy thử nghiệm `-DryRun` thành công.
  - Kiểm tra thuật toán SemVer: Tăng `patch` từ `1.0.0` lên `1.0.1`, `versionCode` từ `1` lên `2`.
  - Mã hóa UTF-8 với BOM đảm bảo không lỗi font tiếng Việt trên PowerShell Windows.
- **Tích hợp CI/CD**:
  - Git tag `v*` được đẩy lên GitHub sẽ tự động kích hoạt workflow `.github/workflows/release.yml`, đóng gói APK và tạo GitHub Release tương thích với tính năng Auto-Update trong ứng dụng.

---

## 5. Kết Luận & Hướng Dẫn Tiếp Theo (Conclusion & Next Steps)

- Skill `deploy-app` đã sẵn sàng hoạt động trong hệ thống Swarm Agent.
- Bất cứ khi nào cần phát hành phiên bản mới, chỉ cần yêu cầu:
  > *"Hãy phát hành phiên bản mới (patch/minor)"*  
  Swarm Agent sẽ tự động chạy quy trình kiểm tra, nâng số hiệu phiên bản, sinh Release Notes và đẩy tag lên GitHub!
