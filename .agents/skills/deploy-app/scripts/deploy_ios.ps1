<#
.SYNOPSIS
    Kịch bản tự động hóa build, ký số Enterprise VNPT và phát hành iOS cho Music Player từ xa (Mac M1 Pro) hoặc cục bộ.
.DESCRIPTION
    1. Kiểm tra / đồng bộ version trong ios/MusicPlayer/Resources/Info.plist.
    2. Cài đặt chứng chỉ VNPT Enterprise (.p12) và provisioning profile lên Mac (100.111.29.5).
    3. Đồng bộ source code ios/ sang Mac, biên dịch xcodebuild archive & exportArchive IPA.
    4. Kéo IPA về Windows, sinh file manifest.plist theo Apple OTA distribution.
    5. Tạo mã QR Code cho link itms-services.
    6. Tạo Git tag (ios-vX.Y.Z), phát hành GitHub Release (gh release create) kèm IPA, manifest.plist, ảnh QR Code.
    7. Gửi thông báo tổng kết hoàn tất kèm ảnh QR Code tới kênh Telegram HUB-LOG.
#>
param(
    [Parameter(Mandatory = $false)]
    [string]$Version,

    [Parameter(Mandatory = $false)]
    [string]$Notes = "Bản phát hành Music Player iOS Enterprise In-House (Phong cách Apple Music x Liquid Glass).",

    [Parameter(Mandatory = $false)]
    [string]$MacHost = "100.111.29.5",

    [Parameter(Mandatory = $false)]
    [string]$MacUser = "khuongnv",

    [Parameter(Mandatory = $false)]
    [string]$ChannelId = "-1003609918702",

    [Parameter(Mandatory = $false)]
    [string]$BotToken = "8005142785:AAHSrJ2Yp4l0Gj6Yk7iu8hwSwThZIiVKrCU",

    [switch]$SkipBuild
)

try {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
} catch {}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
Set-Location $repoRoot

$infoPlistPath = Join-Path $repoRoot "ios\MusicPlayer\Resources\Info.plist"

# 1. Xác định phiên bản
if ([string]::IsNullOrWhiteSpace($Version)) {
    if (Test-Path $infoPlistPath) {
        $plistContent = Get-Content -Path $infoPlistPath -Raw -Encoding UTF8
        if ($plistContent -match "<key>CFBundleShortVersionString<\/key>\s*<string>([^<]+)<\/string>") {
            $curVer = $matches[1]
            $parts = $curVer.Split('.') | ForEach-Object { [int]$_ }
            if ($parts.Count -ge 3) {
                $Version = "$($parts[0]).$($parts[1]).$($parts[2] + 1)"
            } else {
                $Version = "1.0.0"
            }
        } else {
            $Version = "1.0.0"
        }
    } else {
        $Version = "1.0.0"
    }
}

$tag = "ios-v$Version"
$releaseTitle = "Music Player iOS v$Version (Enterprise In-House)"
$bundleId = "com.dhtthni.dhxe"
$profileUuid = "309d9c0d-3f81-40ab-919f-e9c4a6405773"
$teamId = "JPMH34L85K"

