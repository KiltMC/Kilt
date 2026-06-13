// TRACKED HASH: 76d201a1a6836dbb6ce5a521f6f40e36bff0dc05
package xyz.bluspring.kilt.injects.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Mixin(LiquidBlockRenderer.class)
public abstract class LiquidBlockRendererInject {
    @Shadow
    private static boolean isFaceOccludedBySelf(BlockGetter level, BlockPos pos, BlockState state, Direction face) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "setupSprites", at = @At("TAIL"))
    private void kilt$reloadFluidSpriteCache(CallbackInfo ci) {
        FluidSpriteCache.reload();
    }

    @Unique
    private static boolean isNeighborStateHidingOverlay(FluidState selfState, BlockState otherState, Direction neighborFace) {
        return otherState.shouldHideAdjacentFluidFace(neighborFace, selfState);
    }

    @CreateStatic
    private static boolean shouldRenderFace(BlockAndTintGetter level, BlockPos pos, FluidState fluidState, BlockState selfState, Direction direction, BlockState otherState) {
        return !isFaceOccludedBySelf(level, pos, selfState, direction) && !isNeighborStateHidingOverlay(fluidState, otherState, direction.getOpposite());
    }

    // Kilt: forge added method
    private void vertex(VertexConsumer pConsumer, float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float alpha, float pU, float pV, int pPackedLight) {
        pConsumer.addVertex(pX, pY, pZ).setColor(pRed, pGreen, pBlue, alpha).setUv(pU, pV).setLight(pPackedLight).setNormal(0.0F, 1.0F, 0.0F);
    }
}
