// TRACKED HASH: 97c42202e73e1d47dbfa64a7a8ef74b994638419
package xyz.bluspring.kilt.injects.commands.arguments;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;

@Mixin(EntityArgument.class)
public abstract class EntityArgumentInject {
    @Definition(id = "hasPermission", method = "Lnet/minecraft/server/permissions/PermissionSet;hasPermission(Lnet/minecraft/server/permissions/Permission;)Z")
    @Definition(id = "COMMANDS_ENTITY_SELECTORS", field = "Lnet/minecraft/server/permissions/Permissions;COMMANDS_ENTITY_SELECTORS:Lnet/minecraft/server/permissions/Permission;")
    @Expression("?.hasPermission(COMMANDS_ENTITY_SELECTORS)")
    @ModifyExpressionValue(method = "listSuggestions", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIfCanUseSelectors(boolean original, @Local(name = "source") SharedSuggestionProvider source) {
        return original || CommonHooks.canUseEntitySelectors(source);
    }
}
