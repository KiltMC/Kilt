package xyz.bluspring.kilt.injections.client.gui.components;

import xyz.bluspring.kilt.util.KiltHelper;

public interface AbstractWidgetInjection {
    int UNSET_FG_COLOR = -1;

    default int getFGColor() {
        throw KiltHelper.createMixinException(AbstractWidgetInjection.class, "getFGColor");
    }

    default void setFGColor(int color) {
        throw KiltHelper.createMixinException(AbstractWidgetInjection.class, "setFGColor");
    }

    default void clearFGColor() {
        throw KiltHelper.createMixinException(AbstractWidgetInjection.class, "clearFGColor");
    }
}
