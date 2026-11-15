Add-Type -AssemblyName System.Drawing

$srcPath = Resolve-Path "docs/IconMusic/icnsFile_528548faafc2e00efe2a44c6608758d4_Apple_Music_1024x1024x32.png"
$srcImg = [System.Drawing.Bitmap]::FromFile($srcPath)

$outDir = "ios/MusicPlayer/Resources/Assets.xcassets/AppIcon.appiconset"
if (-not (Test-Path $outDir)) { New-Item -ItemType Directory -Force $outDir | Out-Null }

# 1. Tạo Canvas 1024x1024 chuẩn Full-Bleed
$fullBleed = New-Object System.Drawing.Bitmap(1024, 1024, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($fullBleed)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

# Vẽ nền Gradient Apple Music chuẩn không có góc bo và không có viền trong suốt
$topColor = [System.Drawing.Color]::FromArgb(255, 252, 60, 68)     # #FC3C44
$bottomColor = [System.Drawing.Color]::FromArgb(255, 249, 38, 72)  # #F92648
$brush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    (New-Object System.Drawing.Point(0, 0)),
    (New-Object System.Drawing.Point(0, 1024)),
    $topColor,
    $bottomColor
)
$g.FillRectangle($brush, 0, 0, 1024, 1024)
$brush.Dispose()

# 2. Cắt vùng chứa nốt nhạc trắng từ ảnh gốc (minX=262, minY=226, w=441, h=561)
# Để đẹp mắt và có độ mềm, ta lấy vùng rộng hơn một chút: x=250, y=215, w=465, h=585
$glyphRect = New-Object System.Drawing.Rectangle(250, 215, 465, 585)
$glyphCrop = $srcImg.Clone($glyphRect, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

# Tách riêng chỉ lấy các pixel nốt nhạc trắng (loại bỏ màu nền cũ của ảnh gốc)
$cleanGlyph = New-Object System.Drawing.Bitmap($glyphCrop.Width, $glyphCrop.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
for ($y = 0; $y -lt $glyphCrop.Height; $y++) {
    for ($x = 0; $x -lt $glyphCrop.Width; $x++) {
        $p = $glyphCrop.GetPixel($x, $y)
        # Nốt nhạc có R, G, B cao (gần trắng)
        if ($p.R -gt 210 -and $p.G -gt 210 -and $p.B -gt 210) {
            # Giữ nguyên độ trong suốt mịn anti-alias
            $alpha = [math]::Min(255, [int](($p.R + $p.G + $p.B) / 3))
            $cleanGlyph.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($alpha, 255, 255, 255))
        } else {
            $cleanGlyph.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(0, 0, 0, 0))
        }
    }
}
$glyphCrop.Dispose()

# 3. Vẽ nốt nhạc trắng lên giữa 1024x1024 với tỉ lệ chuẩn Apple Music (khoảng 54% kích thước icon)
$targetWidth = 530
$targetHeight = [int]($targetWidth * ($cleanGlyph.Height / $cleanGlyph.Width))
$targetX = [int]((1024 - $targetWidth) / 2) + 6
$targetY = [int]((1024 - $targetHeight) / 2)

# Vẽ bóng đổ nhẹ cho nốt nhạc
$shadowAttributes = New-Object System.Drawing.Imaging.ImageAttributes
$matrix = New-Object System.Drawing.Imaging.ColorMatrix
$matrix.Matrix33 = 0.25 # Độ mờ 25%
$shadowAttributes.SetColorMatrix($matrix)
$destShadowRect = New-Object System.Drawing.Rectangle($targetX, ($targetY + 8), $targetWidth, $targetHeight)
$g.DrawImage($cleanGlyph, $destShadowRect, 0, 0, $cleanGlyph.Width, $cleanGlyph.Height, [System.Drawing.GraphicsUnit]::Pixel, $shadowAttributes)

# Vẽ nốt nhạc trắng chính
$destRect = New-Object System.Drawing.Rectangle($targetX, $targetY, $targetWidth, $targetHeight)
$g.DrawImage($cleanGlyph, $destRect, 0, 0, $cleanGlyph.Width, $cleanGlyph.Height, [System.Drawing.GraphicsUnit]::Pixel)

$g.Dispose()
$cleanGlyph.Dispose()
$srcImg.Dispose()

# Lưu icon gốc 1024x1024
$masterIconPath = Join-Path $outDir "AppIcon-1024.png"
$fullBleed.Save($masterIconPath, [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host "Đã tạo AppIcon-1024.png full-bleed (100% kích thước chuẩn)" -ForegroundColor Green

# 4. Sinh các kích thước con
$sizes = @(
    @{ Name = "AppIcon60x60@2x.png"; Width = 120; Height = 120 },
    @{ Name = "AppIcon60x60@3x.png"; Width = 180; Height = 180 },
    @{ Name = "AppIcon76x76~ipad.png"; Width = 76; Height = 76 },
    @{ Name = "AppIcon76x76@2x~ipad.png"; Width = 152; Height = 152 },
    @{ Name = "AppIcon83.5x83.5@2x~ipad.png"; Width = 167; Height = 167 },
    @{ Name = "AppIcon20x20@2x.png"; Width = 40; Height = 40 },
    @{ Name = "AppIcon20x20@3x.png"; Width = 60; Height = 60 },
    @{ Name = "AppIcon29x29@2x.png"; Width = 58; Height = 58 },
    @{ Name = "AppIcon29x29@3x.png"; Width = 87; Height = 87 },
    @{ Name = "AppIcon40x40@2x.png"; Width = 80; Height = 80 },
    @{ Name = "AppIcon40x40@3x.png"; Width = 120; Height = 120 }
)

foreach ($item in $sizes) {
    $outPath = Join-Path $outDir $item.Name
    $bmp = New-Object System.Drawing.Bitmap($item.Width, $item.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $gSub = [System.Drawing.Graphics]::FromImage($bmp)
    $gSub.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $gSub.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $gSub.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $gSub.DrawImage($fullBleed, 0, 0, $item.Width, $item.Height)
    $gSub.Dispose()
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Generated $($item.Name) ($($item.Width)x$($item.Height))"
}

$fullBleed.Dispose()
Write-Host "Hoàn tất tái tạo toàn bộ icon full-bleed sắc nét!" -ForegroundColor Green
