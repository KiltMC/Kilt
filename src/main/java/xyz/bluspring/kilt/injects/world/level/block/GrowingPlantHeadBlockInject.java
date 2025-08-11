package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrowingPlantHeadBlock.class)
public abstract class GrowingPlantHeadBlockInject {
    @Definition(id = "random", local = @Local(type = RandomSource.class, argsOnly = true))
    @Definition(id = "nextDouble", method = "Lnet/minecraft/util/RandomSource;nextDouble()D")
    @Definition(id = "growPerTickProbability", field = "Lnet/minecraft/world/level/block/GrowingPlantHeadBlock;growPerTickProbability:D")
    @Expression("random.nextDouble() < this.growPerTickProbability")
    @ModifyExpressionValue(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$callForgeCropsGrowPreEvent(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        return CommonHooks.onCropsGrowPre(level, pos, state, original);
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", shift = At.Shift.AFTER))
    private void kilt$callForgeCropsGrowPostEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        CommonHooks.onCropsGrowPost(level, pos, state);
    }
}
