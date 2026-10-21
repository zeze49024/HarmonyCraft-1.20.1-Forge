#requires -Version 7.0
param([string]$ProjectRoot = (Split-Path -Parent $PSScriptRoot))

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$generated = Join-Path $ProjectRoot 'src/generated/resources/assets/harmonycraft_mod'
$mainAssets = Join-Path $ProjectRoot 'src/main/resources/assets/harmonycraft_mod'

function Assert-Resource([bool]$Condition, [string]$Message) {
    if (-not $Condition) { throw $Message }
}

function Read-Json([string]$Path) {
    Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json -AsHashtable
}

function Get-DiagonalOpacity([string]$Pattern, [int]$X, [int]$Z) {
    $offset = $X - $Z
    $distance = if ($Pattern.Contains('_double_')) {
        [Math]::Min([Math]::Abs($offset - 26), [Math]::Abs($offset - 38))
    } else { [Math]::Abs($offset - 32) }
    if ($distance -lt 3) { return 255 }
    if ($distance -eq 3) { return 128 }
    return 0
}

function Blend-PaintColor([System.Drawing.Color]$Background, [System.Drawing.Color]$Paint, [int]$Opacity) {
    if ($Opacity -eq 0) { return $Background.ToArgb() }
    if ($Opacity -eq 255) { return $Paint.ToArgb() }
    $red = [int][Math]::Floor(($Background.R * (255 - $Opacity) + $Paint.R * $Opacity + 127) / 255)
    $green = [int][Math]::Floor(($Background.G * (255 - $Opacity) + $Paint.G * $Opacity + 127) / 255)
    $blue = [int][Math]::Floor(($Background.B * (255 - $Opacity) + $Paint.B * $Opacity + 127) / 255)
    return [System.Drawing.Color]::FromArgb($red, $green, $blue).ToArgb()
}

function Get-StripeProfiles([int[]]$Opacity) {
    $mass = 0.0
    $moment = 0.0
    for ($index = 0; $index -le $Opacity.Count; $index++) {
        if ($index -lt $Opacity.Count -and $Opacity[$index] -gt 0) {
            $mass += $Opacity[$index]
            $moment += $index * $Opacity[$index]
        } elseif ($mass -gt 0) {
            [pscustomobject]@{Width = $mass / 255; Center = $moment / $mass}
            $mass = 0.0
            $moment = 0.0
        }
    }
}

function Assert-ModReference([string]$Reference, [string]$Kind, [string]$Extension) {
    if ($Reference -notmatch '^harmonycraft_mod:(.+)$') { return }
    $relative = $Matches[1]
    $first = Join-Path $generated "$Kind/$relative.$Extension"
    $second = Join-Path $mainAssets "$Kind/$relative.$Extension"
    Assert-Resource ((Test-Path -LiteralPath $first) -or (Test-Path -LiteralPath $second)) "Missing reference: $Reference ($Kind)"
}

$roads = [ordered]@{
    'asphalt_white_str' = 'white_solid_line'
    'asphalt_white_dashed_line' = 'white_dashed_line'
    'asphalt_white_double_line' = 'white_double_line'
    'asphalt_yellow_solid_line' = 'yellow_solid_line'
    'asphalt_yellow_dashed_line' = 'yellow_dashed_line'
    'asphalt_yellow_double_line' = 'yellow_double_line'
    'asphalt_stop_line' = 'stop_line'
    'asphalt_crosswalk' = 'crosswalk'
    'asphalt_white_diagonal_line' = 'white_diagonal_line'
    'asphalt_yellow_diagonal_line' = 'yellow_diagonal_line'
    'asphalt_white_double_diagonal_line' = 'white_double_diagonal_line'
    'asphalt_yellow_double_diagonal_line' = 'yellow_double_diagonal_line'
    'asphalt_white_right_angle_line' = 'white_right_angle_line'
    'asphalt_yellow_right_angle_line' = 'yellow_right_angle_line'
    'asphalt_white_double_right_angle_line' = 'white_double_right_angle_line'
    'asphalt_yellow_double_right_angle_line' = 'yellow_double_right_angle_line'
    'asphalt_white_curved_line' = 'white_curved_line'
    'asphalt_yellow_curved_line' = 'yellow_curved_line'
    'asphalt_white_double_curved_line' = 'white_double_curved_line'
    'asphalt_yellow_double_curved_line' = 'yellow_double_curved_line'
    'asphalt_reversible_lane_zigzag' = 'reversible_lane_zigzag'
}
$arrows = @('road_arrow_straight', 'road_arrow_left', 'road_arrow_right',
    'road_arrow_straight_left', 'road_arrow_straight_right', 'road_arrow_left_right',
    'road_arrow_uturn', 'road_arrow_straight_uturn', 'road_arrow_left_uturn',
    'road_arrow_merge_left', 'road_arrow_merge_right')
