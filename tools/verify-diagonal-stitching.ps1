#requires -Version 7.0
param(
    [string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$TextureDirectory,
    [string]$PreviewPath,
    [switch]$ReportOnly
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
if (-not $TextureDirectory) { $TextureDirectory = Join-Path $ProjectRoot 'src/generated/resources/assets/harmonycraft_mod/textures/block' }
if (-not $PreviewPath) { $PreviewPath = Join-Path $ProjectRoot 'build/road-diagonal-stitching.png' }

function Rotate-Point([int]$X, [int]$Z, [int]$Maximum, [int]$Turns) {
    switch ($Turns % 4) {
        0 { $X, $Z }
        1 { ($Maximum - $Z), $X }
        2 { ($Maximum - $X), ($Maximum - $Z) }
        3 { $Z, ($Maximum - $X) }
    }
}

function Expected-Color([System.Drawing.Color]$Background, [System.Drawing.Color]$Paint, [int]$Opacity) {
    if ($Opacity -eq 0) { return $Background.ToArgb() }
    if ($Opacity -eq 255) { return $Paint.ToArgb() }
    $red = [int][Math]::Floor(($Background.R * (255 - $Opacity) + $Paint.R * $Opacity + 127) / 255)
    $green = [int][Math]::Floor(($Background.G * (255 - $Opacity) + $Paint.G * $Opacity + 127) / 255)
    $blue = [int][Math]::Floor(($Background.B * (255 - $Opacity) + $Paint.B * $Opacity + 127) / 255)
    [System.Drawing.Color]::FromArgb($red, $green, $blue).ToArgb()
}

# A five-tile staircase; every adjoining N/S glyph must lie on one world-space line.
$layout = @(
    [pscustomobject]@{X = 0; Z = 0; Turns = 0},
    [pscustomobject]@{X = 1; Z = 0; Turns = 2},
    [pscustomobject]@{X = 1; Z = 1; Turns = 0},
    [pscustomobject]@{X = 2; Z = 1; Turns = 2},
    [pscustomobject]@{X = 2; Z = 2; Turns = 0}
)
$samples = @(
    foreach ($joint in 0..3) {
        for ($near = 60; $near -lt 68; $near++) {
            for ($along = 0; $along -lt 64; $along++) {
                switch ($joint) {
                    0 { [pscustomobject]@{X = $near; Z = $along} }
                    1 { [pscustomobject]@{X = 64 + $along; Z = $near} }
                    2 { [pscustomobject]@{X = 64 + $near; Z = 64 + $along} }
                    3 { [pscustomobject]@{X = 128 + $along; Z = 64 + $near} }
                }
            }
        }
    }
)
$patterns = @('white_diagonal_line', 'yellow_diagonal_line', 'white_double_diagonal_line', 'yellow_double_diagonal_line')
$labels = @('White single: five adjoining tiles', 'Yellow single: five adjoining tiles', 'Double white: five adjoining tiles', 'Double yellow: five adjoining tiles')
$asphalt = [System.Drawing.Bitmap]::new((Join-Path $ProjectRoot 'src/main/resources/assets/harmonycraft_mod/textures/block/asphalt_block.png'))
$canvas = [System.Drawing.Bitmap]::new(856, 908)
$graphics = [System.Drawing.Graphics]::FromImage($canvas)
$font = [System.Drawing.Font]::new('Segoe UI', 12)
$checked = 0
$failures = 0
$firstFailure = ''
try {
    $graphics.Clear([System.Drawing.Color]::FromArgb(26, 30, 35))
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $graphics.DrawString('HarmonyCraft - diagonal seam regression (64 x 64, shown at 2x)', $font, [System.Drawing.Brushes]::White, 24, 20)
    for ($patternIndex = 0; $patternIndex -lt $patterns.Count; $patternIndex++) {
        $pattern = $patterns[$patternIndex]
        $top = [System.Drawing.Bitmap]::new((Join-Path $TextureDirectory "road_$pattern.png"))
        try {
            $paint = if ($pattern.StartsWith('yellow')) { [System.Drawing.Color]::FromArgb(244, 211, 35) } else { [System.Drawing.Color]::FromArgb(245, 245, 242) }
            for ($turns = 0; $turns -lt 4; $turns++) {
                $tiles = @{}
                foreach ($tile in $layout) {
                    $tileX, $tileZ = Rotate-Point $tile.X $tile.Z 2 $turns
                    $tiles["$tileX,$tileZ"] = ($tile.Turns + $turns) % 4
                }
                foreach ($sample in $samples) {
                    $worldX, $worldZ = Rotate-Point $sample.X $sample.Z 191 $turns
                    $tileX = [int][Math]::Floor($worldX / 64)
                    $tileZ = [int][Math]::Floor($worldZ / 64)
                    $tileTurns = $tiles["$tileX,$tileZ"]
                    if ($null -eq $tileTurns) { throw "Seam sample did not land on a road tile: $tileX,$tileZ" }
                    $u, $v = Rotate-Point ($worldX % 64) ($worldZ % 64) 63 ((4 - $tileTurns) % 4)
                    $distance = if ($pattern.Contains('_double_')) {
                        [Math]::Min([Math]::Abs(($sample.X - $sample.Z) - 26), [Math]::Abs(($sample.X - $sample.Z) - 38))
                    } else { [Math]::Abs(($sample.X - $sample.Z) - 32) }
                    $opacity = if ($distance -lt 3) { 255 } elseif ($distance -eq 3) { 128 } else { 0 }
                    $expected = Expected-Color ($asphalt.GetPixel($u, $v)) $paint $opacity
                    if ($top.GetPixel($u, $v).ToArgb() -ne $expected) {
                        $failures++
                        if (-not $firstFailure) { $firstFailure = "$pattern, rotation=$($turns * 90), world pixel=$worldX,$worldZ" }
                    }
                    $checked++
                }
            }

            # Preview reads the real PNGs, not a redrawn idealized version of the line.
            $scene = [System.Drawing.Bitmap]::new(192, 192)
            $sceneGraphics = [System.Drawing.Graphics]::FromImage($scene)
            try {
                for ($tileX = 0; $tileX -lt 3; $tileX++) {
                    for ($tileZ = 0; $tileZ -lt 3; $tileZ++) { $sceneGraphics.DrawImageUnscaled($asphalt, ($tileX * 64), ($tileZ * 64)) }
                }
                foreach ($tile in $layout) {
                    $rotated = [System.Drawing.Bitmap]::new($top)
                    try {
                        if ($tile.Turns -eq 2) { $rotated.RotateFlip([System.Drawing.RotateFlipType]::Rotate180FlipNone) }
                        $sceneGraphics.DrawImageUnscaled($rotated, ($tile.X * 64), ($tile.Z * 64))
                    } finally { $rotated.Dispose() }
                }
                $panelX = 24 + ($patternIndex % 2) * 416
                $panelY = 64 + [int][Math]::Floor($patternIndex / 2) * 416
                $graphics.DrawImage($scene, $panelX, $panelY, 384, 384)
                $graphics.DrawString($labels[$patternIndex], $font, [System.Drawing.Brushes]::White, [single]$panelX, [single]($panelY + 388))
            } finally { $sceneGraphics.Dispose(); $scene.Dispose() }
        } finally { $top.Dispose() }
    }
    $canvas.Save($PreviewPath, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Output "Seam pixels checked: $checked; mismatches: $failures; first mismatch: $firstFailure"
    Write-Output "Preview: $PreviewPath"
    if ($failures -gt 0 -and -not $ReportOnly) { throw "Diagonal seam regression failed at $failures pixels" }
    if ($failures -eq 0) { Write-Output 'PASS: four patterns, four rotations, four seams per five-tile run; constant width, centerline and spacing.' }
} finally {
    $font.Dispose()
    $graphics.Dispose()
    $canvas.Dispose()
    $asphalt.Dispose()
}
