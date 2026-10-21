#requires -Version 7.0
param([string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot))

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$textures = Join-Path $ProjectRoot 'src/generated/resources/assets/harmonycraft_mod/textures/block'
$arrows = @('straight', 'left', 'right', 'straight_left', 'straight_right', 'left_right',
    'uturn', 'straight_uturn', 'left_uturn', 'merge_left', 'merge_right')
$images = @{}
$white = [System.Drawing.Color]::FromArgb(245, 245, 242).ToArgb()
$canvas = [System.Drawing.Bitmap]::new(1000, 1130)
$graphics = [System.Drawing.Graphics]::FromImage($canvas)
$font = [System.Drawing.Font]::new('Segoe UI', 12)
try {
    $graphics.Clear([System.Drawing.Color]::FromArgb(26, 30, 35))
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $graphics.DrawString('a02 - GB 5768.3-2025 Fig.42, actual 1.25 x 2.25 plane aspect', $font, [System.Drawing.Brushes]::White, 18, 12)
    for ($index = 0; $index -lt $arrows.Count; $index++) {
        $type = $arrows[$index]
        $image = [System.Drawing.Bitmap]::new((Join-Path $textures "road_arrow_$type.png"))
        $images[$type] = $image
        if ($image.Width -ne 64 -or $image.Height -ne 64) { throw "$type must remain 64 x 64" }
        $count = 0
        $occupiedRows = [System.Collections.Generic.HashSet[int]]::new()
        $painted = [bool[,]]::new(64, 64)
        for ($x = 0; $x -lt 64; $x++) {
            for ($z = 0; $z -lt 64; $z++) {
                $pixel = $image.GetPixel($x, $z)
                if ($pixel.A -eq 0) { continue }
                if ($pixel.ToArgb() -ne $white) { throw "$type must only contain opaque white and transparency" }
                if ($x -eq 0 -or $x -eq 63 -or $z -lt 2 -or $z -ge 62) { throw "$type was clipped at the canvas boundary" }
                $count++
                $painted[$x, $z] = $true
                [void]$occupiedRows.Add($z)
            }
        }
        if ($count -lt 180 -or $occupiedRows.Count -lt 57) { throw "$type is empty, disconnected vertically, or undersized" }
        $visited = [bool[,]]::new(64, 64)
        $components = 0
        for ($x = 0; $x -lt 64; $x++) {
            for ($z = 0; $z -lt 64; $z++) {
                if (-not $painted[$x, $z] -or $visited[$x, $z]) { continue }
                $components++
                $queue = [System.Collections.Generic.Queue[int]]::new()
                $queue.Enqueue($z * 64 + $x)
                $visited[$x, $z] = $true
                while ($queue.Count -gt 0) {
                    $point = $queue.Dequeue()
                    $px = $point % 64
                    $pz = [int][Math]::Floor($point / 64)
                    for ($dx = -1; $dx -le 1; $dx++) {
                        for ($dz = -1; $dz -le 1; $dz++) {
                            $nx = $px + $dx
                            $nz = $pz + $dz
                            if ($nx -ge 0 -and $nx -lt 64 -and $nz -ge 0 -and $nz -lt 64 -and $painted[$nx, $nz] -and -not $visited[$nx, $nz]) {
                                $visited[$nx, $nz] = $true
                                $queue.Enqueue($nz * 64 + $nx)
                            }
                        }
                    }
                }
            }
        }
        if ($components -ne 1) { throw "$type contains detached paint fragments ($components components)" }
        $left = 25 + ($index % 4) * 250
        $top = 62 + [int][Math]::Floor($index / 4) * 350
        $graphics.FillRectangle([System.Drawing.Brushes]::DimGray, $left, $top, 170, 306)
        $graphics.DrawImage($image, $left, $top, 170, 306)
        $graphics.DrawString($type.Replace('_', ' '), $font, [System.Drawing.Brushes]::White, [single]$left, [single]($top + 313))
    }
    foreach ($pair in @(@('left', 'right'), @('straight_left', 'straight_right'), @('merge_left', 'merge_right'))) {
        for ($x = 0; $x -lt 64; $x++) {
            for ($z = 0; $z -lt 64; $z++) {
                if ($images[$pair[0]].GetPixel($x, $z).ToArgb() -ne $images[$pair[1]].GetPixel(63 - $x, $z).ToArgb()) {
                    throw "Arrow pair $($pair -join '/') must be exact pixel mirrors"
                }
            }
        }
    }
    foreach ($type in @('straight', 'left_right')) {
        for ($x = 0; $x -lt 64; $x++) {
            for ($z = 0; $z -lt 64; $z++) {
                if ($images[$type].GetPixel($x, $z).ToArgb() -ne $images[$type].GetPixel(63 - $x, $z).ToArgb()) { throw "$type must be self-symmetric" }
            }
        }
    }
    # Fig.42 straight arrow: 15-cm shaft / 45-cm head and 120/300 head length.
    for ($z = 2; $z -lt 62; $z++) {
        for ($x = 0; $x -lt 64; $x++) {
            $cmX = ($x + 0.5 - 32) / 0.36
            $cmZ = ($z + 0.5 - 2) / 0.2
            $expected = if ($cmZ -lt 120) { [Math]::Abs($cmX) -lt 22.5 * $cmZ / 120 } else { [Math]::Abs($cmX) -lt 7.5 }
            if (($images.straight.GetPixel($x, $z).A -gt 0) -ne $expected) { throw 'Straight arrow must retain Fig.42 proportions in world-space' }
        }
    }
    $preview = Join-Path $ProjectRoot 'build/a02-road-arrows-preview.png'
    $canvas.Save($preview, [System.Drawing.Imaging.ImageFormat]::Png)
    Write-Output 'PASS: 11 GB arrow sprites, connected silhouettes, binary transparency, no clipping, 3 exact mirror pairs, 2 self-symmetric arrows, straight-arrow dimension ratios.'
    Write-Output "Preview: $preview"
} finally {
    foreach ($image in $images.Values) { $image.Dispose() }
    $font.Dispose(); $graphics.Dispose(); $canvas.Dispose()
}