$rotations = @{north = 0; east = 90; south = 180; west = 270}
$ids = @('asphalt_block', 'asphalt_slab', 'asphalt_new_block', 'asphalt_new_slab',
    'asphalt_faded_block', 'asphalt_faded_slab', 'road_manhole', 'road_drain')
$removedResourcePattern = '^(asphalt_white_guide_|asphalt_crosswalk_edge_line_|asphalt_pedestrian_waiting_area_|asphalt_two_stage_crossing_island_|asphalt_bus_stop_border_|asphalt_bus_bay_line_|asphalt_bus_stop_area_|road_text_|road_number_|road_speed_limit_|road_symbol_bus|road_arrow_reversible_lane)'

foreach ($road in $roads.GetEnumerator()) {
    $fullId = $road.Key + '_block'
    $slabId = $road.Key + '_slab'
    $ids += $fullId, $slabId
    $fullStates = (Read-Json (Join-Path $generated "blockstates/$fullId.json")).variants
    $slabStates = (Read-Json (Join-Path $generated "blockstates/$slabId.json")).variants
    Assert-Resource ($fullStates.Count -eq 4) "$fullId must contain four facings"
    Assert-Resource ($slabStates.Count -eq 12) "$slabId must contain all 4 x 3 facing/height combinations"
    foreach ($facing in $rotations.Keys) {
        $fullState = $fullStates["facing=$facing"]
        Assert-Resource ($null -ne $fullState) "Missing $fullId facing=$facing"
        Assert-Resource ([int]$fullState.y -eq $rotations[$facing]) "Unexpected $fullId rotation"
        foreach ($height in @('bottom', 'top', 'double')) {
            $key = $slabStates.Keys | Where-Object { $_ -match "(^|,)facing=$facing(,|$)" -and $_ -match "(^|,)type=$height(,|$)" }
            Assert-Resource (@($key).Count -eq 1) "Missing slab state $slabId/$facing/$height"
            $variant = $slabStates[$key]
            Assert-Resource ([int]$variant.y -eq $rotations[$facing]) "Unexpected $slabId rotation"
            if ($height -eq 'double') {
                Assert-Resource ($variant.model -eq "harmonycraft_mod:block/$fullId") "$slabId double state must use the matching full model"
            }
        }
    }
    foreach ($modelName in @($fullId, $slabId, ($slabId + '_top'))) {
        $model = Read-Json (Join-Path $generated "models/block/$modelName.json")
        Assert-Resource ($model.textures.side -eq 'harmonycraft_mod:block/asphalt_block') "$modelName side must be plain asphalt"
        Assert-Resource ($model.textures.bottom -eq 'harmonycraft_mod:block/asphalt_block') "$modelName bottom must be plain asphalt"
        Assert-Resource ($model.textures.top -eq "harmonycraft_mod:block/road_$($road.Value)") "$modelName top texture mismatch"
        $paintedEdges = switch -Regex ($road.Value) {
            '(solid_line|double_line)$' { 'north', 'south' }
            'diagonal_line$' { 'north', 'east' }
            '(right_angle_line|curved_line)$' { 'north', 'east' }
            '^crosswalk$' { 'east', 'west' }
            '^stop_line$' { 'north', 'east', 'west' }
            '^reversible_lane_zigzag$' { 'north', 'south' }
        }
        Assert-Resource ($model.elements.Count -eq 1) "$modelName must have a single cuboid"
        $element = $model.elements[0]
        $bottomY = if ($modelName.EndsWith('_top')) { 8 } else { 0 }
        $topY = if ($modelName -eq $slabId) { 8 } else { 16 }
        Assert-Resource (($element.from -join ',') -eq "0,$bottomY,0" -and ($element.to -join ',') -eq "16,$topY,16") "$modelName geometry mismatch"
        foreach ($edge in @('north', 'east', 'south', 'west')) {
            $face = $element.faces[$edge]
            $expectedTexture = if ($edge -in $paintedEdges) { "#$edge" } else { '#side' }
            Assert-Resource ($face.texture -eq $expectedTexture -and $face.cullface -eq $edge) "$modelName/$edge side mismatch"
            # Forge omits UVs when they match Minecraft's geometry-derived default.
            $uv = if ($face.ContainsKey('uv')) { $face.uv } else { @(0, (16 - $topY), 16, (16 - $bottomY)) }
            Assert-Resource (($uv -join ',') -eq "0,$(16 - $topY),16,$(16 - $bottomY)") "$modelName/$edge must crop, not squash, its side texture"
            if ($edge -in $paintedEdges) {
                Assert-Resource ($model.textures[$edge] -eq "harmonycraft_mod:block/road_$($road.Value)_side_$edge") "$modelName/$edge painted texture mismatch"
            }
        }
        Assert-Resource ($element.faces.up.texture -eq '#top' -and $element.faces.down.texture -eq '#bottom') "$modelName top/bottom face mismatch"
        Assert-Resource (($element.faces.up.cullface -eq 'up') -eq ($topY -eq 16)) "$modelName internal slab top must not be culled"
        Assert-Resource (($element.faces.down.cullface -eq 'down') -eq ($bottomY -eq 0)) "$modelName internal slab bottom must not be culled"
    }
}

