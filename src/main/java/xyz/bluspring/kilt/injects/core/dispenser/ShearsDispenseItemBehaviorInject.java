package xyz.bluspring.kilt.injects.core.dispenser;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;

@Mixin(ShearsDispenseItemBehavior.class)
public abstract class ShearsDispenseItemBehaviorInject {
    @WrapOperation(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/dispenser/ShearsDispenseItemBehavior;tryShearBeehive(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/BlockPos;)Z", ordinal = 0))
    private boolean kilt$tryShearEvent(ServerLevel level, ItemStack tool, BlockPos pos, Operation<Boolean> original, @Local(argsOnly = true, name = "source") BlockSource source) {
        return CommonHooks.tryDispenseShearsHarvestBlock(source, tool, level, pos) || original.call(level, tool, pos);
    }

    @Definition(id = "livingEntity", local = @Local(type = Entity.class))
    @Definition(id = "Shearable", type = Shearable.class)
    @Expression("livingEntity instanceof Shearable")
    @Inject(method = "tryShearEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static void kilt$tryUseNeoShearable(ServerLevel level, BlockPos pos, ItemStack tool, CallbackInfoReturnable<Boolean> cir, @Local(name = "entity") Entity entity) {
        if (entity instanceof IShearable shearable && shearable.isShearable(null, tool, level, pos)) {
            shearable.onSheared(null, tool, level, pos)
                .forEach(drop -> shearable.spawnShearedDrop(level, pos, drop));
        }
    }
}
