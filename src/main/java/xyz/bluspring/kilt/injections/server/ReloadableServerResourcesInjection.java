package xyz.bluspring.kilt.injections.server;

import java.util.Map;

import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.resource.ListenerKey;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;

@FabricInjectedInterface(ReloadableServerResources.class)
public interface ReloadableServerResourcesInjection {
    default Map<ListenerKey<?>, PreparableReloadListener> kilt$getRetainedListeners() {
        throw KiltHelper.createMixinException(ReloadableServerResourcesInjection.class, "kilt$getRetainedListeners");
    }

    default <T extends PreparableReloadListener> T getListener(ListenerKey<T> key) {
        throw KiltHelper.createMixinException(ReloadableServerResourcesInjection.class, "getListener");
    }

    default ICondition.IContext getConditionContext() {
        throw new RuntimeException("mixin, why didn't you add this");
    }

    default HolderLookup.Provider getRegistryLookup() {
        throw new RuntimeException("mixin, why didn't you add this");
    }
}
