Add-Type -AssemblyName System.Drawing
$path = Resolve-Path "docs/IconMusic/icnsFile_528548faafc2e00efe2a44c6608758d4_Apple_Music_1024x1024x32.png"
$bmp = [System.Drawing.Bitmap]::FromFile($path)
$minX = $bmp.Width
$maxX = 0
$minY = $bmp.Height
$maxY = 0

for ($y = 0; $y -lt $bmp.Height; $y += 4) {
    for ($x = 0; $x -lt $bmp.Width; $x += 4) {
        $pixel = $bmp.GetPixel($x, $y)
        if ($pixel.A -gt 10) {
            if ($x -lt $minX) { $minX = $x }
            if ($x -gt $maxX) { $maxX = $x }
            if ($y -lt $minY) { $minY = $y }
            if ($y -gt $maxY) { $maxY = $y }
        }
    }
}
$bmp.Dispose()
$w = $maxX - $minX + 1
$h = $maxY - $minY + 1
Write-Host "Bounding Box: minX=$minX, maxX=$maxX, minY=$minY, maxY=$maxY"
Write-Host "Visible content size: ${w}x${h} inside 1024x1024"
