param(
    [string]$SourcePath = "docs/IconMusic/icnsFile_528548faafc2e00efe2a44c6608758d4_Apple_Music_1024x1024x32.png",
    [string]$OutputDir = "ios/MusicPlayer/Resources/Assets.xcassets/AppIcon.appiconset"
)

Add-Type -AssemblyName System.Drawing
$fullSrc = Resolve-Path $SourcePath
$srcImg = [System.Drawing.Image]::FromFile($fullSrc)

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Force $OutputDir | Out-Null
}

# Copy original 1024x1024
Copy-Item $fullSrc (Join-Path $OutputDir "AppIcon-1024.png") -Force

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
    $outPath = Join-Path $OutputDir $item.Name
    $bmp = New-Object System.Drawing.Bitmap($item.Width, $item.Height)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.DrawImage($srcImg, 0, 0, $item.Width, $item.Height)
    $g.Dispose()
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Generated $($item.Name) ($($item.Width)x$($item.Height))"
}

$srcImg.Dispose()
Write-Host "All icons generated successfully!" -ForegroundColor Green
