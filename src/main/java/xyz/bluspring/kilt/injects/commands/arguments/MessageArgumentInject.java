// TRACKED HASH: 856e5b736d022bee140bbfe0d7b9e81f727b50e2
package xyz.bluspring.kilt.injects.commands.arguments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.MessageArgument;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MessageArgument.class)
public abstract class MessageArgumentInject {
    @Mixin(MessageArgument.Message.class)
    public static abstract class MessageInject {
        @WrapOperation(method = "resolveComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/arguments/selector/EntitySelectorParser;allowSelectors(Ljava/lang/Object;)Z"))
        private <S> boolean kilt$checkIfCanUseSelectors(S object, Operation<Boolean> original) {
            return original.call(object) || CommonHooks.canUseEntitySelectors((SharedSuggestionProvider) object);
        }
    }
}