package xyz.bluspring.kilt.injects.client.renderer.blockentity;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.AABB;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererInject implements BlockEntityRenderer<SkullBlockEntity> {
    @Inject(method = "createSkullRenderers", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", shift = At.Shift.BEFORE, remap = false))
    private static void kilt$addForgeSkullRenderers(EntityModelSet entityModelSet, CallbackInfoReturnable<Map<SkullBlock.Type, SkullModelBase>> cir, @Local ImmutableMap.Builder<SkullBlock.Type, SkullModelBase> builder) {
        ModLoader.postEvent(new EntityRenderersEvent.CreateSkullModels(builder, entityModelSet));
    }

    @Override
    public AABB getRenderBoundingBox(SkullBlockEntity blockEntity) {
        SkullBlock.Type type = ((AbstractSkullBlock) blockEntity.getBlockState().getBlock()).getType();
        if (type == SkullBlock.Types.DRAGON) {
            BlockPos pos = blockEntity.getBlockPos();
            return new AABB(pos.getX() - .75, pos.getY() - .35, pos.getZ() - .75, pos.getX() + 1.75, pos.getY() + 1, pos.getZ() + 1.75);
        }

        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
    }
}
