package xyz.bluspring.kilt.injections.server.packs.resources;

import java.util.List;

import net.neoforged.neoforge.event.SortedReloadListenerEvent;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface ReloadableResourceManagerInjection {
    default List<PreparableReloadListener> getListeners() {
        throw KiltHelper.createMixinException(ReloadableResourceManagerInjection.class, "getListeners");
    }

    default void updateListenersFrom(SortedReloadListenerEvent event) {
        throw KiltHelper.createMixinException(ReloadableResourceManagerInjection.class, "updateListenersFrom");
    }
}
