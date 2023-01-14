package xyz.bluspring.kilt.injections;

import net.minecraftforge.common.crafting.conditions.ICondition;

public interface ReloadableServerResourcesInjection {
    default ICondition.IContext getConditionContext() {
        throw new RuntimeException("mixin, why didn't you add this");
    }
}