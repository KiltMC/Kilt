package xyz.bluspring.kilt.injects.world.inventory;

import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;

@Mixin(FurnaceResultSlot.class)
public abstract class FurnaceResultSlotInject {
    @Shadow @Final private Player player;
    @Shadow private int removeCount;

    @Inject(method = "checkTakeAchievements", at = @At("TAIL"))
    private void kilt$callForgePlayerSmeltedEvent(ItemStack carried, CallbackInfo ci) {
        EventHooks.firePlayerSmeltedEvent(this.player, carried, this.removeCount);
    }
}