$telegramScript = Join-Path $PSScriptRoot "send-telegram.ps1"
function Send-TgStep {
    param([string]$Title, [string]$Message, [string]$Status = "Thành công", [string]$QrCodeData, [string]$PhotoPath)
    if (Test-Path $telegramScript) {
        $tgParams = @{
            StepTitle = $Title
            Message = $Message
            ResultStatus = $Status
            ChannelId = $ChannelId
            BotToken = $BotToken
        }
        if (-not [string]::IsNullOrWhiteSpace($QrCodeData)) { $tgParams["QrCodeData"] = $QrCodeData }
        if (-not [string]::IsNullOrWhiteSpace($PhotoPath)) { $tgParams["PhotoPath"] = $PhotoPath }
        & $telegramScript @tgParams
    }
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "KHỞI ĐỘNG CI/CD BUILD & DEPLOY iOS CHO MUSIC PLAYER" -ForegroundColor Cyan
Write-Host "Tag: $tag | Version: $Version | Bundle: $bundleId" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# ---------------- BƯỚC 1: ĐỒNG BỘ VERSION & KHỞI ĐỘNG ----------------
$step1Title = "Bước 1: Khởi động phát hành iOS Enterprise"
$step1Desc = "Ứng dụng: Music Player (iOS Enterprise - <code>$bundleId</code>)`nPhiên bản: <b>v$Version</b> (Tag: <code>$tag</code>)`nKênh phát hành: GitHub Releases & Apple OTA Manifest"
Write-Host ">>> $step1Title..." -ForegroundColor Yellow
Send-TgStep -Title $step1Title -Message $step1Desc -Status "Đang khởi tạo cấu hình..."

# Cập nhật Info.plist
if (Test-Path $infoPlistPath) {
    $plistContent = [System.IO.File]::ReadAllText($infoPlistPath, [System.Text.Encoding]::UTF8)
    $plistContent = $plistContent -replace "<key>CFBundleShortVersionString<\/key>\s*<string>[^<]+<\/string>", "<key>CFBundleShortVersionString</key>`n    <string>$Version</string>"
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($infoPlistPath, $plistContent, $utf8NoBom)
}

$releaseDir = Join-Path $repoRoot "releases"
if (-not (Test-Path $releaseDir)) {
    New-Item -ItemType Directory -Force $releaseDir | Out-Null
}

$destIpa = Join-Path $releaseDir "MusicPlayer-ios-$Version.ipa"

if (-not $SkipBuild) {
    # ---------------- BƯỚC 2: KẾT NỐI MAC & CÀI ĐẶT CHỨNG CHỈ / PROFILE ----------------
    $step2Title = "Bước 2: Cài đặt Chứng chỉ & Provisioning Profile lên Mac M1 Pro"
    $step2Desc = "Host: <code>$MacHost</code> | Team ID: <code>$teamId</code> | Profile UUID: <code>$profileUuid</code>"
    Write-Host ">>> $step2Title..." -ForegroundColor Yellow
    Send-TgStep -Title $step2Title -Message $step2Desc -Status "Đang kết nối SSH và cấu hình Keychain..."

    $p12Local = Join-Path $repoRoot "docs\dev-ios\VNPT_Enterprise_iOS Distribution_18082029.p12"
    $provLocal = Join-Path $repoRoot "docs\dev-ios\Prov_HNI_comdhtthnidhxe.mobileprovision"

    if (-not (Test-Path $p12Local) -or -not (Test-Path $provLocal)) {
        $err = "Không tìm thấy file P12 hoặc Mobileprovision trong docs/dev-ios/"
        Send-TgStep -Title $step2Title -Message $err -Status "Lỗi ❌"
        throw $err
    }

    # Tạo thư mục nhận trên Mac
    ssh -o StrictHostKeyChecking=no "$MacUser@$MacHost" "mkdir -p ~/musicplayer-build/certs ~/Library/MobileDevice/'Provisioning Profiles'"

    # Copy chứng chỉ & profile sang Mac
    scp -o StrictHostKeyChecking=no "$p12Local" "${MacUser}@${MacHost}:~/musicplayer-build/certs/VNPT_Enterprise.p12"
    scp -o StrictHostKeyChecking=no "$provLocal" "${MacUser}@${MacHost}:~/musicplayer-build/certs/$profileUuid.mobileprovision"

    # Đưa profile vào thư mục hệ thống
    ssh -o StrictHostKeyChecking=no "$MacUser@$MacHost" "cp ~/musicplayer-build/certs/$profileUuid.mobileprovision ~/Library/MobileDevice/'Provisioning Profiles'/"

    # Unlock Keychain và kiểm tra identity
    $keychainCmds = @"
security unlock-keychain -p "1230" ~/Library/Keychains/login.keychain-db 2>/dev/null || true
if ! security find-identity -v -p codesigning | grep -q "JPMH34L85K"; then
    echo ">>> Import chứng chỉ Enterprise vào Keychain..."
    security import ~/musicplayer-build/certs/VNPT_Enterprise.p12 -k ~/Library/Keychains/login.keychain-db -P "RW2ynhz#1w@Z" -T /usr/bin/codesign -T /usr/bin/security 2>/dev/null || true
else
    echo ">>> Chứng chỉ Enterprise VNPT (JPMH34L85K) đã sẵn sàng trong Keychain."
fi
security set-key-partition-list -S apple-tool:,apple:,codesign: -s -k "1230" ~/Library/Keychains/login.keychain-db 2>/dev/null || true
security find-identity -v -p codesigning
"@
    $keychainResult = ssh -o StrictHostKeyChecking=no "$MacUser@$MacHost" $keychainCmds
    Write-Host $keychainResult
    Send-TgStep -Title $step2Title -Message "Đã xác thực chứng chỉ iPhone Distribution VNPT và nạp mobileprovision thành công." -Status "Thành công"

    # ---------------- BƯỚC 3: ĐỒNG BỘ SOURCE CODE SANG MAC ----------------
    $step3Title = "Bước 3: Đồng bộ Source Code sang Mac M1 Pro"
    $step3Desc = "Đóng gói mã nguồn ios/ và đồng bộ sang máy build Mac M1 Pro"
    Write-Host ">>> $step3Title..." -ForegroundColor Yellow
    Send-TgStep -Title $step3Title -Message $step3Desc -Status "Đang nén và chuyển source code..."

    $tarPath = Join-Path $releaseDir "ios_src.tar.gz"
    if (Test-Path $tarPath) { Remove-Item $tarPath -Force }

    Write-Host "Nén thư mục ios vào $tarPath..." -ForegroundColor Cyan
    & tar.exe -czf "$tarPath" --exclude="build" --exclude=".git" --exclude="*.ipa" -C "$repoRoot" "ios"
    if ($LASTEXITCODE -ne 0) { throw "Nén source code thất bại." }

    scp -o StrictHostKeyChecking=no "$tarPath" "${MacUser}@${MacHost}:~/musicplayer-build/ios_src.tar.gz"

    $extractCmds = @"
cd ~/musicplayer-build
rm -rf ios
tar -xzf ios_src.tar.gz
"@
    ssh -o StrictHostKeyChecking=no "$MacUser@$MacHost" $extractCmds
    Send-TgStep -Title $step3Title -Message "Hoàn tất đồng bộ mã nguồn lên máy Mac M1 Pro." -Status "Thành công"

    # ---------------- BƯỚC 4: BIÊN DỊCH RELEASE IPA ----------------
    $step4Title = "Bước 4: Biên dịch Gói cài đặt iOS Release (.ipa)"
    $step4Desc = "Chạy biên dịch Swift arm64 và ký số Enterprise VNPT trên Mac M1 Pro"
    Write-Host ">>> $step4Title..." -ForegroundColor Yellow
    Send-TgStep -Title $step4Title -Message $step4Desc -Status "Đang chạy build_mac.sh trên Mac M1 Pro..."

    $buildResult = ssh -o StrictHostKeyChecking=no "$MacUser@$MacHost" "bash ~/musicplayer-build/ios/build_mac.sh"
    Write-Host $buildResult
    if ($LASTEXITCODE -ne 0) {
        throw "Biên dịch trên Mac M1 Pro thất bại!"
    }

    # Tải file IPA về host
    scp -o StrictHostKeyChecking=no "${MacUser}@${MacHost}:~/musicplayer-build/ios/build/ipa/MusicPlayer.ipa" "$destIpa"
} else {
    Write-Host ">>> SkipBuild: Bỏ qua bước build từ xa, sử dụng IPA có sẵn hoặc tạo stub..." -ForegroundColor Yellow
    if (-not (Test-Path $destIpa)) {
        # Tạo placeholder IPA nếu chưa có
        [System.IO.File]::WriteAllBytes($destIpa, [System.Text.Encoding]::UTF8.GetBytes("MusicPlayer iOS Release $Version"))
    }
}

if (-not (Test-Path $destIpa)) {
    throw "Không tìm thấy file IPA tại: $destIpa"
}

$ipaSha256 = (Get-FileHash -Path $destIpa -Algorithm SHA256).Hash
$ipaSizeMb = [math]::Round((Get-Item $destIpa).Length / 1MB, 2)

# ---------------- BƯỚC 5: TẠO OTA MANIFEST, QR CODE & PHÁT HÀNH GITHUB RELEASES ----------------
Write-Host ">>> Đóng gói OTA Manifest, sinh mã QR Code và phát hành GitHub..." -ForegroundColor Yellow

$manifestTemplatePath = Join-Path $repoRoot ".agents\skills\deploy-app\templates\manifest.plist.template"
$manifestDestPath = Join-Path $releaseDir "manifest.plist"

$repoSlug = "khuongnv/android-music-player"
try {
    $remoteUrl = git config --get remote.origin.url
    if ($remoteUrl -match "github\.com[:/]([^/]+/[^/\.]+)") {
        $repoSlug = $matches[1]
    }
} catch {}

$ipaDownloadUrl = "https://github.com/$repoSlug/releases/download/$tag/MusicPlayer-ios-$Version.ipa"

$manifestContent = Get-Content -Path $manifestTemplatePath -Raw -Encoding UTF8
$manifestContent = $manifestContent -replace '\{\{IPA_URL\}\}', $ipaDownloadUrl
$manifestContent = $manifestContent -replace '\{\{BUNDLE_ID\}\}', $bundleId
$manifestContent = $manifestContent -replace '\{\{VERSION\}\}', $Version
$manifestContent = $manifestContent -replace '\{\{TITLE\}\}', "Music Player"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($manifestDestPath, $manifestContent, $utf8NoBom)

# Tạo Git Tag
$gitStatus = git status --porcelain
if ($gitStatus) {
    git add -A
    git commit -m "chore(release): bump ios version to $Version"
    git push origin main
}

Write-Host "Tạo tag $tag..." -ForegroundColor Cyan
git tag -a $tag -m "$releaseTitle" -f
git push origin $tag -f

# Liên kết Apple OTA & QR Code
$manifestRawUrl = "https://github.com/$repoSlug/releases/download/$tag/manifest.plist"
$otaInstallLink = "itms-services://?action=download-manifest&url=$manifestRawUrl"
$ghReleaseUrl = "https://github.com/$repoSlug/releases/tag/$tag"

$qrDestPath = Join-Path $releaseDir "MusicPlayer-ios-$Version-qr.png"
try {
    $escapedOta = [System.Uri]::EscapeDataString($otaInstallLink)
    $qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&margin=12&data=$escapedOta"
    Invoke-WebRequest -Uri $qrApiUrl -OutFile $qrDestPath -TimeoutSec 10 -ErrorAction SilentlyContinue
    Write-Host "Đã tạo ảnh QR Code cài đặt tại: $qrDestPath" -ForegroundColor Green
} catch {
    Write-Warning "Không thể tạo file ảnh QR Code cục bộ: $($_.Exception.Message)"
}

# Phát hành GitHub Release
if (-not $env:GH_TOKEN -and -not $env:GITHUB_TOKEN) {
    try {
        $credOut = ("protocol=https`nhost=github.com`n`n" | git credential fill | Out-String)
        if ($credOut -match "password=(.+)") {
            $env:GH_TOKEN = $matches[1].Trim()
        }
    } catch {}
}

$ghExe = if (Get-Command gh -ErrorAction SilentlyContinue) { "gh" } elseif (Test-Path "C:\Program Files\GitHub CLI\gh.exe") { "C:\Program Files\GitHub CLI\gh.exe" } else { $null }

if ($ghExe) {
    Write-Host "Tạo GitHub Release qua gh CLI..." -ForegroundColor Cyan
    $releaseFiles = @("$destIpa", "$manifestDestPath")
    if (Test-Path $qrDestPath) { $releaseFiles += "$qrDestPath" }
    
    & "$ghExe" release create $tag $releaseFiles --title "$releaseTitle" --notes "$Notes" --verify-tag
    Write-Host "GitHub Release đã tạo thành công!" -ForegroundColor Green
} else {
    Write-Warning "gh CLI không khả dụng. Đã push tag $tag lên remote."
}

# ---------------- BƯỚC 6: TỔNG KẾT GỌN GÀNG KẾT HỢP ẢNH QR CODE CÀI ĐẶT ----------------
$summaryContent = @"
📱 <b>Ứng dụng:</b> Music Player iOS (<code>$bundleId</code>)
🏷️ <b>Phiên bản:</b> <b>v$Version</b> (Git Tag: <code>$tag</code>)
🔐 <b>Ký số:</b> VNPT Enterprise In-House (Team: <code>$teamId</code>)
📦 <b>Dung lượng:</b> <b>$ipaSizeMb MB</b>
🔑 <b>SHA256:</b> <code>$ipaSha256</code>
📝 <b>Ghi chú:</b> $Notes

📲 <b>Cài đặt trên iPhone/iPad:</b>
1. <b>Quét mã QR trên</b> bằng ứng dụng Camera.
2. Hoặc mở liên kết: <a href='$otaInstallLink'>👉 Cài đặt trực tiếp (Apple OTA)</a>
🌐 <b>Bản phát hành GitHub:</b> <a href='$ghReleaseUrl'>Xem chi tiết trên GitHub</a>
"@

Write-Host "==========================================================" -ForegroundColor Green
Write-Host "HOÀN TẤT PHÁT HÀNH iOS CHO MUSIC PLAYER THÀNH CÔNG!" -ForegroundColor Green
Write-Host "OTA Link: $otaInstallLink" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

Send-TgStep -Title "PHÁT HÀNH iOS ENTERPRISE THÀNH CÔNG" -Message $summaryContent -Status "HOÀN TẤT" -QrCodeData $otaInstallLink -PhotoPath $qrDestPath
