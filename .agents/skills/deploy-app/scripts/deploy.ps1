param (
    [ValidateSet("patch", "minor", "major", "custom")]
    [string]$BumpType = "patch",

    [string]$CustomVersion = "",
    [string]$ReleaseTitle = "",
    [string]$ReleaseNotes = "",
    [switch]$DryRun,
    [switch]$SkipPush,
    [switch]$SkipTest
)

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
$buildGradlePath = Join-Path -Path $repoRoot -ChildPath "app\build.gradle.kts"

if (-not (Test-Path $buildGradlePath)) {
    Write-Error "Không tìm thấy app/build.gradle.kts tại: $buildGradlePath"
    exit 1
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   QUY TRINH PHAT HANH PHIEN BAN MOI     " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Đọc phiên bản hiện tại
$gradleContent = [System.IO.File]::ReadAllText($buildGradlePath, [System.Text.Encoding]::UTF8)

$codeMatch = [regex]::Match($gradleContent, 'versionCode\s*=\s*(\d+)')
$nameMatch = [regex]::Match($gradleContent, 'versionName\s*=\s*"([^"]+)"')

if (-not $codeMatch.Success -or -not $nameMatch.Success) {
    Write-Error "Không thể trích xuất versionCode hoặc versionName từ build.gradle.kts"
    exit 1
}

$currentCode = [int]$codeMatch.Groups[1].Value
$currentVersion = $nameMatch.Groups[1].Value
$newCode = $currentCode + 1

# 2. Tính toán phiên bản mới (SemVer)
$parts = $currentVersion.Split('.')
$major = if ($parts.Length -ge 1) { [int]$parts[0] } else { 1 }
$minor = if ($parts.Length -ge 2) { [int]$parts[1] } else { 0 }
$patch = if ($parts.Length -ge 3) { [int]$parts[2] } else { 0 }

$newVersion = ""
switch ($BumpType) {
    "patch" {
        $patch++
        $newVersion = "$major.$minor.$patch"
    }
    "minor" {
        $minor++
        $patch = 0
        $newVersion = "$major.$minor.$patch"
    }
    "major" {
        $major++
        $minor = 0
        $patch = 0
        $newVersion = "$major.$minor.$patch"
    }
    "custom" {
        if ([string]::IsNullOrWhiteSpace($CustomVersion)) {
            Write-Error "Vui lòng cung cấp -CustomVersion khi chọn BumpType = custom"
            exit 1
        }
        $newVersion = $CustomVersion.TrimStart('v', 'V')
    }
}

$tag = "v$newVersion"
Write-Host "Phiên bản hiện tại : $currentVersion (Build $currentCode)" -ForegroundColor Gray
Write-Host "Phiên bản mới      : $newVersion (Build $newCode)" -ForegroundColor Green
Write-Host "Git Tag phát hành  : $tag" -ForegroundColor Green

# 3. Kiểm tra kiểm thử / biên dịch trước khi deploy
if (-not $SkipTest) {
    Write-Host "`n[1/5] Đang kiểm tra biên dịch (compileDebugKotlin)..." -ForegroundColor Yellow
    $gradlewBat = Join-Path -Path $repoRoot -ChildPath "gradlew.bat"
    if (Test-Path $gradlewBat) {
        & $gradlewBat compileDebugKotlin
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Biên dịch thất bại! Hủy bỏ phát hành để tránh đưa lỗi lên release."
            exit $LASTEXITCODE
        }
        Write-Host "Biên dịch thành công!" -ForegroundColor Green
    } else {
        Write-Host "Không tìm thấy gradlew.bat, bỏ qua bước kiểm tra biên dịch." -ForegroundColor Yellow
    }
} else {
    Write-Host "`n[1/5] Bỏ qua kiểm tra biên dịch (SkipTest)" -ForegroundColor Yellow
}

# 4. Tự động trích xuất lịch sử commit & tạo Release Notes
Write-Host "`n[2/5] Đang chuẩn bị Release Notes..." -ForegroundColor Yellow
$lastTag = (git describe --tags --abbrev=0 2>$null)
$commitLogs = @()
if ($lastTag) {
    $commitLogs = (git log "${lastTag}..HEAD" --oneline --no-merges 2>$null)
} else {
    $commitLogs = (git log -n 10 --oneline --no-merges 2>$null)
}

$today = Get-Date -Format "yyyy-MM-dd"
$title = if ([string]::IsNullOrWhiteSpace($ReleaseTitle)) { "Bản cập nhật $tag" } else { $ReleaseTitle }

$features = @()
$uiux = @()
$fixes = @()
$other = @()

