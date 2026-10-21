package com.zeze.harmony.gametest;

import com.mojang.logging.LogUtils;
import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import com.zeze.harmony.block.RoadArrowBlock;
import com.zeze.harmony.block.RoadLineBlock;
import com.zeze.harmony.block.RoadLineSlabBlock;
import com.zeze.harmony.block.ReversibleRoadArrowBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

/** Development-only client check: real atlas/model loading, then orderly shutdown. */
@Mod.EventBusSubscriber(modid = HarmonyCraftMod.MOD_ID, value = Dist.CLIENT)
public final class ClientResourceCheck {
    private static boolean completed;

    @SubscribeEvent
    public static void checkModels(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("harmonycraft.resourceCheck") || completed || event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        // Wait until the initial resource loading screen has completed.
        if (minecraft.getOverlay() != null) {
            return;
        }
        completed = true;
        int checked = 0;
        for (var registered : ModBlocks.BLOCKS.getEntries()) {
            for (BlockState state : registered.get().getStateDefinition().getPossibleStates()) {
                BakedModel model = minecraft.getBlockRenderer().getBlockModelShaper().getBlockModel(state);
                require(model != minecraft.getModelManager().getMissingModel(), "Missing model: " + state);
                var quads = model.getQuads(state, null, RandomSource.create(0));
                int quadCount = quads.size();
                for (Direction direction : Direction.values()) {
                    quadCount += model.getQuads(state, direction, RandomSource.create(0)).size();
                }
                require(quadCount > 0, "Empty model: " + state);
                for (ModBlocks.RoadVariant road : ModBlocks.ROAD_MARKINGS) {
                    if (state.is(road.block().get()) || state.is(road.slab().get())) {
                        checkRoadFaces(state, model, road);
                    }
                }
                if (state.getBlock() instanceof RoadArrowBlock) {
                    require(quads.size() == 2, "Arrow must retain both unculled faces: " + state);
                    float height = (state.getValue(RoadArrowBlock.LOWERED) ? -7.95F : 0.05F) / 16;
                    for (BakedQuad quad : quads) {
                        int[] vertices = quad.getVertices();
                        int stride = vertices.length / 4;
                        for (int vertex = 0; vertex < 4; vertex++) {
                            require(Math.abs(Float.intBitsToFloat(vertices[vertex * stride + 1]) - height) < 0.00001,
                                    "Arrow height was changed during baking: " + state);
                        }
                        require(quad.getSprite() != minecraft.getModelManager().getMissingModel().getParticleIcon(),
                                "Arrow sprite is missing: " + state);
                    }
                    if (state.hasProperty(ReversibleRoadArrowBlock.MIRRORED)
                            && state.getValue(ReversibleRoadArrowBlock.MIRRORED)) {
                        BlockState original = state.setValue(ReversibleRoadArrowBlock.MIRRORED, false);
                        var baseQuads = minecraft.getBlockRenderer().getBlockModelShaper().getBlockModel(original)
                                .getQuads(original, null, RandomSource.create(0));
                        for (BakedQuad reflected : quads) {
                            BakedQuad base = baseQuads.stream().filter(quad -> quad.getDirection() == reflected.getDirection())
                                    .findFirst().orElseThrow();
                            int[] reflectedVertices = reflected.getVertices();
                            int[] baseVertices = base.getVertices();
                            int stride = reflectedVertices.length / 4;
                            float uCenterTwice = reflected.getSprite().getU0() + reflected.getSprite().getU1();
                            for (int vertex = 0; vertex < 4; vertex++) {
                                int offset = vertex * stride;
                                for (int coordinate = 0; coordinate < 3; coordinate++) {
                                    require(reflectedVertices[offset + coordinate] == baseVertices[offset + coordinate],
                                            "Mirrored U-turn changed physical geometry: " + state);
                                }
                                float u = Float.intBitsToFloat(reflectedVertices[offset + 4]);
                                float baseU = Float.intBitsToFloat(baseVertices[offset + 4]);
                                require(Math.abs(u + baseU - uCenterTwice) < 0.00005F,
                                        "Mirrored U-turn must reflect U: " + state);
                                require(Math.abs(Float.intBitsToFloat(reflectedVertices[offset + 5])
                                                - Float.intBitsToFloat(baseVertices[offset + 5])) < 0.00005F,
                                        "Mirrored U-turn must retain V: " + state);
                            }
                        }
                    }
                }
                checked++;
            }
        }
        LogUtils.getLogger().info("HARMONYCRAFT CLIENT RESOURCE CHECK PASSED: {} baked block states", checked);
        minecraft.stop();
    }

    private static void checkRoadFaces(BlockState state, BakedModel model, ModBlocks.RoadVariant road) {
        var allFaces = new ArrayList<>(model.getQuads(state, null, RandomSource.create(0)));
        for (Direction face : Direction.values()) {
            allFaces.addAll(model.getQuads(state, face, RandomSource.create(0)));
        }
        require(allFaces.size() == 6, "A road must bake all six faces: " + state);
        Direction facing = state.getValue(state.is(road.block().get()) ? RoadLineBlock.FACING : RoadLineSlabBlock.FACING);
        Rotation rotation = switch (facing) {
            case NORTH -> Rotation.NONE;
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default -> throw new IllegalStateException("Not a horizontal facing");
        };
        for (Direction localFace : Direction.values()) {
            Direction worldFace = rotation.rotate(localFace);
            var faces = allFaces.stream().filter(quad -> quad.getDirection() == worldFace).toList();
            require(faces.size() == 1, "Incorrect rotated road face: " + state + "/" + worldFace);
            String texture = switch (localFace) {
                case UP -> "road_" + road.textureName();
                case DOWN -> "asphalt_block";
                default -> road.paintedEdges().contains(localFace)
                        ? "road_" + road.textureName() + "_side_" + localFace.getName() : "asphalt_block";
            };
            require(faces.get(0).getSprite().contents().name().toString()
                            .equals(HarmonyCraftMod.MOD_ID + ":block/" + texture),
                    "Wrong painted/plain road face after rotation: " + state + "/" + worldFace);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
