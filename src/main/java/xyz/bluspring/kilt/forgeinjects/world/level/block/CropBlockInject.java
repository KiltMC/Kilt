package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CropBlock.class)
public abstract class CropBlockInject {
    @Definition(id = "random", local = @Local(type = RandomSource.class))
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Definition(id = "f", local = @Local(type = float.class))
    @Expression("random.nextInt((int)(25.0 / f) + 1) == 0")
    @ModifyExpressionValue(method = "randomTick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$callForgePreGrow(boolean original, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) BlockPos pos, @Local(argsOnly = true) BlockState state) {
        return ForgeHooks.onCropsGrowPre(level, pos, state, original);
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER))
    private void kilt$callForgePostGrow(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        ForgeHooks.onCropsGrowPost(level, pos, state);
    }

    @WrapOperation(method = "getGrowthSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    private static boolean kilt$checkCanSustainPlant(BlockState instance, Block block, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter level, @Local(ordinal = 1) BlockPos pos, @Local(ordinal = 0) int i, @Local(ordinal = 1) int j, @Local(argsOnly = true) Block block2) {
        return original.call(instance, block) || instance.canSustainPlant(level, pos.offset(i, 0, j), Direction.UP, (IPlantable) block2);
    }

    // i don't care that this is a wildcard, it works.
    @Expression("? > 0")
    @ModifyExpressionValue(method = "getGrowthSpeed", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$callForgePreGrow(boolean original, @Local BlockState state, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos, @Local(ordinal = 0) int i, @Local(ordinal = 1) int j) {
        return original || state.isFertile(level, pos.offset(i, 0, j))
    }

    @WrapOperation(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkMobGriefing(GameRules instance, GameRules.Key<GameRules.BooleanValue> key, Operation<Boolean> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) Entity entity) {
        // the stuff I do for mod compat...
        var result = original.call(instance, key);
        var event = ForgeEventFactory.getMobGriefingEvent(level, entity);

        if (result && event)
            return true;
        else if (result && !event)
            return false;
        else if (!result && event)
            return true;
        else
            return false;
    }
}
