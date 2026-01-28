package xyz.bluspring.kilt.injections.core.component;

import net.minecraft.core.component.PatchedDataComponentMap;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(PatchedDataComponentMap.class)
public interface PatchedDataComponentMapInjection {
    default boolean isPatchEmpty() {
        throw KiltHelper.createMixinException(PatchedDataComponentMapInjection.class, "isPatchEmpty");
    }
}
