package xyz.bluspring.kilt.forgeinjects.world.inventory;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuInject {
    @Mixin(targets = "net.minecraft.world.inventory.InventoryMenu$1")
    public abstract static class AnonymousEquipmentSlotInject {
        @Shadow @Final EquipmentSlot val$slot;
        @Shadow @Final Player val$owner;

        @ModifyReturnValue(method = "mayPlace", at = @At("RETURN"))
        private boolean kilt$checkCanEquipStack(boolean original, ItemStack stack) {
            return original || stack.canEquip(val$slot, val$owner);
        }
    }
}
