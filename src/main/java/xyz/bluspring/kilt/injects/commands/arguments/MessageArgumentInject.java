// TRACKED HASH: 856e5b736d022bee140bbfe0d7b9e81f727b50e2
package xyz.bluspring.kilt.injects.commands.arguments;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;

@Mixin(MessageArgument.class)
public abstract class MessageArgumentInject {
    @Mixin(MessageArgument.Message.class)
    public static abstract class MessageInject {
        @Definition(id = "hasPermission", method = "Lnet/minecraft/server/permissions/PermissionSet;hasPermission(Lnet/minecraft/server/permissions/Permission;)Z")
        @Definition(id = "COMMANDS_ENTITY_SELECTORS", field = "Lnet/minecraft/server/permissions/Permissions;COMMANDS_ENTITY_SELECTORS:Lnet/minecraft/server/permissions/Permission;")
        @Expression("?.hasPermission(COMMANDS_ENTITY_SELECTORS)")
        @ModifyExpressionValue(method = "resolveComponent", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$checkIfCanUseSelectors(boolean original, @Local(argsOnly = true, name = "sender") CommandSourceStack sender) {
            return original || CommonHooks.canUseEntitySelectors(sender);
        }
    }
}
