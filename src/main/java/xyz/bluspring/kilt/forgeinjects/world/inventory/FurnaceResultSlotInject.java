package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceResultSlot.class)
public abstract class FurnaceResultSlotInject {
    @Shadow @Final private Player player;

    @Inject(method = "checkTakeAchievements", at = @At("TAIL"))
    private void kilt$callForgePlayerSmeltedEvent(ItemStack stack, CallbackInfo ci) {
        ForgeEventFactory.firePlayerSmeltedEvent(this.player, stack);
    }
}
