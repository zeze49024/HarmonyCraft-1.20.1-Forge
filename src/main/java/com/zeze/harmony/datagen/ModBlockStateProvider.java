package com.zeze.harmony.datagen;

import com.zeze.harmony.HarmonyCraftMod;
import com.zeze.harmony.block.ModBlocks;
import com.zeze.harmony.block.RoadArrowBlock;
import com.zeze.harmony.block.RoadLineBlock;
import com.zeze.harmony.block.RoadLineSlabBlock;
import com.zeze.harmony.block.ReversibleRoadArrowBlock;
import com.zeze.harmony.block.RoadSurfaceFeatureBlock;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

//方块状态文件
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, HarmonyCraftMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        randomAsphaltModels();
        ModBlocks.ASPHALT_VARIANTS.forEach(this::asphaltVariantModels);
        ModBlocks.ROAD_MARKINGS.forEach(this::roadModels);
        ModBlocks.ROAD_ARROWS.forEach(this::arrowModels);
        surfaceFeatureModels(ModBlocks.ROAD_MANHOLE, "road_manhole");
        surfaceFeatureModels(ModBlocks.ROAD_DRAIN, "road_drain");
    }

    private void randomAsphaltModels() {
        ResourceLocation asphalt = blockTexture(ModBlocks.ASPHALT_BLOCK.get());
        String[] textureNames = {"asphalt_block", "asphalt_cracked", "asphalt_patched", "asphalt_pothole"};
        ModelFile[] full = new ModelFile[textureNames.length];
        ModelFile[] bottom = new ModelFile[textureNames.length];
        ModelFile[] top = new ModelFile[textureNames.length];
        for (int index = 0; index < textureNames.length; index++) {
            ResourceLocation surface = modLoc("block/" + textureNames[index]);
            String suffix = index == 0 ? "" : "_" + textureNames[index].substring("asphalt_".length());
            full[index] = plainRoadShape("asphalt_block" + suffix, asphalt, surface, 0, 16);
            bottom[index] = plainRoadShape("asphalt_slab" + suffix, asphalt, surface, 0, 8);
            top[index] = plainRoadShape("asphalt_slab_top" + suffix, asphalt, surface, 8, 16);
        }
        getVariantBuilder(ModBlocks.ASPHALT_BLOCK.get()).partialState().setModels(weighted(full));
        getVariantBuilder(ModBlocks.ASPHALT_SLAB.get()).forAllStatesExcept(state -> weighted(switch (state.getValue(SlabBlock.TYPE)) {
            case BOTTOM -> bottom;
            case TOP -> top;
            case DOUBLE -> full;
        }), SlabBlock.WATERLOGGED);
        simpleBlockItem(ModBlocks.ASPHALT_BLOCK.get(), full[0]);
        simpleBlockItem(ModBlocks.ASPHALT_SLAB.get(), bottom[0]);
    }

    private static ConfiguredModel[] weighted(ModelFile[] models) {
        // 835 + 55 + 55 + 55 = 1000: each defect is 5.5%, for a combined 16.5%.
        return ConfiguredModel.builder().modelFile(models[0]).weight(835).nextModel()
                .modelFile(models[1]).weight(55).nextModel()
                .modelFile(models[2]).weight(55).nextModel()
                .modelFile(models[3]).weight(55).build();
    }

    private void asphaltVariantModels(ModBlocks.AsphaltVariant variant) {
        ResourceLocation texture = modLoc("block/" + variant.textureName());
        ModelFile full = plainRoadShape(variant.block().getId().getPath(), texture, texture, 0, 16);
        ModelFile bottom = plainRoadShape(variant.slab().getId().getPath(), texture, texture, 0, 8);
        ModelFile top = plainRoadShape(variant.slab().getId().getPath() + "_top", texture, texture, 8, 16);
        simpleBlock(variant.block().get(), full);
        getVariantBuilder(variant.slab().get()).forAllStatesExcept(state -> new ConfiguredModel[]{new ConfiguredModel(
                state.getValue(SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.BOTTOM ? bottom
                        : state.getValue(SlabBlock.TYPE) == net.minecraft.world.level.block.state.properties.SlabType.TOP ? top : full)},
                SlabBlock.WATERLOGGED);
        simpleBlockItem(variant.block().get(), full);
        simpleBlockItem(variant.slab().get(), bottom);
    }

    private void roadModels(ModBlocks.RoadVariant road) {
        ResourceLocation asphalt = blockTexture(ModBlocks.ASPHALT_BLOCK.get());
        ResourceLocation top = modLoc("block/road_" + road.textureName());
        ModelFile full = roadShape(road.block().getId().getPath(), road, asphalt, top, 0, 16);
        ModelFile bottom = roadShape(road.slab().getId().getPath(), road, asphalt, top, 0, 8);
        ModelFile upper = roadShape(road.slab().getId().getPath() + "_top", road, asphalt, top, 8, 16);
        getVariantBuilder(road.block().get()).forAllStates(state -> new ConfiguredModel[]{
                new ConfiguredModel(full, 0, rotation(state.getValue(RoadLineBlock.FACING)), false)});
        getVariantBuilder(road.slab().get()).forAllStatesExcept(state -> {
            ModelFile model = switch (state.getValue(SlabBlock.TYPE)) {
                case BOTTOM -> bottom;
                case TOP -> upper;
                case DOUBLE -> full;
            };
            return new ConfiguredModel[]{new ConfiguredModel(model, 0,
                    rotation(state.getValue(RoadLineSlabBlock.FACING)), false)};
        }, SlabBlock.WATERLOGGED);
        simpleBlockItem(road.block().get(), full);
        simpleBlockItem(road.slab().get(), bottom);
    }

    private ModelFile roadShape(String name, ModBlocks.RoadVariant road, ResourceLocation asphalt,
                                ResourceLocation top, int bottomY, int topY) {
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/block"))
                .texture("side", asphalt).texture("bottom", asphalt).texture("top", top).texture("particle", asphalt);
        for (Direction edge : road.paintedEdges()) {
            model.texture(edge.getName(), modLoc("block/road_" + road.textureName() + "_side_" + edge.getName()));
        }
        var element = model.element().from(0, bottomY, 0).to(16, topY, 16);
        for (Direction edge : Direction.Plane.HORIZONTAL) {
            element.face(edge).texture(road.paintedEdges().contains(edge) ? "#" + edge.getName() : "#side")
                    .uvs(0, 16 - topY, 16, 16 - bottomY).cullface(edge).end();
        }
        var upperFace = element.face(Direction.UP).texture("#top").uvs(0, 0, 16, 16);
        if (topY == 16) {
            upperFace.cullface(Direction.UP);
        }
        upperFace.end();
        var lowerFace = element.face(Direction.DOWN).texture("#bottom").uvs(0, 0, 16, 16);
        if (bottomY == 0) {
            lowerFace.cullface(Direction.DOWN);
        }
        lowerFace.end();
        element.end();
        return model;
    }

    private ModelFile plainRoadShape(String name, ResourceLocation side, ResourceLocation top, int bottomY, int topY) {
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/block"))
                .texture("side", side).texture("bottom", side).texture("top", top).texture("particle", top);
        var element = model.element().from(0, bottomY, 0).to(16, topY, 16);
        for (Direction edge : Direction.Plane.HORIZONTAL) {
            element.face(edge).texture("#side").uvs(0, 16 - topY, 16, 16 - bottomY).cullface(edge).end();
        }
        var upperFace = element.face(Direction.UP).texture("#top").uvs(0, 0, 16, 16);
        if (topY == 16) upperFace.cullface(Direction.UP);
        upperFace.end();
        var lowerFace = element.face(Direction.DOWN).texture("#bottom").uvs(0, 0, 16, 16);
        if (bottomY == 0) lowerFace.cullface(Direction.DOWN);
        lowerFace.end();
        element.end();
        return model;
    }

    private void arrowModels(RegistryObject<RoadArrowBlock> arrow) {
        decalModels(arrow, 20, 36, arrow.get() instanceof ReversibleRoadArrowBlock);
    }

    private void decalModels(RegistryObject<RoadArrowBlock> arrow, float width, float depth, boolean reversible) {
        String name = arrow.getId().getPath();
        ResourceLocation texture = modLoc("block/" + name);
        ModelFile normal = markingPlane(name, texture, 0.05F, false, width, depth);
        ModelFile lowered = markingPlane(name + "_lowered", texture, -7.95F, false, width, depth);
        ModelFile reflected = reversible ? markingPlane(name + "_mirrored", texture, 0.05F, true, width, depth) : normal;
        ModelFile reflectedLowered = reversible ? markingPlane(name + "_lowered_mirrored", texture, -7.95F, true, width, depth) : lowered;
        getVariantBuilder(arrow.get()).forAllStates(state -> {
            boolean low = state.getValue(RoadArrowBlock.LOWERED);
            boolean mirror = reversible && state.getValue(ReversibleRoadArrowBlock.MIRRORED);
            ModelFile model = mirror ? (low ? reflectedLowered : reflected) : (low ? lowered : normal);
            return new ConfiguredModel[]{new ConfiguredModel(model,
                    0, rotation(state.getValue(RoadArrowBlock.FACING)), false)};
        });
        itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", texture);
    }

    private BlockModelBuilder markingPlane(String name, ResourceLocation texture, float height, boolean mirrored,
                                           float width, float depth) {
        BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/block"))
                .texture("marking", texture).texture("particle", texture).renderType("cutout").ao(false);
        float minX = (16 - width) / 2;
        float minZ = (16 - depth) / 2;
        model.element().from(minX, height, minZ).to(minX + width, height, minZ + depth).shade(false)
                .face(Direction.UP).texture("#marking").uvs(mirrored ? 16 : 0, 0, mirrored ? 0 : 16, 16).end()
                .face(Direction.DOWN).texture("#marking").uvs(mirrored ? 16 : 0, 16, mirrored ? 0 : 16, 0).end().end();
        return model;
    }

    private void surfaceFeatureModels(RegistryObject<RoadSurfaceFeatureBlock> feature, String name) {
        ResourceLocation asphalt = blockTexture(ModBlocks.ASPHALT_BLOCK.get());
        ModelFile round = plainRoadShape(name + "_round", asphalt, modLoc("block/" + name + "_round"), 0, 16);
        ModelFile square = plainRoadShape(name + "_square", asphalt, modLoc("block/" + name + "_square"), 0, 16);
        getVariantBuilder(feature.get()).forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(
                state.getValue(RoadSurfaceFeatureBlock.SHAPE) == RoadSurfaceFeatureBlock.FeatureShape.ROUND ? round : square,
                0, rotation(state.getValue(RoadSurfaceFeatureBlock.FACING)), false)});
        simpleBlockItem(feature.get(), round);
    }

    private static int rotation(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }
}
