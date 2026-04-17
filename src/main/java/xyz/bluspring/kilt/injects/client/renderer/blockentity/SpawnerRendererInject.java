package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(SpawnerRenderer.class)
public abstract class SpawnerRendererInject implements BlockEntityRenderer<SpawnerBlockEntity> {
    @Override
    public AABB getRenderBoundingBox(SpawnerBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
    }
}
