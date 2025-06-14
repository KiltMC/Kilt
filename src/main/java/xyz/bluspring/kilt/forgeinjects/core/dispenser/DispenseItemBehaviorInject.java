package xyz.bluspring.kilt.forgeinjects.core.dispenser;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(DispenseItemBehavior.class)
public interface DispenseItemBehaviorInject {
    @Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$16")
    public abstract static class DispenseItemBehavior16Inject {
        @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DispensibleContainerItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
        private boolean kilt$tryForgeEmptyContents(DispensibleContainerItem instance, Player player, Level level, BlockPos blockPos, BlockHitResult hitResult, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack) {
            if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "emptyContents", Player.class, Level.class, BlockPos.class, BlockHitResult.class, ItemStack.class)) {
                return instance.emptyContents(player, level, blockPos, hitResult, stack);
            }

            return original.call(instance, player, level, blockPos, hitResult);
        }
    }

    @Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$18")
    public abstract static class DispenseItemBehavior18Inject {
        @Definition(id = "blockState", local = @Local(type = BlockState.class))
        @Definition(id = "getBlock", method = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")
        @Definition(id = "TntBlock", type = TntBlock.class)
        @Expression("blockState.getBlock() instanceof TntBlock")
        @ModifyExpressionValue(method = "execute", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$checkIsFlammable(boolean original, @Local BlockState state, @Local Level level, @Local BlockPos pos, @Local(argsOnly = true) BlockSource source) {
            if (!original) {
                if (state.isFlammable(level, pos, source.getBlockState().getValue(DispenserBlock.FACING).getOpposite())) {
                    return true;
                }
            }

            return original;
        }

        @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
        private void kilt$checkTNTBlockState(Level level, BlockPos pos, Operation<Void> original, @Local BlockState state, @Local(argsOnly = true) BlockSource source) {
            if (state.getBlock() instanceof TntBlock)
                original.call(level, pos);
            else
                state.onCaughtFire(level, pos, source.getBlockState().getValue(DispenserBlock.FACING).getOpposite(), null);
        }

        @WrapWithCondition(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
        private boolean kilt$checkIsTNTBlock(Level instance, BlockPos pos, boolean isMoving, @Local BlockState state) {
            return state.getBlock() instanceof TntBlock;
        }
    }
}
