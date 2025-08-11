// TRACKED HASH: 9bcd5a80b0707b5a3fa5ef5bd41ac6501ade942b
package xyz.bluspring.kilt.injects.commands;

import com.google.common.base.Throwables;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class CommandsInject {
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V"))
    private void kilt$registerForgeCommands(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
        EventHooks.onCommandRegister(this.dispatcher, selection, context);
    }

    @WrapOperation(method = "performCommand", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I"))
    private int kilt$callForgeCommandEvent(CommandDispatcher<CommandSourceStack> instance, ParseResults<CommandSourceStack> results, Operation<Integer> original) throws Exception {
        var event = new CommandEvent(results);

        if (NeoForge.EVENT_BUS.post(event)) {
            if (event.getException() instanceof Exception e) {
                throw e;
            } else if (event.getException() != null) {
                Throwables.throwIfUnchecked(event.getException());
            }

            return 1;
        }

        return original.call(instance, results);
    }
}