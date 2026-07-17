package xyz.bluspring.kilt.injects.client.renderer;

import java.util.Objects;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.ScreenEffectRendererInjection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = ScreenEffectRenderer.class, priority = 1050)
public abstract class ScreenEffectRendererInject implements ScreenEffectRendererInjection {
    @SuppressWarnings("MixinAnnotationTarget") // this exists in Fabric API :D
    @Shadow private static BlockPos pos;

    @Shadow @Final private SpriteGetter sprites;

    @Shadow
    private static @Nullable BlockState getViewBlockingState(Player player) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @WrapWithCondition(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderTex(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private boolean kilt$tryRenderBlockOverlay(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource bufferSource, @Local Player player, @Local BlockState state) {
        return !ClientHooks.renderBlockOverlay(player, poseStack, RenderBlockScreenEffectEvent.OverlayType.BLOCK, state, Objects.requireNonNullElse(pos, BlockPos.ZERO), this.sprites, bufferSource);
    }

    // Kilt: implemented by Fabric API
//    @WrapOperation(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockStateModelSet;getParticleMaterial(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/resources/model/sprite/Material$Baked;"))
//    private Material.Baked kilt$tryUseCustomOverlayTexture(BlockStateModelSet instance, BlockState blockState, Operation<Material.Baked> original) {
//        if (kilt$overlayBlockPos != null && minecraft.level != null) {
//            return instance.getParticleMaterial(blockState, minecraft.level, kilt$overlayBlockPos);
//        }
//
//        return original.call(instance, blockState);
//    }

    @Unique
    private static @Nullable Pair<BlockState, BlockPos> getOverlayBlock(Player player) {
        var state = getViewBlockingState(player);
        return Pair.of(state, pos);
    }

    @ModifyArg(method = "renderWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;blockScreenEffect(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private static Identifier kilt$tryUseCustomTexture(Identifier texture) {
        var customTexture = ScreenEffectRendererInjection.renderFluid$texture.getAndSet(null);
        if (customTexture != null)
            return customTexture;

        return texture;
    }

    @CreateStatic
    private static void renderFluid(Minecraft minecraft, PoseStack poseStack, MultiBufferSource bufferSource, Identifier texture) {
        ScreenEffectRendererInjection.renderFluid(minecraft, poseStack, bufferSource, texture);
    }
}
