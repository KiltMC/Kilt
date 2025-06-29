package xyz.bluspring.kilt.forgeinjects.world.level.block.grower;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.event.ForgeEventFactory;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMegaTreeGrower.class)
public abstract class AbstractMegaTreeGrowerInject {
    @ModifyExpressionValue(method = "placeMega", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <T> T kilt$callBlockGrowFeature(T original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) RandomSource random, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        var holder = (Holder<ConfiguredFeature<?, ?>>) original;
        var event = ForgeEventFactory.blockGrowFeature(level, random, pos, holder);

        if (event.getResult() == Event.Result.DENY) {
            cir.setReturnValue(false);
            return null;
        }

        return (T) event.getFeature();
    }
}
