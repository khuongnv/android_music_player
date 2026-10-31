param (
    [Parameter(Mandatory=$true)]
    [string]$TaskSlug,

    [Parameter(Mandatory=$true)]
    [string]$TaskTitle,

    [Parameter(Mandatory=$false)]
    [string]$AgentName = "lead-architect"
)

$today = Get-Date -Format "yyyy-MM-dd"
$now = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$logFileName = "${today}_${TaskSlug}.md"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
$resolvedLogDir = Join-Path -Path $repoRoot -ChildPath "Docs\logs"

if (-not (Test-Path $resolvedLogDir)) {
    New-Item -ItemType Directory -Path $resolvedLogDir -Force | Out-Null
}

$logFilePath = Join-Path -Path $resolvedLogDir -ChildPath $logFileName
$indexFilePath = Join-Path -Path $resolvedLogDir -ChildPath "README.md"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

if (Test-Path $logFilePath) {
    Write-Host "Log file already exists: $logFilePath"
} else {
    $template = @"
# 📋 Nhật Ký Thực Thi: $TaskTitle

- **Ngày thực hiện**: $now
- **Agent phụ trách**: `$AgentName`
- **Yêu cầu gốc**: $TaskTitle
- **Trạng thái**: 🔄 Đang tiến hành

---

## 1. Phân Tích & Kế Hoạch (Analysis & Planning)
- Bối cảnh & Mục tiêu kỹ thuật:
- Các tầng kiến trúc liên quan (UI / Domain / Data / Player / Di):
- Phân chia nhiệm vụ cho các Agent:

## 2. Chi Tiết Các Bước Thực Hiện (Execution Steps)
- **Bước 1**: 
- **Bước 2**: 

## 3. Danh Sách Tệp Thay Đổi (Impacted Files)
- ``[Created]`` ``path/to/file.kt`` - Mô tả
- ``[Modified]`` ``path/to/file.kt`` - Mô tả

## 4. Kiểm Thử & Xác Minh (Verification & Testing)
- Kiểm tra build / tests (\`./gradlew test\` hoặc build kiểm tra).
- Đánh giá Memory Leak, Audio Focus, Battery Drain.

## 5. Kết Luận & Hướng Dẫn Tiếp Theo (Conclusion & Next Steps)
- Tóm tắt kết quả.
- Đề xuất các bước tiếp theo.
"@
    [System.IO.File]::WriteAllText($logFilePath, $template, $utf8NoBom)
    Write-Host "Created log file: $logFilePath"
}

# Add to README.md index if not already present
if (Test-Path $indexFilePath) {
    $content = [System.IO.File]::ReadAllText($indexFilePath, [System.Text.Encoding]::UTF8)
    if ($content -notmatch [regex]::Escape($logFileName)) {
        $newRow = "| {0} | [``{1}``](./{1}) | {2} | ``{3}`` | 🔄 Đang tiến hành |`r`n" -f $today, $logFileName, $TaskTitle, $AgentName
        $pattern = "(\| :--- \| :--- \| :--- \| :--- \| :--- \|\r?\n)"
        if ($content -match $pattern) {
            $updatedContent = $content -replace $pattern, "`$1$newRow"
            [System.IO.File]::WriteAllText($indexFilePath, $updatedContent, $utf8NoBom)
            Write-Host "Updated index in $indexFilePath"
        }
    }
}
