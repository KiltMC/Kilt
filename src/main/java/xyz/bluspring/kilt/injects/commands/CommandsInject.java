// TRACKED HASH: 9bcd5a80b0707b5a3fa5ef5bd41ac6501ade942b
package xyz.bluspring.kilt.injects.commands;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.EventHooks;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class CommandsInject {
    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V"))
    private void kilt$registerForgeCommands(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
        EventHooks.onCommandRegister(this.dispatcher, selection, context);
    }

    @Inject(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;getServer()Lnet/minecraft/server/MinecraftServer;", ordinal = 0), cancellable = true)
    private void kilt$callForgeCommandEvent(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci, @Local(argsOnly = true) LocalRef<ParseResults<CommandSourceStack>> parseResultsRef, @Local CommandSourceStack commandSourceStack) {
        CommandEvent event = new CommandEvent(parseResults);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            if (event.getException() != null) {
                commandSourceStack.sendFailure(Component.literal(Util.describeError(event.getException())));
                LOGGER.error("'/{}' threw an exception", command, event.getException());
            }
            ci.cancel();
            return;
        }
        parseResultsRef.set(event.getParseResults());
    }
}