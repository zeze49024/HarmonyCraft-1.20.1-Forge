package com.zeze.harmony.datagen;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Original vector reconstruction of GB 5768.3-2025, table 1 and figure 42 (cm).
 * 300 cm -> 60 texture rows on the 2.25-block plane; the SAME world-space scale
 * is used across X and Z. Square item sprites are not the physical aspect ratio.
 * See ROAD_MARKINGS.md for the source and the deliberate Minecraft scale choice.
 */
final class RoadArrowArtwork {
    private static final int WHITE = 0xfff5f5f2;
    private static final double PIXELS_PER_CM_X = 0.36;
    private static final double PIXELS_PER_CM_Z = 0.20;

    private RoadArrowArtwork() { }

    static BufferedImage render(String name) {
        String type = name.replace("road_arrow_", "");
        String leftHanded = switch (type) {
            case "right" -> "left";
            case "straight_right" -> "straight_left";
            case "merge_right" -> "merge_left";
            default -> null;
        };
        if (leftHanded != null) {
            // Rasterize once, then reflect exact texels. Area.contains() can make
            // opposite inclusion decisions on a floating-point polygon boundary.
            BufferedImage left = render("road_arrow_" + leftHanded);
            BufferedImage reflected = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < 64; x++) {
                for (int z = 0; z < 64; z++) {
                    reflected.setRGB(x, z, left.getRGB(63 - x, z));
                }
            }
            return reflected;
        }
        Area shape = shape(type);
        Rectangle2D bounds = shape.getBounds2D();
        double center = bounds.getCenterX();
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        // Pixel-center sampling makes independently mirrored silhouettes exact
        // X reflections. Cutout sprites have no translucent fringes or dark halo.
        for (int x = 0; x < 64; x++) {
            for (int z = 0; z < 64; z++) {
                double worldX = (x + 0.5 - 32) / PIXELS_PER_CM_X + center;
                double worldZ = (z + 0.5 - 2) / PIXELS_PER_CM_Z;
                if (shape.contains(worldX, worldZ)) {
                    image.setRGB(x, z, WHITE);
                }
            }
        }
        return image;
    }

    private static Area shape(String type) {
        return switch (type) {
            case "straight" -> straight();
            case "left" -> left();
            case "straight_left" -> with(straight(), leftBranch());
            case "left_right" -> with(left(), reflect(left()));
            case "uturn" -> uturn();
            case "straight_uturn" -> with(straight(), uturnBranch());
            case "left_uturn" -> with(left(), uturnBranch());
            case "merge_left" -> mergeLeft();
            default -> throw new IllegalArgumentException("Unknown GB road arrow: " + type);
        };
    }

    private static Area straight() {
        // Figure 42: length 300, head length 120, shaft 15, head 15+15+15.
        return polygon(0, 0, 22.5, 120, 7.5, 120, 7.5, 300,
                -7.5, 300, -7.5, 120, -22.5, 120);
    }

    private static Area left() {
        // 20/40/15 transverse dimensions; 50/30/30/45 head segments;
        // 135 tail and 60 difference between the two branch junctions.
        return polygon(-47.5, 0, -47.5, 50, 7.5, 105, 7.5, 300,
                -7.5, 300, -7.5, 165, -47.5, 110, -47.5, 155, -67.5, 80);
    }

    private static Area leftBranch() {
        // Straight/left figure: branch begins at 130, 45-degree diagonals,
        // leaves a 20-cm tail below the branch and retains the 15-cm main shaft.
        return polygon(-47.5, 130, -47.5, 180, -7.5, 220,
                -7.5, 280, -47.5, 240, -47.5, 285, -67.5, 210);
    }

    private static Area uturn() {
        // Figure 42: 90 outer arch span, 45 outer radius, 20 shaft;
        // head begins at 100, length 120, three 20-cm transverse segments.
        Area result = upperRing(45, 45, 45, 25);
        result.add(new Area(new Rectangle2D.Double(0, 45, 20, 55)));
        result.add(new Area(new Rectangle2D.Double(70, 45, 20, 255)));
        result.add(polygon(-20, 100, 40, 100, 10, 220));
        return result;
    }

    private static Area uturnBranch() {
        // Combination arrows: 15-cm shaft, 60+15 overall transverse span,
        // 147-cm down-facing head ending level with the main arrow's tail.
        Area result = upperRing(-22.5, 153, 30, 15);
        result.add(polygon(-67.5, 153, -22.5, 153, -45, 300));
        return result;
    }

    private static Area mergeLeft() {
        // Figure 42: head length 120, shaft 15, transverse span 60+15.
        double angle = Math.toRadians(20);
        double dx = Math.sin(angle);
        double dz = Math.cos(angle);
        double tipX = -67.5;
        double baseX = tipX + 120 * dx;
        double baseZ = 120 * dz;
        Path2D.Double shaft = new Path2D.Double();
        shaft.moveTo(0, 300);
        shaft.curveTo(0, 225, baseX + 25 * dx, baseZ + 25 * dz, baseX, baseZ);
        Area result = new Area(new BasicStroke(15, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)
                .createStrokedShape(shaft));
        result.add(polygon(tipX, 0, baseX + 22.5 * dz, baseZ - 22.5 * dx,
                baseX - 22.5 * dz, baseZ + 22.5 * dx));
        return result;
    }

    private static Area upperRing(double x, double z, double outer, double inner) {
        Area ring = new Area(new Ellipse2D.Double(x - outer, z - outer, 2 * outer, 2 * outer));
        ring.subtract(new Area(new Ellipse2D.Double(x - inner, z - inner, 2 * inner, 2 * inner)));
        ring.intersect(new Area(new Rectangle2D.Double(x - outer, z - outer, 2 * outer, outer)));
        return ring;
    }

    private static Area reflect(Shape shape) {
        return new Area(AffineTransform.getScaleInstance(-1, 1).createTransformedShape(shape));
    }

    private static Area with(Area first, Area second) {
        first.add(second);
        return first;
    }

    private static Area polygon(double... points) {
        Path2D.Double polygon = new Path2D.Double();
        polygon.moveTo(points[0], points[1]);
        for (int i = 2; i < points.length; i += 2) {
            polygon.lineTo(points[i], points[i + 1]);
        }
        polygon.closePath();
        return new Area(polygon);
    }
}
