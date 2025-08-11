// TRACKED HASH: 030f407a6354673d00123c0bcaa1c7f070d001e1
package xyz.bluspring.kilt.injects.commands.arguments;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(ObjectiveArgument.class)
public abstract class ObjectiveArgumentInject {
    @Inject(method = "getObjective", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/scores/Scoreboard;getObjective(Ljava/lang/String;)Lnet/minecraft/world/scores/Objective;"))
    private static void kilt$useSourceScoreboard(CommandContext<CommandSourceStack> context, String name, CallbackInfoReturnable<Objective> cir, @Local LocalRef<Scoreboard> scoreboard) {
        scoreboard.set(context.getSource().getScoreboard());
    }

    @Redirect(method = "listSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerScoreboard;getObjectiveNames()Ljava/util/Collection;"))
    private Collection<String> kilt$useSourceScoreboardNames(ServerScoreboard instance, @Local CommandSourceStack source) {
        return source.getScoreboard().getObjectiveNames();
    }
}