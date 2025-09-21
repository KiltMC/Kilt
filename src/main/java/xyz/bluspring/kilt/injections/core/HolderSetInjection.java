package xyz.bluspring.kilt.injections.core;

import net.minecraft.core.HolderSet;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(HolderSet.class)
public interface HolderSetInjection {
    default void addInvalidationListener(Runnable runnable) {
    }
}