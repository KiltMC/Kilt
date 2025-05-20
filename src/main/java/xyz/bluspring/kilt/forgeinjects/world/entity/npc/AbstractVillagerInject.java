package xyz.bluspring.kilt.forgeinjects.world.entity.npc;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerInject {
    @Shadow public abstract @Nullable Player getTradingPlayer();

    @Inject(method = "notifyTrade", at = @At("TAIL"))
    private void kilt$callVillagerTradeEvent(MerchantOffer offer, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TradeWithVillagerEvent(this.getTradingPlayer(), offer, (AbstractVillager) (Object) this));
    }

    // TODO: implement teleporter
}
