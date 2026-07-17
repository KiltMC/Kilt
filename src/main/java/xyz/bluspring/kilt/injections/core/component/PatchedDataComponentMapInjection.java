package xyz.bluspring.kilt.injections.core.component;

import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;

@FabricInjectedInterface(PatchedDataComponentMap.class)
public interface PatchedDataComponentMapInjection {
    default boolean isPatchEmpty() {
        throw KiltHelper.createMixinException(PatchedDataComponentMapInjection.class, "isPatchEmpty");
    }

    default boolean patchEquals(DataComponentPatch patch) {
        throw KiltHelper.createMixinException(PatchedDataComponentMapInjection.class, "patchEquals");
    }
}
