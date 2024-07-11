package xyz.bluspring.kilt.mixin.compat.indium;

import com.bawnorton.mixinsquared.TargetHandler;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.FRAPIThreadedStorage;
import xyz.bluspring.kilt.mixin.WorldSliceAccessor;

import java.util.function.Supplier;

@IfModLoaded("indium")
@IfModAbsent(value = "sodium", minVersion = "0.6.0") // Sodium version that supports FRAPI
@Mixin(value = BakedModel.class, priority = 1500)
public interface BakedModelMixin {
    @TargetHandler(
        mixin = "link.infra.indium.mixin.renderer.MixinBakedModel",
        name = "emitBlockQuads"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"))
    private void kilt$storeVariables(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, CallbackInfo ci) {
        // Indium uses Sodium's WorldSlice instead, get the original ClientLevel instead
        FRAPIThreadedStorage.LEVEL.set(((WorldSliceAccessor) blockView).getWorld());
        FRAPIThreadedStorage.POS.set(pos);
    }

    @TargetHandler(
        mixin = "link.infra.indium.mixin.renderer.MixinBakedModel",
        name = "emitBlockQuads"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("TAIL"))
    private void kilt$clearVariables(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context, CallbackInfo ci) {
        FRAPIThreadedStorage.LEVEL.remove();
        FRAPIThreadedStorage.POS.remove();
    }
}
