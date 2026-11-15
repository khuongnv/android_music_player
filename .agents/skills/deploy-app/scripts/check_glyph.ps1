Add-Type -AssemblyName System.Drawing
$path = Resolve-Path "docs/IconMusic/icnsFile_528548faafc2e00efe2a44c6608758d4_Apple_Music_1024x1024x32.png"
$bmp = [System.Drawing.Bitmap]::FromFile($path)
$minX = 1024; $maxX = 0; $minY = 1024; $maxY = 0

for ($y = 0; $y -lt $bmp.Height; $y += 2) {
    for ($x = 0; $x -lt $bmp.Width; $x += 2) {
        $p = $bmp.GetPixel($x, $y)
        if ($p.R -gt 240 -and $p.G -gt 240 -and $p.B -gt 240) {
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
Write-Host "Glyph Box: minX=$minX, maxX=$maxX, minY=$minY, maxY=$maxY"
Write-Host "Glyph size: ${w}x${h}"
