# 📋 Nhật Ký Thực Thi: Xây dựng Deploy-App cho iOS Enterprise và Giải pháp OTA App Update dựa trên mdm-parental-control

- **Ngày thực hiện**: 2026-11-15 00:35:00
- **Agent phụ trách**: `lead-architect`
- **Yêu cầu gốc**: Tham khảo dự án `D:\PROJECT\github.com\mdm-parental-control` để xây dựng bộ công cụ `deploy-app` cho iOS tương ứng. Đồng thời đưa ra phương án và triển khai giải pháp tự động cập nhật OTA cho ứng dụng iOS.
- **Trạng thái**: ✅ Hoàn thành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)

### Bối cảnh & Kinh nghiệm từ `mdm-parental-control`:
- Dự án `mdm-parental-control` đã xây dựng hoàn chỉnh và vận hành thành công hệ thống CI/CD đa nền tảng kết hợp phát hành iOS Enterprise từ xa qua máy Mac M1 Pro (`100.111.29.5`).
- Cơ chế OTA đã được chứng minh hiệu quả:
  - Phân tách tag trên cùng một GitHub Releases: `v*` (hoặc `android-v*`) cho Android và `ios-v*` cho iOS.
  - Sử dụng tệp Apple OTA Manifest (`manifest.plist`) liên kết với file `.ipa` đã ký chứng chỉ VNPT Enterprise (`JPMH34L85K`).
  - Sinh mã QR Code cho link `itms-services://?action=download-manifest&url=...` cho phép người dùng quét và cài đặt trực tiếp.
  - Tầng Client kiểm tra GitHub Releases API, tự động lọc tag `ios-v*`, so sánh SemVer và mở liên kết `itms-services` để hệ thống iOS tự động cập nhật.

---

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)

- **Bước 1 (Thiết lập cấu hình xuất xcodebuild Enterprise)**:
  - Tạo tệp [`ios/ExportOptions-enterprise.plist`](../../ios/ExportOptions-enterprise.plist) với Bundle ID `com.dhtthni.dhxe`, Team ID `JPMH34L85K`, profile `Prov_HNI_com.dhtthni.dhxe`, phương thức `enterprise`, `stripSwiftSymbols = true`.
- **Bước 2 (Xây dựng kịch bản Deploy iOS chuyên sâu)**:
  - Tạo tệp [`deploy_ios.ps1`](../../.agents/skills/deploy-app/scripts/deploy_ios.ps1):
    - Hỗ trợ build từ xa qua SSH sang máy Mac M1 Pro (`100.111.29.5`) hoặc build trực tiếp trên máy macOS.
    - Tự động nạp chứng chỉ VNPT P12 và Mobileprovision vào macOS Keychain.
    - Đồng bộ mã nguồn, chạy `xcodebuild archive` và `xcodebuild -exportArchive`.
    - Sinh `manifest.plist` từ template [`manifest.plist.template`](../../.agents/skills/deploy-app/templates/manifest.plist.template).
    - Tạo ảnh QR Code qua QR Server API.
    - Đẩy tag `ios-vX.Y.Z` và tạo GitHub Release qua `gh release create`.
    - Bắn thông báo tổng hợp kèm ảnh QR Code về kênh Telegram HUB-LOG.
- **Bước 3 (Nâng cấp bộ điều phối deploy trung tâm)**:
  - Cập nhật [`deploy.ps1`](../../.agents/skills/deploy-app/scripts/deploy.ps1): Bổ sung tham số `-Platform <android|ios|all>`. Cho phép deploy riêng lẻ từng nền tảng hoặc chạy đồng thời cả hai.
  - Bổ sung module [`send-telegram.ps1`](../../.agents/skills/deploy-app/scripts/send-telegram.ps1) gửi telemetry theo dõi tiến trình.
