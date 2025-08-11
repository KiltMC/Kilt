package xyz.bluspring.kilt.injects.client.multiplayer.chat;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatListener.class)
public abstract class ChatListenerInject {
    @ModifyArg(method = "method_45745", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    private Component kilt$useForgeComponent(Component chatComponent, @Local(argsOnly = true) ChatType.Bound boundType, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        var forgeComponent = ClientHooks.onClientChat(boundType, chatComponent, Util.NIL_UUID);

        if (forgeComponent == null) {
            cir.setReturnValue(false);
            return null;
        }

        return chatComponent;
    }

    @ModifyArg(method = "showMessageToPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"))
    private Component kilt$useForgeComponent(Component chatComponent, @Local(argsOnly = true) ChatType.Bound boundType, @Local(argsOnly = true) PlayerChatMessage chatMessage, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        var forgeComponent = ClientHooks.onClientPlayerChat(boundType, chatComponent, chatMessage, chatMessage.sender());

        if (forgeComponent == null) {
            cir.setReturnValue(false);
            return null;
        }

        return chatComponent;
    }

    @Inject(method = "handleSystemMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/ChatListener;guessChatUUID(Lnet/minecraft/network/chat/Component;)Ljava/util/UUID;", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$tryHandleSystemChat(Component message, boolean isOverlay, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Component> componentRef) {
        componentRef.set(ClientHooks.onClientSystemChat(message, isOverlay));

        if (componentRef.get() == null) {
            ci.cancel();
        }
    }
}
