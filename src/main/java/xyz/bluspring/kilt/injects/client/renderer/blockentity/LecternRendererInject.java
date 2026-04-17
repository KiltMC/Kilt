package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(LecternRenderer.class)
public abstract class LecternRendererInject implements BlockEntityRenderer<LecternBlockEntity> {
    @Override
    public AABB getRenderBoundingBox(LecternBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1.5, pos.getZ() + 1);
    }
}
