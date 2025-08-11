package xyz.bluspring.kilt.injects.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/world/inventory/ArmorSlot")
public abstract class ArmorSlotInject {
    @Shadow @Final private EquipmentSlot slot;

    @Shadow @Final private LivingEntity owner;

    @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
    private boolean kilt$checkCanEquip(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || stack.canEquip(slot, owner);
    }
}
