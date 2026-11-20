// TRACKED HASH: fbc6d677e3b8f6a1e56400c5c2e9af0eeca35e15
package xyz.bluspring.kilt.injects.commands.arguments.selector;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorInject {
    @Definition(id = "hasPermission", method = "Lnet/minecraft/server/permissions/PermissionSet;hasPermission(Lnet/minecraft/server/permissions/Permission;)Z")
    @Definition(id = "COMMANDS_ENTITY_SELECTORS", field = "Lnet/minecraft/server/permissions/Permissions;COMMANDS_ENTITY_SELECTORS:Lnet/minecraft/server/permissions/Permission;")
    @Expression("?.hasPermission(COMMANDS_ENTITY_SELECTORS)")
    @ModifyExpressionValue(method = "checkPermissions", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIfCanUseSelectors(boolean original, @Local(argsOnly = true, name = "sender") CommandSourceStack sender) {
        return original || CommonHooks.canUseEntitySelectors(sender);
    }
}
