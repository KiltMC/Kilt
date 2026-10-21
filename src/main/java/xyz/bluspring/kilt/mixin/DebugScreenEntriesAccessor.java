package xyz.bluspring.kilt.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;

@Mixin(DebugScreenEntries.class)
public interface DebugScreenEntriesAccessor {
    @Mutable
    @Accessor("PROFILES")
    static void kilt$setProfiles(Map<DebugScreenProfile, Map<Identifier, DebugScreenEntryStatus>> PROFILES) {
        throw new UnsupportedOperationException();
    }
}
