package xyz.bluspring.kilt.workarounds;

import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

public interface ContextAwareReloadListenerWorkaround {
    default ContextAwareReloadListener kilt$asContextAware() {
        return (ContextAwareReloadListener) this;
    }

    default <T> ConditionalOps<T> kilt$makeConditionalOps(RegistryOps<T> original) {
        return new ConditionalOps<>(original, this.kilt$asContextAware().getContext());
    }
}
