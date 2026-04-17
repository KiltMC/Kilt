package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(SignRenderer.class)
public abstract class SignRendererInject implements BlockEntityRenderer<SignBlockEntity> {
    @Override
    public AABB getRenderBoundingBox(SignBlockEntity blockEntity) {
        if (blockEntity.getBlockState().getBlock() instanceof StandingSignBlock) {
            BlockPos pos = blockEntity.getBlockPos();
            return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1.125, pos.getZ() + 1);
        }

        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    }
}
