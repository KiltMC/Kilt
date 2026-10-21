package xyz.bluspring.kilt.injections.client.gui.components.debug;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import xyz.bluspring.kilt.mixin.DebugScreenEntriesAccessor;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;

public interface DebugScreenEntriesInjection {
    static void registerModdedDebugEntries() {
        var event = new RegisterDebugEntriesEvent(DebugScreenEntries.allEntries(), DebugScreenEntries.PROFILES);
        ModLoader.postEvent(event);
        DebugScreenEntriesAccessor.kilt$setProfiles(event.validateProfiles());
    }
}
