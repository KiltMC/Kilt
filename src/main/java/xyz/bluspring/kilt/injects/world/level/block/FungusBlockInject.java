package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FungusBlock.class)
public abstract class FungusBlockInject {
    @ModifyReceiver(method = "method_46682", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;value()Ljava/lang/Object;"))
    private static Holder<ConfiguredFeature<?, ?>> kilt$useForgeEventFeature(Holder<ConfiguredFeature<?, ?>> instance, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) RandomSource randomSource, @Local(argsOnly = true) BlockPos pos, @Cancellable CallbackInfo ci) {
        var event = EventHooks.fireBlockGrowFeature(level, randomSource, pos, instance);

        if (event.isCanceled()) {
            ci.cancel();
            return instance;
        }

        return event.getFeature();
    }
}
