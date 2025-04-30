package xyz.bluspring.kilt.forgeinjects.world.inventory;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuInject {
    @Shadow protected abstract SlotAccess createCarriedSlotAccess();

    @WrapWithCondition(method = "synchronizeSlotToRemote", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerSynchronizer;sendSlotChange(Lnet/minecraft/world/inventory/AbstractContainerMenu;ILnet/minecraft/world/item/ItemStack;)V"))
    private boolean kilt$checkStacksShouldSync(ContainerSynchronizer instance, AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack, @Local(argsOnly = true) ItemStack stack, @Local(ordinal = 1) ItemStack stack2) {
        return !stack.equals(stack2, true);
    }

    @WrapOperation(method = "doClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;tryItemClickBehaviourOverride(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean kilt$checkForgeItemStackedEvent(AbstractContainerMenu instance, Player player, ClickAction action, Slot slot, ItemStack clickedItem, ItemStack carriedItem, Operation<Boolean> original) {
        var value = original.call(instance, player, action, slot, clickedItem, carriedItem);

        if (!value) {
            return ForgeHooks.onItemStackedOn(clickedItem, carriedItem, slot, action, player, createCarriedSlotAccess());
        }

        return value;
    }

    @WrapOperation(method = "moveItemStackTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I"))
    private int kilt$checkMaxStackSize(ItemStack instance, Operation<Integer> original, @Local Slot slot) {
        return Math.min(slot.getMaxStackSize(), original.call(instance));
    }
}
