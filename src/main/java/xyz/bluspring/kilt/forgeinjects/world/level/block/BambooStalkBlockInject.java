// TRACKED HASH: 03ded715f0ac77df4876c6c27716a2e34c9c0157
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(BambooStalkBlock.class)
public abstract class BambooStalkBlockInject implements IPlantable {
    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int kilt$returnZeroAlways(RandomSource instance, int i, Operation<Integer> original, @Share("originalRandomResult") LocalIntRef originalRandomResult) {
        originalRandomResult.set(original.call(instance, i));
        return 0;
    }

    @WrapOperation(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BambooStalkBlock;growBamboo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;I)V"))
    private void kilt$tryGrowBambooEvent(BambooStalkBlock instance, BlockState state, Level level, BlockPos pos, RandomSource random, int age, Operation<Void> original, @Share("originalRandomResult") LocalIntRef originalRandomResult) {
        if (ForgeHooks.onCropsGrowPre(level, pos, state, originalRandomResult.get() == 0)) {
            original.call(instance, state, level, pos, random, age);
            ForgeHooks.onCropsGrowPost(level, pos, state);
        }
    }

    @WrapOperation(method = "getDestroyProgress", constant = @Constant(classValue = SwordItem.class))
    private boolean kilt$checkCanPerformSwordDig(Object object, Operation<Boolean> original, @Local(argsOnly = true) Player player) {
        return original.call(object) || player.getMainHandItem().canPerformAction(ToolActions.SWORD_DIG);
    }

    // no need to override getPlant here, Porting Lib already does that
}