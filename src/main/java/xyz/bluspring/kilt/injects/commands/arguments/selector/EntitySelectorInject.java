// TRACKED HASH: fbc6d677e3b8f6a1e56400c5c2e9af0eeca35e15
package xyz.bluspring.kilt.injects.commands.arguments.selector;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorInject {
    @WrapOperation(method = "checkPermissions", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;hasPermission(I)Z"))
    private boolean kilt$checkIfCanUseSelectors(CommandSourceStack instance, int i, Operation<Boolean> original) {
        return original.call(instance, i) || CommonHooks.canUseEntitySelectors(instance);
    }
}