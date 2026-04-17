package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(ConduitRenderer.class)
public abstract class ConduitRendererInject implements BlockEntityRenderer<ConduitBlockEntity> {
    @Override
    public AABB getRenderBoundingBox(ConduitBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY() - .25, pos.getZ(), pos.getX() + 1, pos.getY() + 1.25, pos.getZ() + 1);
    }
}
