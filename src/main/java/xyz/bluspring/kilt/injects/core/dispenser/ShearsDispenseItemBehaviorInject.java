package xyz.bluspring.kilt.injects.core.dispenser;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsDispenseItemBehavior.class)
public abstract class ShearsDispenseItemBehaviorInject {
    @Shadow private static boolean tryShearLivingEntity(ServerLevel serverLevel, BlockPos blockPos) {
        throw new IllegalStateException();
    }

    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/ShearsDispenseItemBehavior;tryShearBeehive(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$tryUseNeoDispenseHook(ServerLevel serverLevel, BlockPos blockPos, Operation<Boolean> original, @Local(argsOnly = true) BlockSource source, @Local(argsOnly = true) ItemStack stack) {
        return CommonHooks.tryDispenseShearsHarvestBlock(source, stack, serverLevel, blockPos) || original.call(serverLevel, blockPos);
    }

    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/ShearsDispenseItemBehavior;tryShearLivingEntity(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$tryUseForgeShearable(ServerLevel serverLevel, BlockPos blockPos, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack) {
        kilt$currentStack.set(stack);
        var value = original.call(serverLevel, blockPos);
        kilt$currentStack.remove();

        return value;
    }

    @Unique private static final ThreadLocal<ItemStack> kilt$currentStack = new ThreadLocal<>();

    @Unique
    private static boolean tryShearLivingEntity(ServerLevel level, BlockPos pos, ItemStack stack) {
        kilt$currentStack.set(stack);
        var value = tryShearLivingEntity(level, pos);
        kilt$currentStack.remove();

        return value;
    }

    @Definition(id = "livingEntity", local = @Local(type = LivingEntity.class))
    @Definition(id = "Shearable", type = Shearable.class)
    @Expression("livingEntity instanceof Shearable")
    @Inject(method = "tryShearLivingEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static void kilt$tryUseNeoShearable(ServerLevel level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local LivingEntity entity) {
        if (kilt$currentStack.get() == null)
            return;

        if (entity instanceof IShearable shearable && shearable.isShearable(null, kilt$currentStack.get(), level, pos)) {
            shearable.onSheared(null, kilt$currentStack.get(), level, pos)
                .forEach(drop -> shearable.spawnShearedDrop(level, pos, drop));
        }
    }
}
