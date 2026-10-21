package xyz.bluspring.kilt.injects.client.gui.components.debug;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;

@Mixin(DebugScreenEntries.class)
public abstract class DebugScreenEntriesInject {
    // Kilt: all handled by DebugScreenEntriesInjection.
}
