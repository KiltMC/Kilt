package xyz.bluspring.kilt.injects.world.level.block.grower;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

@Mixin(TreeGrower.class)
public abstract class TreeGrowerInject {
    @Definition(id = "holder", local = @Local(type = Holder.class))
    @Expression("holder != null")
    @ModifyVariable(method = "growTree", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Holder<ConfiguredFeature<?, ?>> kilt$handleGrowFeatureEvent(Holder<ConfiguredFeature<?, ?>> holder, @Cancellable CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) RandomSource random, @Local(argsOnly = true) BlockPos pos) {
        var event = EventHooks.fireBlockGrowFeature(level, random, pos, holder);

        if (event.isCanceled()) {
            cir.setReturnValue(false);
        }

        return event.getFeature();
    }

    @Definition(id = "holder2", local = @Local(type = Holder.class))
    @Expression("holder2 == null")
    @ModifyVariable(method = "growTree", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Holder<ConfiguredFeature<?, ?>> kilt$handleGrowFeatureEvent2(Holder<ConfiguredFeature<?, ?>> holder, @Cancellable CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) RandomSource random, @Local(argsOnly = true) BlockPos pos) {
        var event = EventHooks.fireBlockGrowFeature(level, random, pos, holder);

        if (event.isCanceled()) {
            cir.setReturnValue(false);
        }

        return event.getFeature();
    }
}
