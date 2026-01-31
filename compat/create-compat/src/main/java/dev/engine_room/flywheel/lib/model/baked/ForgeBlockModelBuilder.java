package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import xyz.bluspring.kilt.compat.create.flywheel.BakedModelBuffererHelper;

// From Flywheel 1.20.1 Forge, licensed under MIT - https://github.com/Engine-Room/Flywheel/blob/1.20.1/dev/LICENSE.md
// https://github.com/Engine-Room/Flywheel/blob/1.20.1/dev/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/ForgeBlockModelBuilder.java
public final class ForgeBlockModelBuilder extends BlockModelBuilder {
    @Nullable
    private Function<BlockPos, ModelData> modelDataLookup;

    public ForgeBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions) {
        super(level, positions);
    }

    @Override
    public ForgeBlockModelBuilder poseStack(@Nullable PoseStack poseStack) {
        super.poseStack(poseStack);
        return this;
    }

    @Override
    public ForgeBlockModelBuilder renderFluids(boolean renderFluids) {
        super.renderFluids(renderFluids);
        return this;
    }

    @Override
    public ForgeBlockModelBuilder materialFunc(@Nullable BiFunction<RenderType, Boolean, Material> materialFunc) {
        super.materialFunc(materialFunc);
        return this;
    }

    public ForgeBlockModelBuilder modelDataLookup(@Nullable Function<BlockPos, ModelData> modelDataLookup) {
        this.modelDataLookup = modelDataLookup;
        return this;
    }

    @Override
    public SimpleModel build() {
        if (materialFunc == null) {
            materialFunc = ModelUtil::getMaterial;
        }
        if (modelDataLookup == null) {
            modelDataLookup = pos -> {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                return blockEntity != null ? blockEntity.getModelData() : ModelData.EMPTY;
            };
        }

        var builder = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

        BakedModelBuffererHelper.getModelDataLookup().set(modelDataLookup);
        // Kilt TODO: fix
//        BakedModelBufferer.bufferBlocks(positions.iterator(), level, poseStack, renderFluids, (renderType, shaded, data) -> {
//            Material material = materialFunc.apply(renderType, shaded);
//            if (material != null) {
//                Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=BlockModelBuilder," + "renderType=" + renderType + ",shaded=" + shaded);
//                builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
//            }
//        });
        BakedModelBuffererHelper.getModelDataLookup().remove();

        return new SimpleModel(builder.build());
    }
}