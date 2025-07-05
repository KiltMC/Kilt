package xyz.bluspring.kilt.compat.sodium.mixin.thirst;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ghen.thirst.content.thirst.PlayerThirst;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Uses an improved version of Thirst's mixin.
// TODO: make PR to Thirst to fix
@Mixin(PotionItem.class)
public class PotionItemMixin {
    @WrapOperation(method = "finishUsingItem",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean finishUsingItem(Inventory instance, ItemStack stack, Operation<Boolean> original){
        if (!original.call(instance, stack)) {
            instance.player.drop(stack, false);
        }
        return true;
    }

    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    public void onFinishUsingItem(ItemStack item, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir)
    {
        if(livingEntity != null && livingEntity instanceof Player player) {
            PlayerThirst.drink(item, player);
        }
    }
}
