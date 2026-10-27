package xyz.bluspring.kilt.injections.util.context;

import net.minecraft.util.context.ContextMap;

public interface ContextMapInjection {
    ContextMap EMPTY = new ContextMap.Builder()
        .create(ContextKeySetInjection.EMPTY);
}
