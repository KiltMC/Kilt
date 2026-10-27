package xyz.bluspring.kilt.injections.client.gui.components;

import java.util.function.Predicate;

import xyz.bluspring.kilt.util.KiltHelper;

public interface EditBoxInjection {
    default void setTextShadow(boolean textShadow) {
        throw KiltHelper.createMixinException(EditBoxInjection.class, "setTextShadow");
    }

    default boolean getTextShadow() {
        throw KiltHelper.createMixinException(EditBoxInjection.class, "getTextShadow");
    }

    default void setFilter(Predicate<String> filter) {
        throw KiltHelper.createMixinException(EditBoxInjection.class, "setFilter");
    }
}