foreach ($arrow in $arrows) {
    $ids += $arrow
    $states = (Read-Json (Join-Path $generated "blockstates/$arrow.json")).variants
    $reversible = $arrow -in @('road_arrow_uturn', 'road_arrow_straight_uturn', 'road_arrow_left_uturn')
    $mirrorValues = if ($reversible) { @('false', 'true') } else { @('false') }
    Assert-Resource ($states.Count -eq (8 * $mirrorValues.Count)) "$arrow facing/height/mirror state count mismatch"
    foreach ($facing in $rotations.Keys) {
        foreach ($lowered in @('false', 'true')) {
            foreach ($mirrorValue in $mirrorValues) {
                $key = $states.Keys | Where-Object { $_ -match "(^|,)facing=$facing(,|$)" -and $_ -match "(^|,)lowered=$lowered(,|$)" -and (-not $reversible -or $_ -match "(^|,)mirrored=$mirrorValue(,|$)") }
                Assert-Resource (@($key).Count -eq 1) "Missing arrow state $arrow/$facing/$lowered/$mirrorValue"
                Assert-Resource ([int]$states[$key].y -eq $rotations[$facing]) "Unexpected $arrow rotation"
                $lowSuffix = if ($lowered -eq 'true') { '_lowered' } else { '' }
                $mirrorSuffix = if ($mirrorValue -eq 'true') { '_mirrored' } else { '' }
                Assert-Resource ($states[$key].model -eq "harmonycraft_mod:block/$arrow$lowSuffix$mirrorSuffix") "$arrow has incorrect mirror/height model"
            }
        }
    }
    foreach ($lowered in @($false, $true)) {
      foreach ($mirrorValue in $mirrorValues) {
        $suffix = if ($lowered) { '_lowered' } else { '' }
        if ($mirrorValue -eq 'true') { $suffix += '_mirrored' }
        $model = Read-Json (Join-Path $generated "models/block/$arrow$suffix.json")
        $element = $model.elements[0]
        Assert-Resource ($model.render_type -eq 'minecraft:cutout') "$arrow must use cutout transparency"
        Assert-Resource (($element.to[0] - $element.from[0]) -eq 20 -and ($element.to[2] - $element.from[2]) -eq 36) "$arrow plane must be 1.25 x 2.25 blocks"
        $height = if ($lowered) { -7.95 } else { 0.05 }
        Assert-Resource ([Math]::Abs($element.from[1] - $height) -lt 0.00001) "$arrow height mismatch"
        Assert-Resource ($element.faces.up.ContainsKey('cullface') -eq $false) "$arrow overhang must not be culled"
        Assert-Resource ($element.faces.down.ContainsKey('cullface') -eq $false) "$arrow bottom overhang must not be culled"
        $upUv = if ($element.faces.up.ContainsKey('uv')) { $element.faces.up.uv } else { @(0, 0, 16, 16) }
        $expectedUv = if ($mirrorValue -eq 'true') { '16,0,0,16' } else { '0,0,16,16' }
        Assert-Resource (($upUv -join ',') -eq $expectedUv) "$arrow mirror UVs mismatch"
      }
    }
}