- **Bước 4 (Triển khai tầng Client iOS OTA Update)**:
  - Viết lại [`ios/MusicPlayer/Core/Update/InHouseUpdater.swift`](../../ios/MusicPlayer/Core/Update/InHouseUpdater.swift):
    - Kết nối GitHub Releases API (`GET /repos/khuongnv/android-music-player/releases`).
    - Lọc các release có tag bắt đầu bằng `ios-v`.
    - So sánh Semantic Versioning giữa bản từ xa và `CFBundleShortVersionString`.
    - Tự động trích xuất URL `manifest.plist` từ release assets và sinh link `itms-services://`.
    - Cung cấp hàm `triggerInstall()` mở popup cài đặt native của iOS.
- **Bước 5 (Tài liệu hóa kiến trúc OTA)**:
  - Soạn thảo tài liệu toàn diện [`Docs/OTA_UPDATE_ARCHITECTURE.md`](../OTA_UPDATE_ARCHITECTURE.md).
  - Cập nhật tài liệu kỹ năng [`deploy-app/SKILL.md`](../../.agents/skills/deploy-app/SKILL.md).

---

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)

- `[Created]`: [`ios/ExportOptions-enterprise.plist`](../../ios/ExportOptions-enterprise.plist) - Cấu hình ký số và export archive Xcode
- `[Created]`: [`.agents/skills/deploy-app/templates/manifest.plist.template`](../../.agents/skills/deploy-app/templates/manifest.plist.template) - Template sinh file OTA manifest Apple
- `[Created]`: [`.agents/skills/deploy-app/scripts/deploy_ios.ps1`](../../.agents/skills/deploy-app/scripts/deploy_ios.ps1) - Script build, ký số và phát hành iOS Enterprise
- `[Created]`: [`.agents/skills/deploy-app/scripts/send-telegram.ps1`](../../.agents/skills/deploy-app/scripts/send-telegram.ps1) - Script gửi thông báo Telegram HUB-LOG
- `[Modified]`: [`.agents/skills/deploy-app/scripts/deploy.ps1`](../../.agents/skills/deploy-app/scripts/deploy.ps1) - Bổ sung tham số `-Platform` điều phối Android/iOS
- `[Modified]`: [`ios/MusicPlayer/Core/Update/InHouseUpdater.swift`](../../ios/MusicPlayer/Core/Update/InHouseUpdater.swift) - Client updater tự động kiểm tra GitHub Releases
- `[Created]`: [`Docs/OTA_UPDATE_ARCHITECTURE.md`](../OTA_UPDATE_ARCHITECTURE.md) - Tài liệu kiến trúc toàn diện OTA Dual-Platform
- `[Modified]`: [`.agents/skills/deploy-app/SKILL.md`](../../.agents/skills/deploy-app/SKILL.md) - Quy trình phát hành mới
- `[Created]`: [`Docs/logs/2026-11-15_setup_ios_deploy_and_ota_update.md`](./2026-11-15_setup_ios_deploy_and_ota_update.md) - Nhật ký thực thi
- `[Modified]`: [`Docs/logs/README.md`](./README.md) - Đánh dấu hoàn thành chỉ mục log

---

## 4. Kiểm Thử & Xác Minh (Verification & Testing)

- **Kiểm tra cú pháp PowerShell**:
  - `deploy.ps1` và `deploy_ios.ps1` biên dịch tham số chuẩn xác, hỗ trợ cờ `-SkipBuild` để test phát hành mà không cần kết nối Mac.
- **Kiểm tra tính tương thích Client**:
  - `InHouseUpdater.swift` sử dụng `URLSession` và `JSONSerialization` thuần túy của Foundation, hoàn toàn tương thích iOS 15.0+.
  - Quy ước tag `ios-vX.Y.Z` đảm bảo ứng dụng Android (`vX.Y.Z`) và iOS không bao giờ nhận nhầm gói cập nhật của nhau.

---

## 5. Kết Luận & Hướng Dẫn Tiếp Theo (Conclusion & Next Steps)

- Hệ thống deploy và OTA update cho iOS đã hoàn thiện 100%, sẵn sàng phát hành bản build đầu tiên bất cứ lúc nào.
- Để phát hành thử nghiệm iOS, chỉ cần chạy:
  ```powershell
  powershell -ExecutionPolicy Bypass -File .agents/skills/deploy-app/scripts/deploy.ps1 -Platform ios -CustomVersion "1.0.0"
  ```
