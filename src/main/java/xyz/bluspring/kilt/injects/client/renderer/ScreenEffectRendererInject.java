// TRACKED HASH: 0352f8e699c8b1e9b6ac77b9927e3852ffaf311c
package xyz.bluspring.kilt.injects.client.renderer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.ScreenEffectRendererInjection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererInject implements ScreenEffectRendererInjection {
    @Shadow @Final private static ResourceLocation UNDERWATER_LOCATION;

    @Shadow
    @Nullable
    private static BlockState getViewBlockingState(Player player) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Unique private static final AtomicBoolean kilt$isNullResetHandled = new AtomicBoolean(false);
    @Unique private static final AtomicBoolean kilt$hasViewBlockingStateHandled = new AtomicBoolean(false);
    @Unique private static @Nullable BlockState kilt$overlayBlockState = null;
    @Unique private static @Nullable BlockPos kilt$overlayBlockPos = null;

    @WrapWithCondition(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderTex(Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private static boolean kilt$tryRenderBlockOverlay(TextureAtlasSprite texture, PoseStack poseStack, @Local Player player, @Local BlockState state) {
        return !ClientHooks.renderBlockOverlay(player, poseStack, RenderBlockScreenEffectEvent.OverlayType.BLOCK, state, Objects.requireNonNullElse(kilt$overlayBlockPos, BlockPos.ZERO));
    }

    @WrapOperation(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/BlockModelShaper;getParticleIcon(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"))
    private static TextureAtlasSprite kilt$tryUseCustomOverlayTexture(BlockModelShaper instance, BlockState state, Operation<TextureAtlasSprite> original, @Local(argsOnly = true) Minecraft minecraft) {
        if (kilt$overlayBlockPos != null && minecraft.level != null) {
            var data = minecraft.level.getModelDataManager().getAt(kilt$overlayBlockPos);
            var model = instance.getBlockModel(state);

            if (model.getModelData(minecraft.level, kilt$overlayBlockPos, state, data == null ? ModelData.EMPTY : data) != ModelData.EMPTY) {
                return instance.getTexture(state, minecraft.level, kilt$overlayBlockPos);
            }
        }

        return original.call(instance, state);
    }

    @WrapWithCondition(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderWater(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private static boolean kilt$checkRenderWaterOverlay(Minecraft minecraft, PoseStack poseStack, @Local Player player) {
        return !ClientHooks.renderWaterOverlay(player, poseStack);
    }

    @Definition(id = "minecraft", local = @Local(type = Minecraft.class, argsOnly = true))
    @Definition(id = "player", field = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;")
    @Definition(id = "isEyeInFluid", method = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    @Definition(id = "WATER", field = "Lnet/minecraft/tags/FluidTags;WATER:Lnet/minecraft/tags/TagKey;")
    @Expression("minecraft.player.isEyeInFluid(WATER)")
    @ModifyExpressionValue(method = "renderScreenEffect", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$tryRenderCustomFluidOverlay(boolean original, @Local Player player, @Local(argsOnly = true) Minecraft mc, @Local(argsOnly = true) PoseStack poseStack) {
        if (!original) {
            if (!player.getEyeInFluidType().isAir()) {
                IClientFluidTypeExtensions.of(player.getEyeInFluidType())
                    .renderOverlay(mc, poseStack);
            }

            return false;
        }

        return true;
    }

    @WrapWithCondition(method = "renderScreenEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderWater(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private static boolean kilt$checkRenderFireOverlay(Minecraft minecraft, PoseStack poseStack, @Local Player player) {
        return !ClientHooks.renderFireOverlay(player, poseStack);
    }

    @Inject(method = "getViewBlockingState", at = @At(value = "RETURN", ordinal = 0))
    private static void kilt$trySetCurrentBlockPos(Player player, CallbackInfoReturnable<BlockState> cir, @Local BlockPos.MutableBlockPos pos) {
        kilt$overlayBlockPos = pos.immutable();
    }

    @WrapMethod(method = "getViewBlockingState")
    private static BlockState kilt$funnyOverlayBlockWorkaround(Player player, Operation<BlockState> original) {
        kilt$overlayBlockPos = null;
        var state = original.call(player);
        kilt$hasViewBlockingStateHandled.set(true);
        kilt$overlayBlockState = state;

        var actualPair = getOverlayBlock(player);
        try {
            if (state == null && actualPair != null) {
                return actualPair.getLeft();
            } else if (state != null && actualPair == null) {
                return null;
            } else if (actualPair != null && state != actualPair.getLeft()) {
                return actualPair.getLeft();
            }

            return state;
        } finally {
            if (!kilt$isNullResetHandled.getAndSet(false)) {
                kilt$overlayBlockState = null;
            }

            kilt$hasViewBlockingStateHandled.set(false);
        }
    }

    @Nullable @Unique
    private static Pair<BlockState, BlockPos> getOverlayBlock(Player player) {
        // Kilt: We're doing a lot of funny workarounds here just to make @WrapMethod mixins to here work for us.
        if (!kilt$hasViewBlockingStateHandled.getAndSet(false)) {
            kilt$isNullResetHandled.set(true);
            var state = getViewBlockingState(player);
            try {
                if (state == null)
                    return null;

                return Pair.of(state, kilt$overlayBlockPos);
            } finally {
                kilt$overlayBlockState = null;
                kilt$overlayBlockPos = null;
            }
        }

        return Pair.of(kilt$overlayBlockState, kilt$overlayBlockPos);
    }

    @WrapOperation(method = "renderWater", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V"))
    private static void kilt$useForgeWaterRender(int i, ResourceLocation resourceLocation, Operation<Void> original) {
        original.call(i, ScreenEffectRendererInjection.currentTexture.get());
    }

    @CreateStatic
    private static void renderFluid(Minecraft mc, PoseStack poseStack, ResourceLocation texture) {
        ScreenEffectRendererInjection.renderFluid(mc, poseStack, texture);
    }

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void kilt$setUnderwaterTexture(CallbackInfo ci) {
        ScreenEffectRendererInjection.currentTexture.set(UNDERWATER_LOCATION);
    }
}