$asphaltStates = (Read-Json (Join-Path $generated 'blockstates/asphalt_block.json')).variants['']
Assert-Resource ($asphaltStates.Count -eq 4) 'Base asphalt must expose normal, cracked, patched and pothole models'
$weights = @($asphaltStates | ForEach-Object { [int]$_.weight })
Assert-Resource (($weights -join ',') -eq '835,55,55,55') 'Each defect must be 5.5 percent and total exactly 16.5 percent'
$asphaltSlabStates = (Read-Json (Join-Path $generated 'blockstates/asphalt_slab.json')).variants
Assert-Resource ($asphaltSlabStates.Count -eq 3) 'Base asphalt slab model must cover bottom, top and double while ignoring waterlogging'
foreach ($variants in $asphaltSlabStates.Values) {
    Assert-Resource (@($variants).Count -eq 4) 'Every asphalt slab state must retain four weighted textures'
    Assert-Resource ((@($variants | ForEach-Object { [int]$_.weight }) -join ',') -eq '835,55,55,55') 'Slab defect weights changed'
}

foreach ($feature in @('road_manhole', 'road_drain')) {
    $states = (Read-Json (Join-Path $generated "blockstates/$feature.json")).variants
    Assert-Resource ($states.Count -eq 8) "$feature must expose round/square in all four facings"
    foreach ($shape in @('round', 'square')) {
        Assert-Resource (Test-Path -LiteralPath (Join-Path $generated "textures/block/$feature`_$shape.png")) "Missing $feature $shape texture"
    }
}

foreach ($language in @('zh_cn', 'en_us', 'ja_jp')) {
    $translations = Read-Json (Join-Path $generated "lang/$language.json")
    foreach ($id in $ids) {
        Assert-Resource (-not [string]::IsNullOrWhiteSpace($translations["block.harmonycraft_mod.$id"])) "Missing or empty $language translation: $id"
    }
}

Get-ChildItem -LiteralPath (Join-Path $generated 'models') -Filter '*.json' -Recurse | ForEach-Object {
    $model = Read-Json $_.FullName
    if ($model.parent) { Assert-ModReference $model.parent 'models' 'json' }
    if ($model.textures) {
        foreach ($value in $model.textures.Values) { Assert-ModReference $value 'textures' 'png' }
    }
}
Get-ChildItem -LiteralPath $generated -File -Recurse | Where-Object { $_.BaseName -match $removedResourcePattern } |
    ForEach-Object { throw "Removed road component still has a generated resource: $($_.FullName)" }

