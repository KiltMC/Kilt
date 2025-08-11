package xyz.bluspring.kilt.injects.world.item.trading;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MerchantOffer.class)
public abstract class MerchantOfferInject {
    @WrapOperation(method = "isRequiredItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canBeDepleted()Z"))
    private boolean kilt$checkIsItemDamageable(Item instance, Operation<Boolean> original, @Local(ordinal = 2) ItemStack stack) {
        return original.call(instance) || instance.isDamageable(stack);
    }
}
