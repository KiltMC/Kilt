// TRACKED HASH: fcbd779256061257dc96dee80bbb7cb48e719c79
package xyz.bluspring.kilt.injects.server.packs.resources;

import java.util.List;

import net.neoforged.neoforge.event.SortedReloadListenerEvent;
import net.neoforged.neoforge.resource.ReloadListenerSort;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.server.packs.resources.ReloadableResourceManagerInjection;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerInject implements ReloadableResourceManagerInjection {
    @Shadow public List<PreparableReloadListener> listeners;

    @Override
    public List<PreparableReloadListener> getListeners() {
        return this.listeners;
    }

    @Override
    public void updateListenersFrom(SortedReloadListenerEvent event) {
        this.listeners = ReloadListenerSort.sort(event);
    }
}
