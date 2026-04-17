package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(BeaconRenderer.class)
public abstract class BeaconRendererInject implements BlockEntityRenderer<BeaconBlockEntity> {
    @Shadow
    @Final
    public static int MAX_RENDER_Y;

    @Override
    public AABB getRenderBoundingBox(BeaconBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, MAX_RENDER_Y, pos.getZ() + 1.0);
    }
}
