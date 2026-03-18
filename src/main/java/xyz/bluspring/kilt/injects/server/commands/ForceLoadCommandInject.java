package xyz.bluspring.kilt.injects.server.commands;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.commands.ForceLoadCommand;

@Mixin(ForceLoadCommand.class)
public abstract class ForceLoadCommandInject {
    // Kilt TODO: do we add support for this
}
