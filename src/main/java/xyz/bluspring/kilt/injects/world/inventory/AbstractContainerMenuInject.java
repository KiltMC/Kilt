package xyz.bluspring.kilt.injects.world.inventory;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuInject {
    @Shadow protected abstract SlotAccess createCarriedSlotAccess();

    @Inject(method = "tryItemClickBehaviourOverride", at = @At("HEAD"), cancellable = true)
    private void kilt$checkNeoItemStackedOn(Player player, ClickAction action, Slot slot, ItemStack clickedItem, ItemStack carriedItem, CallbackInfoReturnable<Boolean> cir) {
        if (CommonHooks.onItemStackedOn(clickedItem, carriedItem, slot, action, player, createCarriedSlotAccess())) {
            cir.setReturnValue(true);
        }
    }
}
