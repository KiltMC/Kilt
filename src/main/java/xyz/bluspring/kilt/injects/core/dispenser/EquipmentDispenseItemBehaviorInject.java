package xyz.bluspring.kilt.injects.core.dispenser;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(EquipmentDispenseItemBehavior.class)
public abstract class EquipmentDispenseItemBehaviorInject {
    @Inject(method = "dispenseEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private static void kilt$respectCanEquip(BlockSource source, ItemStack dispensed, CallbackInfoReturnable<Boolean> cir, @Local(name = "slot") EquipmentSlot slot, @Local(name = "target") LivingEntity target) {
        if (!dispensed.canEquip(slot, target))
            cir.setReturnValue(false);
    }
}
