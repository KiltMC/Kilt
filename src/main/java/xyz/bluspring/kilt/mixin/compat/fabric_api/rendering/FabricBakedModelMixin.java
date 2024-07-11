package xyz.bluspring.kilt.mixin.compat.fabric_api.rendering;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.FRAPIThreadedStorage;

import java.util.function.Supplier;

@Mixin(FabricBakedModel.class)
public interface FabricBakedModelMixin {
    @Inject(method = "emitBlockQuads", at = @At("HEAD"))
    private void kilt$storeVariables(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, CallbackInfo ci) {
        FRAPIThreadedStorage.LEVEL.set(blockView);
        FRAPIThreadedStorage.POS.set(pos);
    }

    @Inject(method = "emitBlockQuads", at = @At("TAIL"))
    private void kilt$clearVariables(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, CallbackInfo ci) {
        FRAPIThreadedStorage.LEVEL.remove();
        FRAPIThreadedStorage.POS.remove();
    }
}
