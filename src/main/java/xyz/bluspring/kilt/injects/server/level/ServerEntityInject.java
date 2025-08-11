package xyz.bluspring.kilt.injects.server.level;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public abstract class ServerEntityInject {
    @Shadow @Final private Entity entity;

    // I don't actually know how to handle instanceof properly
    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "getItem", method = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;")
    @Definition(id = "MapItem", type = MapItem.class)
    @Expression("itemStack.getItem() instanceof MapItem")
    @Redirect(method = "sendChanges", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$forceHandleAllItems(Object instance, Class<?> type) {
        return true;
    }

    @Inject(method = "removePairing", at = @At("TAIL"))
    private void kilt$callStopEntityTracking(ServerPlayer player, CallbackInfo ci) {
        EventHooks.onStopEntityTracking(this.entity, player);
    }

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void kilt$callStartEntityTracking(ServerPlayer player, CallbackInfo ci) {
        EventHooks.onStartEntityTracking(this.entity, player);
    }
}
