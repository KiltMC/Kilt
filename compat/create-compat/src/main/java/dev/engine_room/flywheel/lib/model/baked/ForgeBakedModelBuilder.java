package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.bluspring.kilt.compat.create.flywheel.BakedModelBuffererHelper;

// From Flywheel 1.20.1 Forge, licensed under MIT - https://github.com/Engine-Room/Flywheel/blob/1.20.1/dev/LICENSE.md
// https://github.com/Engine-Room/Flywheel/blob/1.20.1/dev/forge/src/lib/java/dev/engine_room/flywheel/lib/model/baked/ForgeBakedModelBuilder.java
public final class ForgeBakedModelBuilder extends BakedModelBuilder {
    @Nullable
    private ModelData modelData;

    public ForgeBakedModelBuilder(BakedModel bakedModel) {
        super(bakedModel);
    }

    @Override
    public ForgeBakedModelBuilder level(@Nullable BlockAndTintGetter level) {
        super.level(level);
        return this;
    }

    @Override
    public ForgeBakedModelBuilder pos(@Nullable BlockPos pos) {
        super.pos(pos);
        return this;
    }

    @Override
    public ForgeBakedModelBuilder poseStack(@Nullable PoseStack poseStack) {
        super.poseStack(poseStack);
        return this;
    }

    @Override
    public ForgeBakedModelBuilder materialFunc(@Nullable BiFunction<RenderType, Boolean, Material> materialFunc) {
        super.materialFunc(materialFunc);
        return this;
    }

    public ForgeBakedModelBuilder modelData(@Nullable ModelData modelData) {
        this.modelData = modelData;
        return this;
    }

    @Override
    public SimpleModel build() {
        if (level == null) {
            level = EmptyVirtualBlockGetter.FULL_DARK;
        }
        if (pos == null) {
            pos = BlockPos.ZERO;
        }
        if (materialFunc == null) {
            materialFunc = ModelUtil::getMaterial;
        }
        if (modelData == null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            modelData = blockEntity != null ? blockEntity.getModelData() : ModelData.EMPTY;
        }
        BlockState blockState = level.getBlockState(pos);

        var builder = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

        BakedModelBuffererHelper.getModelData().set(modelData);
        // Kilt TODO: fix
//        BakedModelBufferer.bufferModel(bakedModel, pos, level, blockState, poseStack, (renderType, shaded, data) -> {
//            Material material = materialFunc.apply(renderType, shaded);
//            if (material != null) {
//                Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=BakedModelBuilder," + "bakedModel=" + bakedModel + ",renderType=" + renderType + ",shaded=" + shaded);
//                builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
//            }
//        });
        BakedModelBuffererHelper.getModelData().remove();

        return new SimpleModel(builder.build());
    }
}