# Validate original pixels and the physical UV orientation of each projected side stripe.
$white = [System.Drawing.Color]::FromArgb(245, 245, 242).ToArgb()
$yellow = [System.Drawing.Color]::FromArgb(244, 211, 35).ToArgb()
$asphalt = [System.Drawing.Bitmap]::new((Join-Path $mainAssets 'textures/block/asphalt_block.png'))
try {
    foreach ($road in $roads.GetEnumerator()) {
        $top = [System.Drawing.Bitmap]::new((Join-Path $generated "textures/block/road_$($road.Value).png"))
        try {
            $paintColor = if ($road.Value.StartsWith('yellow')) { [System.Drawing.Color]::FromArgb(244, 211, 35) } else { [System.Drawing.Color]::FromArgb(245, 245, 242) }
            foreach ($edge in @('north', 'east', 'south', 'west')) {
                $projection = @(for ($u = 0; $u -lt 64; $u++) {
                    $x, $z = switch ($edge) {
                        north { (63 - $u), 0 }
                        east { 63, (63 - $u) }
                        south { $u, 63 }
                        west { 0, $u }
                    }
                    if ($road.Value.EndsWith('diagonal_line')) {
                        Get-DiagonalOpacity $road.Value $x $z
                    } else {
                        $pixel = $top.GetPixel($x, $z).ToArgb()
                        if ($pixel -eq $white -or $pixel -eq $yellow) { 255 } else { 0 }
                    }
                })
                $paint = @($projection | Where-Object { $_ -gt 0 })
                $sidePath = Join-Path $generated "textures/block/road_$($road.Value)_side_$edge.png"
                Assert-Resource ((Test-Path -LiteralPath $sidePath) -eq ($paint.Count -gt 0)) "Side must exist exactly where top paint reaches an edge: $($road.Value)/$edge"
                if ($paint.Count -eq 0) { continue }
                $side = [System.Drawing.Bitmap]::new($sidePath)
                try {
                    Assert-Resource ($side.Width -eq 64 -and $side.Height -eq 64) "Side texture must be 64 x 64: $sidePath"
                    for ($u = 0; $u -lt 64; $u++) {
                        for ($v = 0; $v -lt 64; $v++) {
                            $expected = Blend-PaintColor ($asphalt.GetPixel($u, $v)) $paintColor $projection[$u]
                            Assert-Resource ($side.GetPixel($u, $v).ToArgb() -eq $expected) "Top-to-side pixel mismatch: $($road.Value)/$edge at $u,$v"
                        }
                    }
                } finally { $side.Dispose() }
            }
            if ($road.Value.EndsWith('diagonal_line')) {
                $color = if ($road.Value.StartsWith('yellow')) { $yellow } else { $white }
                for ($x = 0; $x -lt 64; $x++) {
                    for ($z = 0; $z -lt 64; $z++) {
                        $opacity = Get-DiagonalOpacity $road.Value $x $z
                        $expected = Blend-PaintColor ($asphalt.GetPixel($x, $z)) $paintColor $opacity
                        Assert-Resource ($top.GetPixel($x, $z).ToArgb() -eq $expected) "Diagonal must use symmetric six-pixel coverage centered on integer diagonal coordinates"
                    }
                }
                $straightPattern = if ($road.Value.Contains('_double_')) { $road.Value.Replace('double_diagonal_line', 'double_line') } else { $road.Value.Replace('diagonal_line', 'solid_line') }
                $straight = [System.Drawing.Bitmap]::new((Join-Path $generated "textures/block/road_$straightPattern.png"))
                try {
                    $straightOpacity = @(for ($u = 0; $u -lt 64; $u++) { if ($straight.GetPixel($u, 0).ToArgb() -eq $color) { 255 } else { 0 } })
                    $straightProfiles = @(Get-StripeProfiles $straightOpacity)
                    foreach ($edge in @('north', 'east')) {
                        $edgeOpacity = @(for ($u = 0; $u -lt 64; $u++) { if ($edge -eq 'north') { Get-DiagonalOpacity $road.Value $u 0 } else { Get-DiagonalOpacity $road.Value 63 $u } })
                        $edgeProfiles = @(Get-StripeProfiles $edgeOpacity)
                        Assert-Resource ($edgeProfiles.Count -eq $straightProfiles.Count) "Diagonal stripe count must match the straight line"
                        for ($stripe = 0; $stripe -lt $straightProfiles.Count; $stripe++) {
                            Assert-Resource ([Math]::Abs($edgeProfiles[$stripe].Width - $straightProfiles[$stripe].Width) -lt 0.01) "Diagonal edge coverage must retain straight-line width"
                            Assert-Resource ([Math]::Abs($edgeProfiles[$stripe].Center - $straightProfiles[$stripe].Center) -le 0.50001) "Diagonal endpoint may only differ by the half-texel center-to-border sampling distance"
                        }
                        if ($edgeProfiles.Count -eq 2) {
                            Assert-Resource ([Math]::Abs(($edgeProfiles[1].Center - $edgeProfiles[0].Center) - 12) -lt 0.00001) "Double diagonal center spacing must remain twelve pixels"
                        }
                    }
                } finally { $straight.Dispose() }
            }
        } finally { $top.Dispose() }
    }
} finally { $asphalt.Dispose() }

