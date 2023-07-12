package xyz.bluspring.kilt.forgeinjects.server.packs.resources;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerInject {
    @Shadow @Final private List<PreparableReloadListener> listeners;

    @Shadow public abstract void registerReloadListener(PreparableReloadListener listener);

    @Unique
    public void registerReloadListenerIfNotPresent(PreparableReloadListener listener) {
        if (!this.listeners.contains(listener)) {
            this.registerReloadListener(listener);
        }
    }
}
