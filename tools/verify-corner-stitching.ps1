#requires -Version 7.0
param([string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot))

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$textures = Join-Path $ProjectRoot 'src/generated/resources/assets/harmonycraft_mod/textures/block'
$patterns = @('white_right_angle_line', 'yellow_right_angle_line',
    'white_double_right_angle_line', 'yellow_double_right_angle_line',
    'white_curved_line', 'yellow_curved_line', 'white_double_curved_line', 'yellow_double_curved_line')
$asphalt = [System.Drawing.Bitmap]::new((Join-Path $ProjectRoot 'src/main/resources/assets/harmonycraft_mod/textures/block/asphalt_block.png'))
$canvas = [System.Drawing.Bitmap]::new(880, 1040)
$graphics = [System.Drawing.Graphics]::FromImage($canvas)
$font = [System.Drawing.Font]::new('Segoe UI', 11)
$checks = 0
function Rotate-Point([int]$X, [int]$Z, [int]$Turn) {
    switch ($Turn) {
        0 { $X, $Z }
        1 { (63 - $Z), $X }
        2 { (63 - $X), (63 - $Z) }
        3 { $Z, (63 - $X) }
    }
}
try {
    $graphics.Clear([System.Drawing.Color]::FromArgb(26, 30, 35))
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $graphics.DrawString('a02 corners - actual PNGs, straight-to-corner joins', $font, [System.Drawing.Brushes]::White, 16, 10)
    for ($index = 0; $index -lt $patterns.Count; $index++) {
        $pattern = $patterns[$index]
        $double = $pattern.Contains('_double_')
        $color = if ($pattern.StartsWith('yellow')) { 'yellow' } else { 'white' }
        $straightPattern = if ($double) { "${color}_double_line" } else { "${color}_solid_line" }
        $corner = [System.Drawing.Bitmap]::new((Join-Path $textures "road_$pattern.png"))
        $straight = [System.Drawing.Bitmap]::new((Join-Path $textures "road_$straightPattern.png"))
        $rotatedAsphalt = $asphalt.Clone()
        try {
            $mask = [bool[,]]::new(64, 64)
            for ($x = 0; $x -lt 64; $x++) {
                for ($z = 0; $z -lt 64; $z++) {
                    $mask[$x, $z] = $corner.GetPixel($x, $z).ToArgb() -ne $asphalt.GetPixel($x, $z).ToArgb()
                }
            }
            # Flood-fill the real artwork: a double turn must remain TWO paths,
            # not two overlapping L strokes that cross at the elbow.
            $components = 0
            $visited = [bool[,]]::new(64, 64)
            for ($x = 0; $x -lt 64; $x++) {
                for ($z = 0; $z -lt 64; $z++) {
                    if (-not $mask[$x, $z] -or $visited[$x, $z]) { continue }
                    $components++
                    $queue = [System.Collections.Generic.Queue[int]]::new()
                    $queue.Enqueue($z * 64 + $x)
                    $visited[$x, $z] = $true
                    while ($queue.Count -gt 0) {
                        $point = $queue.Dequeue()
                        $px = $point % 64
                        $pz = [int][Math]::Floor($point / 64)
                        foreach ($delta in @(@(-1, 0), @(1, 0), @(0, -1), @(0, 1))) {
                            $nx = $px + $delta[0]
                            $nz = $pz + $delta[1]
                            if ($nx -ge 0 -and $nx -lt 64 -and $nz -ge 0 -and $nz -lt 64 -and $mask[$nx, $nz] -and -not $visited[$nx, $nz]) {
                                $visited[$nx, $nz] = $true
                                $queue.Enqueue($nz * 64 + $nx)
                            }
                        }
                    }
                }
            }
            $expectedComponents = if ($double) { 2 } else { 1 }
            if ($components -ne $expectedComponents) { throw "$pattern has $components paths, expected $expectedComponents" }
            for ($u = 0; $u -lt 64; $u++) {
                if ($mask[0, $u] -or $mask[$u, 63]) { throw "$pattern incorrectly reaches west/south" }
            }
            # Compare every boundary texel in all four actual model rotations.
            for ($turn = 0; $turn -lt 4; $turn++) {
                for ($u = 0; $u -lt 64; $u++) {
                    $nx, $nz = Rotate-Point $u 0 $turn
                    $ex, $ez = Rotate-Point 63 $u $turn
                    $expected = $straight.GetPixel($nx, $nz).ToArgb() -ne $rotatedAsphalt.GetPixel($nx, $nz).ToArgb()
                    $northPaint = $corner.GetPixel($nx, $nz).ToArgb() -ne $rotatedAsphalt.GetPixel($nx, $nz).ToArgb()
                    $eastPaint = $corner.GetPixel($ex, $ez).ToArgb() -ne $rotatedAsphalt.GetPixel($ex, $ez).ToArgb()
                    if ($northPaint -ne $expected -or $eastPaint -ne $expected) {
                        throw "$pattern endpoint offset/width mismatch at texel $u (rotation $turn)"
                    }
                    $checks += 2
                }
                $corner.RotateFlip([System.Drawing.RotateFlipType]::Rotate90FlipNone)
                $straight.RotateFlip([System.Drawing.RotateFlipType]::Rotate90FlipNone)
                $rotatedAsphalt.RotateFlip([System.Drawing.RotateFlipType]::Rotate90FlipNone)
            }
            $left = 14 + ($index % 4) * 220
            $top = 54 + [int][Math]::Floor($index / 4) * 490
            # Three-tile N/E layout; draw real rasters, including the rotated straight.
            $graphics.DrawImage($straight, $left + 64, $top, 64, 64)
            $graphics.DrawImage($corner, $left + 64, $top + 64, 64, 64)
            $straight.RotateFlip([System.Drawing.RotateFlipType]::Rotate90FlipNone)
            $graphics.DrawImage($straight, $left + 128, $top + 64, 64, 64)
            $straight.RotateFlip([System.Drawing.RotateFlipType]::Rotate270FlipNone)
            # 3x enlargement makes the single-tile corner geometry easy to inspect.
            $graphics.DrawImage($corner, $left, $top + 158, 192, 192)
            $graphics.DrawString($pattern.Replace('_', "`n"), $font, [System.Drawing.Brushes]::White, [single]$left, [single]($top + 362))
        } finally { $corner.Dispose(); $straight.Dispose(); $rotatedAsphalt.Dispose() }
    }
    $preview = Join-Path $ProjectRoot 'build/a02-road-corners-preview.png'
    $canvas.Save($preview, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Output "PASS: 8 corners, $checks endpoint comparisons, exact straight-line width/spacing, 1/2 non-crossing paths."
    Write-Output "Preview: $preview"
} finally { $font.Dispose(); $graphics.Dispose(); $canvas.Dispose(); $asphalt.Dispose() }
