package xyz.bluspring.kilt.mixin.compat.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AmbientOcclusionMode;
import net.caffeinemc.mods.sodium.client.services.SodiumModelData;
import net.caffeinemc.mods.sodium.client.util.DirectionUtil;
import net.caffeinemc.mods.sodium.fabric.block.FabricBlockAccess;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@IfModLoaded("sodium")
@Mixin(FabricBlockAccess.class)
public abstract class FabricBlockAccessMixin {
    @Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
    private void kilt$tryUseNeoLightEmission(BlockState state, BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), Block.class, "getLightEmission", BlockState.class, BlockAndTintGetter.class, BlockPos.class)) {
            cir.setReturnValue(state.getLightEmission(level, pos));
        }
    }

    @Inject(method = "shouldSkipRender", at = @At("HEAD"), cancellable = true)
    private void kilt$tryUseNeoSkipRender(BlockGetter level, BlockState selfState, BlockState otherState, BlockPos selfPos, BlockPos otherPos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(otherState.getBlock().getClass(), Block.class, "hidesNeighborFace", BlockState.class, BlockGetter.class, BlockPos.class, BlockState.class, Direction.class) ||
            KiltHelper.INSTANCE.hasMethodOverride(selfState.getBlock().getClass(), Block.class, "supportsExternalFaceHiding", BlockState.class)
        ) {
            cir.setReturnValue(otherState.hidesNeighborFace(level, otherPos, selfState, DirectionUtil.getOpposite(facing)) && selfState.supportsExternalFaceHiding());
        }
    }

    @Inject(method = "shouldShowFluidOverlay", at = @At("HEAD"), cancellable = true)
    private void kilt$tryUseNeoFluidOverlayCheck(BlockState state, BlockAndTintGetter level, BlockPos pos, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), Block.class, "shouldDisplayFluidOverlay", BlockState.class, BlockAndTintGetter.class, BlockPos.class, FluidState.class)) {
            cir.setReturnValue(state.shouldDisplayFluidOverlay(level, pos, fluidState));
        }
    }

    @Redirect(method = "getNormalVectorShade", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/fabric/block/FabricBlockAccess;normalShade(Lnet/minecraft/world/level/BlockAndTintGetter;FFFZ)F"))
    private float kilt$tryUseNeoNormalShade(FabricBlockAccess instance, BlockAndTintGetter blockView, float normalX, float normalY, float normalZ, boolean hasShade) {
        return blockView.getShade(normalX, normalY, normalZ, hasShade);
    }

    @Inject(method = "usesAmbientOcclusion", at = @At("HEAD"), cancellable = true)
    private void kilt$tryUseNeoAOCheck(BakedModel model, BlockState state, SodiumModelData data, RenderType renderType, BlockAndTintGetter level, BlockPos pos, CallbackInfoReturnable<AmbientOcclusionMode> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(model.getClass(), BakedModel.class, "useAmbientOcclusion", BlockState.class, ModelData.class, RenderType.class)) {
            cir.setReturnValue(switch (model.useAmbientOcclusion(state, (ModelData) (Object) data, renderType)) {
                case TRUE -> AmbientOcclusionMode.ENABLED;
                case DEFAULT -> AmbientOcclusionMode.DEFAULT;
                case FALSE -> AmbientOcclusionMode.DISABLED;
            });
        }
    }

    @Inject(method = "shouldBlockEntityGlow", at = @At("HEAD"), cancellable = true)
    private void kilt$checkBlockEntityGlows(BlockEntity blockEntity, LocalPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(blockEntity.getClass(), BlockEntity.class, "hasCustomOutlineRendering", Player.class)) {
            cir.setReturnValue(blockEntity.hasCustomOutlineRendering(player));
        }
    }

    @Inject(method = "shouldOccludeFluid", at = @At("HEAD"), cancellable = true)
    private void kilt$tryCheckNeoAdjacentFluid(Direction adjDirection, BlockState adjBlockState, FluidState fluid, CallbackInfoReturnable<Boolean> cir) {
        if (KiltHelper.INSTANCE.hasMethodOverride(adjBlockState.getBlock().getClass(), Block.class, "shouldHideAdjacentFluidFace", BlockState.class, Direction.class, FluidState.class)) {
            cir.setReturnValue(adjBlockState.shouldHideAdjacentFluidFace(adjDirection, fluid));
        }
    }
}
