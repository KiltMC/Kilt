package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(BannerRenderer.class)
public abstract class BannerRendererInject implements BlockEntityRenderer<BannerBlockEntity> {
    @Override
    public AABB getRenderBoundingBox(BannerBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        boolean standing = blockEntity.getBlockState().getBlock() instanceof BannerBlock;
        return AABB.encapsulatingFullBlocks(pos, standing ? pos.above() : pos.below());
    }
}
