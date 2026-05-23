// TRACKED HASH: 188e8b49f131b25e05beacfc753e1c8aa232a842
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChorusFlowerBlock.class)
public abstract class ChorusFlowerBlockInject {
    @Definition(id = "i", local = @Local(type = int.class))
    @Expression("i < 5")
    @ModifyExpressionValue(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$preGrowEvent(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 1) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        return original && ForgeHooks.onCropsGrowPre(level, pos, state, true);
    }

    @Inject(
            method = "randomTick",
            at = @At(
                    value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )
    )
    private void kilt$onEnterIfStatement(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("shouldRunPostEvent") LocalBooleanRef shouldRunPostEvent) {
        shouldRunPostEvent.set(true);
    }

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void kilt$postGrowEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("shouldRunPostEvent") LocalBooleanRef shouldRunPostEvent) {
        if (shouldRunPostEvent.get()) {
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    @Definition(id = "END_STONE", field = "Lnet/minecraft/world/level/block/Blocks;END_STONE:Lnet/minecraft/world/level/block/Block;")
    @Expression("?.is(END_STONE)")
    @WrapOperation(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkChorusAdditionallyGrows(BlockState instance, Block block, Operation<Boolean> original) {
        return original.call(instance, block) || instance.is(Tags.Blocks.CHORUS_ADDITIONALLY_GROWS_ON);
    }

    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    @Definition(id = "END_STONE", field = "Lnet/minecraft/world/level/block/Blocks;END_STONE:Lnet/minecraft/world/level/block/Block;")
    @Expression("?.is(END_STONE)")
    @WrapOperation(method = "canSurvive", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkChorusDoesntAdditionallyGrow(BlockState instance, Block block, Operation<Boolean> original) {
        return original.call(instance,block) || instance.is(Tags.Blocks.CHORUS_ADDITIONALLY_GROWS_ON);
    }
}