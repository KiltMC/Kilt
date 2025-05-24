package xyz.bluspring.kilt.forgeinjects.server.commands;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeleportCommand.class)
public abstract class TeleportCommandInject {
    @Inject(method = "performTeleport", at = @At("HEAD"), cancellable = true)
    private static void kilt$handleTeleportCommandEvent(CallbackInfo ci, @Local(argsOnly = true) Entity entity, @Local(argsOnly = true, ordinal = 0) LocalDoubleRef x, @Local(argsOnly = true, ordinal = 1) LocalDoubleRef y, @Local(argsOnly = true, ordinal = 2) LocalDoubleRef z) {
        var event = ForgeEventFactory.onEntityTeleportCommand(entity, x.get(), y.get(), z.get());

        if (event.isCanceled()) {
            ci.cancel();
            return;
        }

        x.set(event.getTargetX());
        y.set(event.getTargetY());
        z.set(event.getTargetZ());
    }
}
