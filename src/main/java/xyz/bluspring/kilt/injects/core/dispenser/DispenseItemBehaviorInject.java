package xyz.bluspring.kilt.injects.core.dispenser;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.extensions.IDispensibleContainerItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(DispenseItemBehavior.class)
public interface DispenseItemBehaviorInject {
    @Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$3")
    abstract class BucketDispenseItemBehaviorInject {
        @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DispensibleContainerItem;emptyContents(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"))
        private boolean kilt$tryForgeEmptyContents(DispensibleContainerItem instance, LivingEntity entity, Level level, BlockPos blockPos, BlockHitResult blockHitResult, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack) {
            if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IDispensibleContainerItemExtension.class, "emptyContents", LivingEntity.class, Level.class, BlockPos.class, BlockHitResult.class, ItemStack.class)) {
                return instance.emptyContents(entity, level, blockPos, blockHitResult, stack);
            }

            return original.call(instance, entity, level, blockPos, blockHitResult);
        }
    }

    @Mixin(targets = "net/minecraft/core/dispenser/DispenseItemBehavior$5")
    abstract class FlintAndSteelDispenseItemBehaviorInject {
        @Definition(id = "blockState", local = @Local(type = BlockState.class))
        @Definition(id = "getBlock", method = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;")
        @Definition(id = "TntBlock", type = TntBlock.class)
        @Expression("blockState.getBlock() instanceof TntBlock")
        @ModifyExpressionValue(method = "execute", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$checkIsFlammable(boolean original, @Local BlockState state, @Local ServerLevel level, @Local BlockPos pos, @Local(argsOnly = true) BlockSource source) {
            if (!original) {
                if (state.isFlammable(level, pos, source.state().getValue(DispenserBlock.FACING).getOpposite())) {
                    return true;
                }
            }

            return original;
        }

        @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/TntBlock;prime(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
        private boolean kilt$checkTNTBlockState(Level level, BlockPos pos, Operation<Boolean> original, @Local BlockState state, @Local(argsOnly = true) BlockSource source) {
            if (state.getBlock() instanceof TntBlock)
                return original.call(level, pos);
            else
                return state.onCaughtFire(level, pos, source.state().getValue(DispenserBlock.FACING).getOpposite(), null);
        }

        @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
        private boolean kilt$checkIsTNTBlock(ServerLevel instance, BlockPos pos, boolean isMoving, Operation<Boolean> original, @Local BlockState state) {
            if (state.getBlock() instanceof TntBlock) {
                return original.call(instance, pos, isMoving);
            } else {
                return false;
            }
        }
    }
}
