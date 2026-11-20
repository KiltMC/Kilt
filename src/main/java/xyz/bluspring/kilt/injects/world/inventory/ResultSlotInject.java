package xyz.bluspring.kilt.injects.world.inventory;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.Level;

@Mixin(ResultSlot.class)
public abstract class ResultSlotInject {
    @Shadow @Final private Player player;
    @Shadow @Final private CraftingContainer craftSlots;

    @Inject(method = "checkTakeAchievements", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;onCraftedBy(Lnet/minecraft/world/entity/player/Player;I)V", shift = At.Shift.AFTER))
    private void kilt$firePlayerCraftEvent(ItemStack carried, CallbackInfo ci) {
        EventHooks.firePlayerCraftingEvent(this.player, carried, this.craftSlots);
    }

    @WrapOperation(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ResultSlot;getRemainingItems(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Lnet/minecraft/core/NonNullList;"))
    private NonNullList<ItemStack> kilt$setCurrentCraftingPlayer(ResultSlot instance, CraftingInput input, Level level, Operation<NonNullList<ItemStack>> original, @Local(argsOnly = true, name = "player") Player player) {
        CommonHooks.setCraftingPlayer(player);
        var value = original.call(instance, input, level);
        CommonHooks.setCraftingPlayer(null);
        return value;
    }
}
