// TRACKED HASH: 1886f30859644767992d36dcae1264c9b9614cd4
package xyz.bluspring.kilt.forgeinjects.server.network;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingSwapItemsEvent;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplInject {
    @Shadow @Final public Connection connection;

    @Shadow
    public ServerPlayer player;

    @Inject(
        method = "handlePlayerAction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V",
            ordinal = 0
        ),
        cancellable = true
    )
    private void kilt$handleSendPlayerSwapItemsEvent(
        ServerboundPlayerActionPacket packet, CallbackInfo ci, @Share("event") LocalRef<LivingSwapItemsEvent.Hands> event
    ) {
        event.set(ForgeHooks.onLivingSwapHandItems(player));
        if (event.get().isCanceled()) {
            ci.cancel();
        }
    }

    @WrapOperation(
        method = "handlePlayerAction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private void kilt$updateSwappedItemsFromEvent(
        ServerPlayer instance, InteractionHand hand, ItemStack itemStack, Operation<Void> original,
        @Share("event") LocalRef<LivingSwapItemsEvent.Hands> eventRef
    ) {
        var event = eventRef.get();

        if (hand == InteractionHand.OFF_HAND && !event.getItemSwappedToOffHand().equals(itemStack)) {
            itemStack = event.getItemSwappedToOffHand();
        } else if (hand == InteractionHand.MAIN_HAND && !event.getItemSwappedToMainHand().equals(itemStack)) {
            itemStack = event.getItemSwappedToMainHand();
        }
        original.call(instance, hand, itemStack);
    }

    @ModifyExpressionValue(
        method = "method_44900",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getChatDecorator()Lnet/minecraft/network/chat/ChatDecorator;"
        )
    )
    private ChatDecorator kilt$combineChatDecorators(ChatDecorator fabricDecorator) {
        var forgeDecorator = ForgeHooks.getServerChatSubmittedDecorator();
        return (player, message) -> fabricDecorator.decorate(player, message).thenComposeAsync(
            fabricMessage -> forgeDecorator.decorate(player, fabricMessage),
            Util.backgroundExecutor()
        );
    }

    @ModifyExpressionValue(
        method = "method_45064",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;join()Ljava/lang/Object;",
            ordinal = 0
        )
    )
    private <T> T kilt$skipChatWhenMessageNull(T original, @Cancellable CallbackInfo ci) {
        if (original == null) {
            ci.cancel();
        }
        return original;
    }

    @Inject(at = @At("HEAD"), method = "handleCustomPayload", cancellable = true)
    public void kilt$handleCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (NetworkHooks.onCustomPayload(packet, this.connection)) {
            ci.cancel();
        }
    }
}
