package xyz.bluspring.kilt.injections.core.component;

import net.minecraft.core.component.PatchedDataComponentMap;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(PatchedDataComponentMap.class)
public interface PatchedDataComponentMapInjection {
    boolean isPatchEmpty();
}
