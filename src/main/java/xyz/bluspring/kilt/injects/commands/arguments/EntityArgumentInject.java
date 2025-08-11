// TRACKED HASH: 97c42202e73e1d47dbfa64a7a8ef74b994638419
package xyz.bluspring.kilt.injects.commands.arguments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityArgument.class)
public abstract class EntityArgumentInject {
    @WrapOperation(method = "listSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/SharedSuggestionProvider;hasPermission(I)Z"))
    private boolean kilt$checkIfCanUseSelectors(SharedSuggestionProvider instance, int i, Operation<Boolean> original) {
        return original.call(instance, i) || CommonHooks.canUseEntitySelectors(instance);
    }
}