# Render the exact generated pixels with nearest-neighbor scaling for a readable review sheet.
$specialTextures = @('asphalt_cracked', 'asphalt_patched', 'asphalt_pothole', 'asphalt_new', 'asphalt_faded',
    'road_manhole_round', 'road_manhole_square', 'road_drain_round', 'road_drain_square')
$labels = @(@($roads.Values) + $arrows + $specialTextures | ForEach-Object { $_.Replace('_', ' ') })
$files = @($roads.Values | ForEach-Object { "road_$_.png" }) + @($arrows | ForEach-Object { "$_.png" }) +
    @($specialTextures | ForEach-Object { "$_.png" })
$canvas = [System.Drawing.Bitmap]::new(960, [int](74 + [Math]::Ceiling($files.Count / 4) * 250))
$graphics = [System.Drawing.Graphics]::FromImage($canvas)
$font = [System.Drawing.Font]::new('Segoe UI', 12)
try {
    $graphics.Clear([System.Drawing.Color]::FromArgb(26, 30, 35))
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $graphics.DrawString('HarmonyCraft - original 64 x 64 road markings', $font, [System.Drawing.Brushes]::White, 20, 14)
    for ($index = 0; $index -lt $files.Count; $index++) {
        $image = [System.Drawing.Bitmap]::new((Join-Path $generated "textures/block/$($files[$index])"))
        try {
            Assert-Resource ($image.Width -eq 64 -and $image.Height -eq 64) "Texture must be 64 x 64: $($files[$index])"
            $x = 24 + ($index % 4) * 240
            $y = 54 + [Math]::Floor($index / 4) * 250
            $graphics.DrawImage($image, [int]$x, [int]$y, 208, 208)
            $graphics.DrawString($labels[$index], $font, [System.Drawing.Brushes]::White, [single]$x, [single]($y + 214))
        } finally { $image.Dispose() }
    }
    $preview = Join-Path $ProjectRoot 'build/road-markings-preview.png'
    $canvas.Save($preview, [System.Drawing.Imaging.ImageFormat]::Png)
    $textureCount = @(Get-ChildItem -LiteralPath (Join-Path $generated 'textures/block') -Filter 'road_*.png').Count
    Write-Output "PASS: $($ids.Count) blocks, $textureCount original textures, 3 languages, all models, side projections and diagonal endpoints."
    Write-Output "Preview: $preview"
} finally {
    $font.Dispose()
    $graphics.Dispose()
    $canvas.Dispose()
}
