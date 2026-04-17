package xyz.bluspring.kilt.injections.client.gui.components;

import xyz.bluspring.kilt.util.KiltHelper;

public interface EditBoxInjection {
    default void setTextShadow(boolean textShadow) {
        throw KiltHelper.createMixinException(EditBoxInjection.class, "setTextShadow");
    }

    default boolean getTextShadow() {
        throw KiltHelper.createMixinException(EditBoxInjection.class, "getTextShadow");
    }
}
