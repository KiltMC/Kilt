package xyz.bluspring.kilt.injections.client;

import xyz.bluspring.kilt.util.KiltHelper;

public interface OptionsInjection {
    default void load(boolean limited) {
        throw KiltHelper.createMixinException(OptionsInjection.class, "load");
    }
}
