package xyz.bluspring.kilt.injections.server;

import net.minecraftforge.common.crafting.conditions.ICondition;

public interface ServerAdvancementManagerInjection {
    void kilt$setContext(ICondition.IContext context);
}
