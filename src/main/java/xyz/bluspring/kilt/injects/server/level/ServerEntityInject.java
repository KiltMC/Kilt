package xyz.bluspring.kilt.injects.server.level;

import java.util.function.Consumer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.level.ServerEntityInjection;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;

@Mixin(ServerEntity.class)
public abstract class ServerEntityInject implements ServerEntityInjection {
    @Shadow @Final private Entity entity;

    @Shadow
    public abstract void sendPairingData(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> consumer);

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

    @Unique private PacketAndPayloadAcceptor<?> kilt$payloadAcceptor;

    @WrapOperation(method = "addPairing", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity;sendPairingData(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"))
    private void kilt$wrapWithPacketAndPayloadAcceptor(ServerEntity instance, ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> consumer, Operation<Void> original) {
        this.kilt$payloadAcceptor = new PacketAndPayloadAcceptor<>((Consumer<Packet<? super ClientCommonPacketListener>>) (Object) consumer); // why?
        original.call(instance, player, consumer);
        this.kilt$payloadAcceptor = null;
    }

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void kilt$callStartEntityTracking(ServerPlayer player, CallbackInfo ci) {
        EventHooks.onStartEntityTracking(this.entity, player);
    }

    @Override
    public void sendPairingData(ServerPlayer player, PacketAndPayloadAcceptor<ClientGamePacketListener> acceptor) {
        this.kilt$payloadAcceptor = acceptor;
        this.sendPairingData(player, acceptor::accept);
        this.kilt$payloadAcceptor = null;
    }

    @Definition(id = "trackedDataValues", field = "Lnet/minecraft/server/level/ServerEntity;trackedDataValues:Ljava/util/List;")
    @Expression("this.trackedDataValues != null")
    @Inject(method = "sendPairingData", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$sendPairingDataWithAcceptor(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> consumer, CallbackInfo ci) {
        if (this.kilt$payloadAcceptor != null) {
            this.entity.sendPairingData(player, this.kilt$payloadAcceptor::accept);
        } else {
            this.entity.sendPairingData(player, payload -> ((Consumer) consumer).accept(new ClientboundCustomPayloadPacket(payload)));
        }
    }

    @Inject(method = "sendPairingData", at = @At("TAIL"))
    private void kilt$handleSyncInitialAttachments(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> consumer, CallbackInfo ci) {
        if (this.kilt$payloadAcceptor != null) {
            AttachmentSync.syncInitialEntityAttachments(this.entity, player, packet -> ((Consumer) consumer).accept(packet));
        }
    }
}
