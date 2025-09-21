package xyz.bluspring.kilt.injections.server;

import net.minecraft.server.ReloadableServerResources;
import net.neoforged.neoforge.common.conditions.ICondition;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(ReloadableServerResources.class)
public interface ReloadableServerResourcesInjection {
    default ICondition.IContext getConditionContext() {
        throw new RuntimeException("mixin, why didn't you add this");
    }
}