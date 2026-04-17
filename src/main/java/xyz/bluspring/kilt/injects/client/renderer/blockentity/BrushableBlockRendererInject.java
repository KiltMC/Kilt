package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BrushableBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(BrushableBlockRenderer.class)
public abstract class BrushableBlockRendererInject implements BlockEntityRenderer<BrushableBlockEntity> {
    @Override
    public AABB getRenderBoundingBox(BrushableBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 0.25, pos.getY() - 0.25, pos.getZ() - 0.25, pos.getX() + 1.25, pos.getY() + 1.25, pos.getZ() + 1.25);
    }
}
