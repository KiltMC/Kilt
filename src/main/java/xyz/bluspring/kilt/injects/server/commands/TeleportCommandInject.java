package xyz.bluspring.kilt.injects.server.commands;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

@Mixin(TeleportCommand.class)
public abstract class TeleportCommandInject {
    @Inject(method = "performTeleport", at = @At("HEAD"), cancellable = true)
    private static void kilt$handleTeleportCommandEvent(CallbackInfo ci, @Local(argsOnly = true) ServerLevel level, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true, ordinal = 0) LocalDoubleRef x, @Local(argsOnly = true, ordinal = 1) LocalDoubleRef y, @Local(argsOnly = true, ordinal = 2) LocalDoubleRef z) {
        var event = EventHooks.onEntityTeleportCommand(entity, level, x.get(), y.get(), z.get());

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        x.set(event.getTargetX());
        y.set(event.getTargetY());
        z.set(event.getTargetZ());
    }
}
