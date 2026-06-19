package xyz.bluspring.kilt.compat.fabric.cctweaked;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.function.Consumer;

public interface ComponentAccessImplExt {
    void kilt$initializeCapabilityLookups(BlockCapability<?, Direction> capability, Consumer<Direction> invalidate);
}