foreach ($line in $commitLogs) {
    if ($line -match "feat") { $features += "- $line" }
    elseif ($line -match "ui|ux|style") { $uiux += "- $line" }
    elseif ($line -match "fix|bug") { $fixes += "- $line" }
    else { $other += "- $line" }
}

$notesBuilder = New-Object System.Text.StringBuilder
[void]$notesBuilder.AppendLine("## 🎵 Bản Phát Hành $tag ($title)")
[void]$notesBuilder.AppendLine("")
[void]$notesBuilder.AppendLine("- **Ngày phát hành**: $today")
[void]$notesBuilder.AppendLine("- **Phiên bản**: ``$newVersion`` (Build ``$newCode``)")
[void]$notesBuilder.AppendLine("")

if ($ReleaseNotes) {
    [void]$notesBuilder.AppendLine("### 📝 Ghi Chú Phát Hành:")
    [void]$notesBuilder.AppendLine($ReleaseNotes)
    [void]$notesBuilder.AppendLine("")
}

if ($features.Count -gt 0) {
    [void]$notesBuilder.AppendLine("### ✨ Tính Năng Mới:")
    foreach ($f in $features) { [void]$notesBuilder.AppendLine($f) }
    [void]$notesBuilder.AppendLine("")
}

if ($uiux.Count -gt 0) {
    [void]$notesBuilder.AppendLine("### 🎨 Giao Diện & Trải Nghiệm (UI/UX Apple Music):")
    foreach ($u in $uiux) { [void]$notesBuilder.AppendLine($u) }
    [void]$notesBuilder.AppendLine("")
}

if ($fixes.Count -gt 0) {
    [void]$notesBuilder.AppendLine("### 🐛 Sửa Lỗi & Độ Ổn Định:")
    foreach ($fx in $fixes) { [void]$notesBuilder.AppendLine($fx) }
    [void]$notesBuilder.AppendLine("")
}

if ($other.Count -gt 0 -and $features.Count -eq 0 -and $fixes.Count -eq 0) {
    [void]$notesBuilder.AppendLine("### 🛠 Các Thay Đổi:")
    foreach ($o in $other) { [void]$notesBuilder.AppendLine($o) }
    [void]$notesBuilder.AppendLine("")
}

[void]$notesBuilder.AppendLine("---")
[void]$notesBuilder.AppendLine("*Ghi chú: Bản cập nhật sẽ tự động đồng bộ tới người dùng qua tính năng Auto-Update trong ứng dụng.*")

$finalNotes = $notesBuilder.ToString()
Write-Host $finalNotes -ForegroundColor Cyan

# 5. Cập nhật build.gradle.kts
Write-Host "`n[3/5] Cập nhật app/build.gradle.kts..." -ForegroundColor Yellow
$updatedGradleContent = $gradleContent -replace 'versionCode\s*=\s*\d+', "versionCode = $newCode"
$updatedGradleContent = $updatedGradleContent -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$newVersion`""

if ($DryRun) {
    Write-Host "[DRY RUN] Đã mô phỏng cập nhật phiên bản. Không ghi đĩa, không commit hay push." -ForegroundColor Magenta
    exit 0
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($buildGradlePath, $updatedGradleContent, $utf8NoBom)
Write-Host "Đã lưu version mới vào app/build.gradle.kts" -ForegroundColor Green

# 6. Git Commit & Git Tag
Write-Host "`n[4/5] Tạo Git Commit & Git Tag $tag..." -ForegroundColor Yellow
git add $buildGradlePath
git commit -m "chore(release): bump version to $tag"
git tag -a $tag -m $finalNotes

Write-Host "Đã tạo git tag $tag thành công!" -ForegroundColor Green

# 7. Push lên Remote (kích hoạt CI/CD GitHub Actions)
if (-not $SkipPush) {
    Write-Host "`n[5/5] Đang đẩy Commit & Tag $tag lên GitHub..." -ForegroundColor Yellow
    $currentBranch = (git rev-parse --abbrev-ref HEAD 2>$null)
    if (-not $currentBranch) { $currentBranch = "main" }

    git push origin $currentBranch
    git push origin $tag

    Write-Host "`n🎉 PHÁT HÀNH THÀNH CÔNG!" -ForegroundColor Green
    Write-Host "GitHub Actions đã được kích hoạt để tự động build Release APK." -ForegroundColor Cyan
    Write-Host "Người dùng app sẽ nhận thông báo cập nhật lên $tag qua Auto-Update!" -ForegroundColor Cyan
} else {
    Write-Host "`n[5/5] Bỏ qua git push (SkipPush). Tag $tag đã được tạo ở máy cục bộ." -ForegroundColor Yellow
}
