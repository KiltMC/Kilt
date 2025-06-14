package xyz.bluspring.kilt.forgeinjects.world.entity.monster.piglin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class PiglinAiInject {
    @ModifyExpressionValue(method = {"stopHoldingOffHandItem", "wantsToPickup", "canAdmire"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;isBarterCurrency(Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean kilt$checkIsPiglinCurrency(boolean original, @Local ItemStack stack) {
        return original || stack.isPiglinCurrency();
    }

    @Definition(id = "item", local = @Local(type = Item.class))
    @Definition(id = "ArmorItem", type = ArmorItem.class)
    @Expression("item instanceof ArmorItem")
    @Inject(method = "isWearingGold", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private static void kilt$checkMakesPiglinsNeutral(LivingEntity livingEntity, CallbackInfoReturnable<Boolean> cir, @Local ItemStack stack) {
        if (stack.makesPiglinsNeutral(livingEntity)) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(method = "hasCrossbow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isHolding(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean kilt$checkIsHoldingCrossbow(LivingEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.isHolding(is -> is.getItem() instanceof CrossbowItem);
    }
}
