package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(ShulkerBoxRenderer.class)
public abstract class ShulkerBoxRendererInject implements BlockEntityRenderer<ShulkerBoxBlockEntity> {
    @Override
    public AABB getRenderBoundingBox(ShulkerBoxBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 0.5, pos.getY() - 0.5, pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 1.5, pos.getZ() + 1.5);
    }
}
