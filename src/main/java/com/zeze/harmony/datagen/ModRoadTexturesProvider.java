package com.zeze.harmony.datagen;

import com.google.common.hash.Hashing;
import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/** Deterministic original artwork using only HarmonyCraft's own asphalt source image. */
public class ModRoadTexturesProvider implements DataProvider {
    private static final Color WHITE = new Color(245, 245, 242);
    private static final Color YELLOW = new Color(244, 211, 35);
    private final PackOutput.PathProvider paths;

    public ModRoadTexturesProvider(PackOutput output, ExistingFileHelper helper) {
        paths = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures");
        ModBlocks.ROAD_MARKINGS.forEach(road -> {
            helper.trackGenerated(texture("road_" + road.textureName()), PackType.CLIENT_RESOURCES, ".png", "textures");
            road.paintedEdges().forEach(edge -> helper.trackGenerated(
                    texture("road_" + road.textureName() + "_side_" + edge.getName()),
                    PackType.CLIENT_RESOURCES, ".png", "textures"));
        });
        ModBlocks.ROAD_ARROWS.forEach(arrow -> helper.trackGenerated(texture(arrow.getId().getPath()),
                PackType.CLIENT_RESOURCES, ".png", "textures"));
        for (String name : new String[]{"asphalt_cracked", "asphalt_patched", "asphalt_pothole",
                "asphalt_new", "asphalt_faded", "road_manhole_round", "road_manhole_square",
                "road_drain_round", "road_drain_square"}) {
            helper.trackGenerated(texture(name), PackType.CLIENT_RESOURCES, ".png", "textures");
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        try (InputStream input = ModRoadTexturesProvider.class.getResourceAsStream(
                "/assets/" + HarmonyCraftMod.MOD_ID + "/textures/block/asphalt_block.png")) {
            if (input == null) {
                throw new IOException("Missing HarmonyCraft asphalt source texture");
            }
            BufferedImage asphalt = ImageIO.read(input);
            if (asphalt == null || asphalt.getWidth() != 64 || asphalt.getHeight() != 64) {
                throw new IOException("The asphalt source texture must be a 64 x 64 PNG");
            }
            for (ModBlocks.RoadVariant road : ModBlocks.ROAD_MARKINGS) {
                BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    graphics.drawImage(asphalt, 0, 0, null);
                    graphics.setColor(paintColor(road));
                    if (road.textureName().endsWith("solid_line")) {
                        graphics.fillRect(29, 0, 6, 64);
                    } else if (road.textureName().endsWith("dashed_line")) {
                        graphics.fillRect(29, 16, 6, 32);
                    } else if (road.textureName().endsWith("double_line")) {
                        graphics.fillRect(23, 0, 6, 64);
                        graphics.fillRect(35, 0, 6, 64);
                    } else if (road.isDiagonal()) {
                        // Center on x-z=32 (or 26/38), not on a half-pixel-biased band.
                        // Half-covered boundary pixels keep six-pixel width AND 180-degree symmetry.
                        for (int x = 0; x < 64; x++) {
                            for (int z = 0; z < 64; z++) {
                                image.setRGB(x, z, blend(image.getRGB(x, z), graphics.getColor().getRGB(),
                                        diagonalOpacity(road, x, z)));
                            }
                        }
                    } else if (road.isCorner()) {
                        for (int x = 0; x < 64; x++) {
                            for (int z = 0; z < 64; z++) {
                                image.setRGB(x, z, blend(image.getRGB(x, z), graphics.getColor().getRGB(),
                                        cornerOpacity(road, x, z)));
                            }
                        }
                    } else if (road.textureName().equals("stop_line")) {
                        graphics.fillRect(0, 0, 64, 20);
                    } else if (road.textureName().equals("crosswalk")) {
                        graphics.fillRect(0, 20, 64, 24);
                    } else if (road.textureName().equals("reversible_lane_zigzag")) {
                        graphics.setStroke(new BasicStroke(6, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
                        Path2D zigzag = new Path2D.Double();
                        zigzag.moveTo(32, 0); zigzag.lineTo(20, 16); zigzag.lineTo(44, 32);
                        zigzag.lineTo(20, 48); zigzag.lineTo(32, 64);
                        graphics.draw(zigzag);
                    } else {
                        throw new IOException("Unknown road pattern: " + road.textureName());
                    }
                } finally {
                    graphics.dispose();
                }
                save(output, "road_" + road.textureName(), image);
                for (Direction edge : road.paintedEdges()) {
                    save(output, "road_" + road.textureName() + "_side_" + edge.getName(), side(asphalt, image, road, edge));
                }
            }
            for (var arrow : ModBlocks.ROAD_ARROWS) {
                String name = arrow.getId().getPath();
                save(output, name, RoadArrowArtwork.render(name));
            }
            save(output, "asphalt_cracked", cracked(asphalt));
            save(output, "asphalt_patched", patched(asphalt));
            save(output, "asphalt_pothole", pothole(asphalt));
            save(output, "asphalt_new", recolor(asphalt, 0.72, 0));
            save(output, "asphalt_faded", recolor(asphalt, 0.72, 46));
            save(output, "road_manhole_round", surfaceFeature(asphalt, "manhole", true));
            save(output, "road_manhole_square", surfaceFeature(asphalt, "manhole", false));
            save(output, "road_drain_round", surfaceFeature(asphalt, "drain", true));
            save(output, "road_drain_square", surfaceFeature(asphalt, "drain", false));
            return CompletableFuture.completedFuture(null);
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private static int diagonalOpacity(ModBlocks.RoadVariant road, int x, int z) {
        int offset = x - z;
        int distance = road.isDoubleLine() ? Math.min(Math.abs(offset - 26), Math.abs(offset - 38))
                : Math.abs(offset - 32);
        return distance < 3 ? 255 : distance == 3 ? 128 : 0;
    }

    private static int cornerOpacity(ModBlocks.RoadVariant road, int x, int z) {
        // Concentric quarter-rings around the NE corner. Both ends match straight
        // lines exactly at pixel centers; double lines cannot cross inside the bend.
        // Curve interior uses 8x8 coverage, but tangent edge rows retain exact width.
        boolean curved = road.textureName().endsWith("curved_line");
        int samples = curved && x != 63 && z != 0 ? 8 : 1;
        int covered = 0;
        for (int sx = 0; sx < samples; sx++) {
            for (int sz = 0; sz < samples; sz++) {
                double dx = 64 - x - (sx + 0.5) / samples;
                double dz = z + (sz + 0.5) / samples;
                double radius = curved ? Math.hypot(dx, dz) : Math.max(dx, dz);
                double distance = road.isDoubleLine() ? Math.min(Math.abs(radius - 26), Math.abs(radius - 38))
                        : Math.abs(radius - 32);
                if (distance < 3) {
                    covered++;
                }
            }
        }
        return (covered * 255 + samples * samples / 2) / (samples * samples);
    }

    private static int blend(int background, int paint, int opacity) {
        int result = 0xff000000;
        for (int shift : new int[]{16, 8, 0}) {
            int original = (background >> shift) & 255;
            int marking = (paint >> shift) & 255;
            result |= ((original * (255 - opacity) + marking * opacity + 127) / 255) << shift;
        }
        return result;
    }

    private static BufferedImage side(BufferedImage asphalt, BufferedImage top, ModBlocks.RoadVariant road, Direction edge) {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.drawImage(asphalt, 0, 0, null);
            for (int u = 0; u < 64; u++) {
                // North/east face UVs run opposite world X/Z; south/west run forwards.
                int x = switch (edge) {
                    case NORTH -> 63 - u;
                    case EAST -> 63;
                    case SOUTH -> u;
                    case WEST -> 0;
                    default -> throw new IllegalArgumentException("Not a horizontal road edge: " + edge);
                };
                int z = switch (edge) {
                    case NORTH -> 0;
                    case EAST -> 63 - u;
                    case SOUTH -> 63;
                    case WEST -> u;
                    default -> throw new IllegalArgumentException("Not a horizontal road edge: " + edge);
                };
                int pixel = top.getRGB(x, z);
                int opacity = road.isDiagonal() ? diagonalOpacity(road, x, z)
                        : road.isCorner() ? cornerOpacity(road, x, z)
                        : (pixel == WHITE.getRGB() || pixel == YELLOW.getRGB() ? 255 : 0);
                if (opacity > 0) {
                    int paint = paintColor(road).getRGB();
                    for (int v = 0; v < 64; v++) {
                        image.setRGB(u, v, blend(image.getRGB(u, v), paint, opacity));
                    }
                }
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static Color paintColor(ModBlocks.RoadVariant road) {
        return road.textureName().startsWith("yellow") || road.textureName().startsWith("bus_") ? YELLOW : WHITE;
    }

    private static BufferedImage copy(BufferedImage source) {
        BufferedImage result = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        try { graphics.drawImage(source, 0, 0, null); } finally { graphics.dispose(); }
        return result;
    }

    private static BufferedImage cracked(BufferedImage asphalt) {
        BufferedImage image = copy(asphalt);
        Graphics2D graphics = image.createGraphics();
        try {
            Path2D main = path(4, 0, 12, 8, 10, 17, 21, 25, 18, 34, 30, 42, 28, 51, 39, 64);
            Path2D west = path(11, 17, 4, 22, 0, 31);
            Path2D east = path(20, 25, 31, 19, 42, 20, 51, 13);
            Path2D fork = path(30, 42, 42, 37, 51, 40, 62, 34);
            Path2D lower = path(28, 51, 18, 57, 9, 55, 1, 62);
            Path2D hairline = path(18, 34, 10, 40, 5, 49);
            for (Path2D crack : new Path2D[]{main, west, east, fork, lower, hairline}) {
                graphics.setColor(new Color(92, 87, 78, 115));
                graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                graphics.draw(crack);
                graphics.setColor(new Color(13, 15, 18, 235));
                graphics.setStroke(new BasicStroke(1.25F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                graphics.draw(crack);
            }
            graphics.setColor(new Color(8, 10, 12));
            for (int[] chip : new int[][]{{9, 16}, {19, 25}, {29, 42}, {41, 37}, {28, 51}}) {
                graphics.fillRect(chip[0] - 1, chip[1] - 1, 3, 3);
            }
        } finally { graphics.dispose(); }
        return image;
    }

    private static BufferedImage patched(BufferedImage asphalt) {
        BufferedImage image = copy(asphalt);
        Polygon patch = new Polygon(new int[]{9, 14, 28, 47, 55, 53, 45, 24, 10, 6},
                new int[]{16, 9, 8, 11, 20, 43, 54, 56, 50, 31}, 10);
        for (int x = 0; x < 64; x++) {
            for (int z = 0; z < 64; z++) {
                if (patch.contains(x + 0.5, z + 0.5)) {
                    int noise = pixelNoise(x, z, 31, 8);
                    image.setRGB(x, z, shade(asphalt.getRGB(x, z), -18 + noise, -16 + noise, -13 + noise));
                }
            }
        }
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(17, 18, 20, 220));
            graphics.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawPolygon(patch);
            graphics.setColor(new Color(88, 85, 78, 190));
            graphics.setStroke(new BasicStroke(1));
            graphics.drawPolygon(patch);
            graphics.setColor(new Color(25, 26, 29, 135));
            graphics.setStroke(new BasicStroke(1));
            graphics.drawLine(12, 24, 51, 25);
            graphics.drawLine(10, 39, 51, 41);
            graphics.setColor(new Color(102, 101, 96, 120));
            for (int[] aggregate : new int[][]{{17, 17}, {35, 15}, {47, 31}, {19, 47}, {37, 48}, {29, 31}}) {
                graphics.fillRect(aggregate[0], aggregate[1], 2, 1);
            }
        } finally { graphics.dispose(); }
        return image;
    }

    private static BufferedImage pothole(BufferedImage asphalt) {
        BufferedImage image = copy(asphalt);
        Polygon rim = new Polygon(new int[]{8, 14, 24, 37, 50, 57, 55, 48, 35, 21, 11, 5},
                new int[]{22, 13, 9, 11, 17, 29, 41, 50, 56, 53, 46, 34}, 12);
        Polygon cavity = new Polygon(new int[]{13, 20, 31, 45, 51, 48, 37, 24, 14, 10},
                new int[]{25, 18, 16, 20, 29, 40, 48, 47, 41, 32}, 10);
        for (int x = 0; x < 64; x++) {
            for (int z = 0; z < 64; z++) {
                if (cavity.contains(x + 0.5, z + 0.5)) {
                    double depth = Math.min(1, Math.hypot((x - 31) / 24.0, (z - 33) / 19.0));
                    int base = 15 + (int) Math.round(depth * 22) + pixelNoise(x, z, 71, 5);
                    image.setRGB(x, z, new Color(clamp(base), clamp(base + 2), clamp(base + 4)).getRGB());
                } else if (rim.contains(x + 0.5, z + 0.5)) {
                    int noise = pixelNoise(x, z, 57, 12);
                    image.setRGB(x, z, shade(asphalt.getRGB(x, z), 12 + noise, 9 + noise, 5 + noise));
                }
            }
        }
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(new Color(103, 95, 82, 190));
            graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawPolygon(rim);
            graphics.setColor(new Color(7, 9, 11, 235));
            graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawPolygon(cavity);
            graphics.setColor(new Color(91, 84, 72));
            for (int[] stone : new int[][]{{15, 28, 3, 2}, {45, 25, 2, 3}, {20, 44, 3, 2},
                    {39, 45, 2, 2}, {28, 20, 2, 2}, {50, 36, 2, 2}, {9, 36, 2, 3}}) {
                graphics.fillRect(stone[0], stone[1], stone[2], stone[3]);
                graphics.setColor(new Color(42, 40, 38));
                graphics.drawLine(stone[0], stone[1] + stone[3], stone[0] + stone[2], stone[1] + stone[3]);
                graphics.setColor(new Color(91, 84, 72));
            }
        } finally { graphics.dispose(); }
        return image;
    }

    private static Path2D path(double... coordinates) {
        Path2D path = new Path2D.Double();
        path.moveTo(coordinates[0], coordinates[1]);
        for (int index = 2; index < coordinates.length; index += 2) {
            path.lineTo(coordinates[index], coordinates[index + 1]);
        }
        return path;
    }

    private static int pixelNoise(int x, int z, int seed, int amplitude) {
        int hash = x * 0x1f1f1f1f ^ z * 0x45d9f3b ^ seed * 0x27d4eb2d;
        hash ^= hash >>> 16;
        return Math.floorMod(hash, amplitude * 2 + 1) - amplitude;
    }

    private static int shade(int argb, int redDelta, int greenDelta, int blueDelta) {
        Color color = new Color(argb, true);
        return new Color(clamp(color.getRed() + redDelta), clamp(color.getGreen() + greenDelta),
                clamp(color.getBlue() + blueDelta), color.getAlpha()).getRGB();
    }

    private static BufferedImage recolor(BufferedImage asphalt, double saturation, int brightness) {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < 64; x++) {
            for (int z = 0; z < 64; z++) {
                Color source = new Color(asphalt.getRGB(x, z), true);
                int gray = (source.getRed() * 30 + source.getGreen() * 59 + source.getBlue() * 11) / 100;
                int red = clamp((int) Math.round(gray + (source.getRed() - gray) * saturation) + brightness);
                int green = clamp((int) Math.round(gray + (source.getGreen() - gray) * saturation) + brightness);
                int blue = clamp((int) Math.round(gray + (source.getBlue() - gray) * saturation) + brightness);
                image.setRGB(x, z, new Color(red, green, blue, source.getAlpha()).getRGB());
            }
        }
        return image;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static BufferedImage surfaceFeature(BufferedImage asphalt, String type, boolean round) {
        BufferedImage image = copy(asphalt);
        for (int x = 0; x < 64; x++) {
            for (int z = 0; z < 64; z++) {
                boolean inside = round ? Math.hypot(x + 0.5 - 32, z + 0.5 - 32) <= 23
                        : x >= 9 && x <= 54 && z >= 9 && z <= 54;
                if (inside) {
                    int noise = pixelNoise(x, z, type.equals("manhole") ? 113 : 127, 7);
                    int base = type.equals("manhole") ? 49 : 44;
                    image.setRGB(x, z, new Color(clamp(base + noise), clamp(base + 3 + noise),
                            clamp(base + 5 + noise)).getRGB());
                }
            }
        }
        Graphics2D graphics = image.createGraphics();
        try {
            Shape outer = round ? new Ellipse2D.Double(7, 7, 50, 50) : new Rectangle2D.Double(7, 7, 50, 50);
            Shape inner = round ? new Ellipse2D.Double(11, 11, 42, 42) : new Rectangle2D.Double(11, 11, 42, 42);
            graphics.setColor(new Color(16, 18, 20));
            graphics.setStroke(new BasicStroke(5));
            graphics.draw(outer);
            graphics.setColor(new Color(116, 121, 122));
            graphics.setStroke(new BasicStroke(2));
            graphics.draw(outer);
            graphics.setColor(new Color(73, 78, 80));
            graphics.setStroke(new BasicStroke(2));
            graphics.draw(inner);

            if (type.equals("manhole")) {
                Shape oldClip = graphics.getClip();
                graphics.setClip(round ? new Ellipse2D.Double(13, 13, 38, 38) : new Rectangle2D.Double(13, 13, 38, 38));
                graphics.setStroke(new BasicStroke(2));
                for (int offset = -28; offset <= 64; offset += 8) {
                    graphics.setColor(new Color(20, 23, 25));
                    graphics.drawLine(offset, 53, offset + 45, 8);
                    graphics.drawLine(offset, 8, offset + 45, 53);
                    graphics.setColor(new Color(78, 82, 83));
                    graphics.drawLine(offset + 2, 53, offset + 47, 8);
                    graphics.drawLine(offset + 2, 8, offset + 47, 53);
                }
                graphics.setClip(oldClip);
                graphics.setColor(new Color(25, 28, 30));
                graphics.setStroke(new BasicStroke(3));
                if (round) graphics.drawOval(20, 20, 24, 24); else graphics.drawRect(20, 20, 24, 24);
                graphics.setColor(new Color(99, 103, 103));
                graphics.setStroke(new BasicStroke(1));
                if (round) graphics.drawOval(21, 21, 22, 22); else graphics.drawRect(21, 21, 22, 22);
                graphics.setColor(new Color(22, 24, 26));
                graphics.fillRect(29, 24, 6, 16);
                graphics.fillRect(24, 29, 16, 6);
            } else {
                Shape oldClip = graphics.getClip();
                graphics.setClip(round ? new Ellipse2D.Double(13, 13, 38, 38) : new Rectangle2D.Double(13, 13, 38, 38));
                for (int value = 16; value <= 48; value += 8) {
                    graphics.setColor(new Color(12, 14, 16));
                    graphics.fillRoundRect(value, 11, 5, 43, 2, 2);
                    graphics.setColor(new Color(91, 96, 98));
                    graphics.fillRect(value + 1, 13, 1, 38);
                }
                graphics.setClip(oldClip);
                graphics.setColor(new Color(24, 27, 29));
                graphics.fillRect(12, 29, 40, 5);
                graphics.setColor(new Color(96, 101, 102));
                graphics.drawLine(13, 29, 51, 29);
            }
            for (int[] bolt : new int[][]{{16, 16}, {48, 16}, {16, 48}, {48, 48}}) {
                if (round && Math.hypot(bolt[0] - 32, bolt[1] - 32) > 23) continue;
                graphics.setColor(new Color(15, 17, 19));
                graphics.fillOval(bolt[0] - 2, bolt[1] - 2, 5, 5);
                graphics.setColor(new Color(135, 139, 139));
                graphics.fillRect(bolt[0] - 1, bolt[1] - 1, 2, 1);
            }
        } finally { graphics.dispose(); }
        return image;
    }

    private void save(CachedOutput output, String name, BufferedImage image) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        byte[] png = bytes.toByteArray();
        output.writeIfNeeded(paths.file(texture(name), "png"), png, Hashing.sha1().hashBytes(png));
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(HarmonyCraftMod.MOD_ID, "block/" + name);
    }

    @Override
    public String getName() {
        return "HarmonyCraft original road textures";
    }
}
