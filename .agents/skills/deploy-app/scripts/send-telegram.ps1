param(
    [Parameter(Mandatory = $false)]
    [string]$Message,

    [Parameter(Mandatory = $false)]
    [string]$StepTitle,

    [Parameter(Mandatory = $false)]
    [string]$ResultStatus = "Thành công",

    [Parameter(Mandatory = $false)]
    [string]$ChannelId = "-1003609918702",

    [Parameter(Mandatory = $false)]
    [string]$BotToken = "8005142785:AAHSrJ2Yp4l0Gj6Yk7iu8hwSwThZIiVKrCU",

    [Parameter(Mandatory = $false)]
    [string]$ParseMode = "HTML",

    [Parameter(Mandatory = $false)]
    [string]$QrCodeData,

    [Parameter(Mandatory = $false)]
    [string]$PhotoUrl,

    [Parameter(Mandatory = $false)]
    [string]$PhotoPath
)

try {
    [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
} catch {}

$iconRocket  = [char]::ConvertFromUtf32(0x1F680) # 🚀 Rocket
$iconSuccess = [char]::ConvertFromUtf32(0x2705)  # ✅ Checkmark
$iconWait    = [char]::ConvertFromUtf32(0x23F3)  # ⏳ Hourglass
$iconWarn    = [char]::ConvertFromUtf32(0x26A0)  # ⚠️ Warning
$iconNote    = [char]::ConvertFromUtf32(0x1F4DD)  # 📝 Memo
$iconClock   = [char]::ConvertFromUtf32(0x23F1)  # ⏱️ Stopwatch

$nowStr = (Get-Date).ToString("HH:mm:ss dd/MM/yyyy")

$finalMsg = ""
if (-not [string]::IsNullOrWhiteSpace($StepTitle)) {
    $isComplete = ($ResultStatus -match "Thanh cong|HOAN TAT|Hoan tat|Success|Complete|Done") -or ($StepTitle -match "THANH CONG|HOAN TAT|TONG KET|Complete|Success")
    $statusIcon = if ($isComplete) { $iconRocket } elseif ($ResultStatus -match "Dang|Cho|Tien hanh|Progress|Wait") { $iconWait } else { $iconWarn }
    
    if ($isComplete -and ($StepTitle -match "TONG KET|THANH CONG|HOAN TAT|Complete")) {
        $finalMsg = "$statusIcon <b>[MUSIC PLAYER CI/CD] $StepTitle</b>`n`n"
        if (-not [string]::IsNullOrWhiteSpace($Message)) {
            $finalMsg += "$Message`n`n"
        }
        $finalMsg += "$iconClock <i>$nowStr</i>"
    } else {
        $finalMsg = "$statusIcon <b>[MUSIC PLAYER CI/CD] $StepTitle</b>`n`n"
        if (-not [string]::IsNullOrWhiteSpace($Message)) {
            $finalMsg += "$iconNote <b>Chi tiết:</b> $Message`n"
        }
        $finalMsg += "<b>Trạng thái:</b> $ResultStatus`n"
        $finalMsg += "$iconClock <i>$nowStr</i>"
    }
} else {
    $finalMsg = $Message
}

if ([string]::IsNullOrWhiteSpace($BotToken) -or [string]::IsNullOrWhiteSpace($ChannelId)) {
    Write-Host "[Telegram] BotToken hoặc ChannelId để trống, bỏ qua gửi tin." -ForegroundColor Yellow
    return
}

try {
    # 1. Nếu có file ảnh cục bộ (PhotoPath)
    if (-not [string]::IsNullOrWhiteSpace($PhotoPath) -and (Test-Path $PhotoPath)) {
        $sendPhotoUrl = "https://api.telegram.org/bot$BotToken/sendPhoto"
        
        Add-Type -AssemblyName System.Net.Http
        $httpClient = New-Object System.Net.Http.HttpClient
        $content = New-Object System.Net.Http.MultipartFormDataContent
        
        $content.Add((New-Object System.Net.Http.StringContent($ChannelId)), "chat_id")
        $content.Add((New-Object System.Net.Http.StringContent($finalMsg)), "caption")
        $content.Add((New-Object System.Net.Http.StringContent($ParseMode)), "parse_mode")
        
        $fileBytes = [System.IO.File]::ReadAllBytes($PhotoPath)
        $fileContent = New-Object System.Net.Http.ByteArrayContent (,$fileBytes)
        $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("image/png")
        $content.Add($fileContent, "photo", [System.IO.Path]::GetFileName($PhotoPath))
        
        $response = $httpClient.PostAsync($sendPhotoUrl, $content).Result
        if (-not $response.IsSuccessStatusCode) {
            $errBody = $response.Content.ReadAsStringAsync().Result
            Write-Warning "Telegram sendPhoto thất bại: $errBody"
        }
        $httpClient.Dispose()
        return
    }
    
    # 2. Gửi text thông thường
    $sendMsgUrl = "https://api.telegram.org/bot$BotToken/sendMessage"
    $body = @{
        chat_id = $ChannelId
        text = $finalMsg
        parse_mode = $ParseMode
        disable_web_page_preview = $false
    } | ConvertTo-Json -Compress

    $res = Invoke-RestMethod -Uri $sendMsgUrl -Method Post -ContentType "application/json; charset=utf-8" -Body ([System.Text.Encoding]::UTF8.GetBytes($body)) -TimeoutSec 10 -ErrorAction SilentlyContinue
} catch {
    Write-Warning "Lỗi gửi thông báo Telegram: $($_.Exception.Message)"
}

