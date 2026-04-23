package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ChorusFlowerBlock.class)
public abstract class ChorusFlowerBlockInject extends Block {
    public ChorusFlowerBlockInject(Properties properties) {
        super(properties);
    }

    @Definition(id = "i", local = @Local(type = int.class))
    @Expression("i < 5")
    @ModifyExpressionValue(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$preGrowEvent(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 1) BlockPos pos, @Local(argsOnly = true) BlockState state, @Share("shouldRunPostEvent") LocalBooleanRef shouldRunPostEvent) {
        shouldRunPostEvent.set(true);
        return original && !CommonHooks.canCropGrow(level, pos, state, true);
    }

    @Definition(id = "blockState", local = @Local(type = BlockState.class, ordinal = 1))
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    @Definition(id = "END_STONE", field = "Lnet/minecraft/world/level/block/Blocks;END_STONE:Lnet/minecraft/world/level/block/Block;")
    @Expression("blockState.is(END_STONE)")
    @ModifyVariable(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"), ordinal = 1)
    private boolean kilt$checkHandleSustainPlant(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 1) BlockState soilState, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState plantState) {
        var soilDecision = soilState.canSustainPlant(level, pos.below(), Direction.UP, plantState);
        if (!soilDecision.isDefault())
            return soilDecision.isTrue();

        return original;
    }

    @Definition(id = "blockState2", local = @Local(type = BlockState.class, ordinal = 2))
    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z")
    @Definition(id = "END_STONE", field = "Lnet/minecraft/world/level/block/Blocks;END_STONE:Lnet/minecraft/world/level/block/Block;")
    @Expression("blockState2.is(END_STONE)")
    @ModifyVariable(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"), ordinal = 1)
    private boolean kilt$checkHandleSustainPlant2(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(ordinal = 2) BlockState soilState, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState plantState, @Local(ordinal = 1) int j) {
        var soilDecision = soilState.canSustainPlant(level, pos.below(j + 1), Direction.UP, plantState);
        if (!soilDecision.isDefault())
            return soilDecision.isTrue();

        return original;
    }

    @Inject(method = "randomTick", at = @At("RETURN"))
    private void kilt$postGrowEvent(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Share("shouldRunPostEvent") LocalBooleanRef shouldRunPostEvent) {
        if (shouldRunPostEvent.get()) {
            CommonHooks.fireCropGrowPost(level, pos, state);
        }
    }

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void kilt$checkCanSustainPlant(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var soilDecision = level.getBlockState(pos.below()).canSustainPlant(level, pos.below(), Direction.UP, state);
        if (!soilDecision.isDefault())
            cir.setReturnValue(soilDecision.isTrue());
    }
}
