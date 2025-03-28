package xyz.bluspring.kilt.forgeinjects.world.level.storage.loot.functions;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmeltItemFunction.class)
public abstract class SmeltItemFunctionInject {
    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"))
    private int kilt$multiplySmelting(ItemStack instance, Operation<Integer> original, @Local(ordinal = 1) ItemStack stack) {
        return original.call(instance) * stack.getCount();
    }
}
