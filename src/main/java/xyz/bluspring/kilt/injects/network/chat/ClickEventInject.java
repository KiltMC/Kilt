package xyz.bluspring.kilt.injects.network.chat;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.chat.ClickEvent;

@Mixin(ClickEvent.class)
public abstract class ClickEventInject {
    @Mixin(ClickEvent.Action.class)
    public abstract static class ActionInject {
        @Definition(id = "action", local = @Local(type = ClickEvent.Action.class, argsOnly = true))
        @Definition(id = "isAllowedFromServer", method = "Lnet/minecraft/network/chat/ClickEvent$Action;isAllowedFromServer()Z")
        @Expression("action.isAllowedFromServer() == false")
        @ModifyExpressionValue(method = "filterForSerialization", at = @At("MIXINEXTRAS:EXPRESSION"))
        private static boolean kilt$allowCommandsOnIntegrated(boolean original) {
            return original && (ServerLifecycleHooks.getCurrentServer() == null || ServerLifecycleHooks.getCurrentServer().isDedicatedServer());
        }
    }
}
