package xyz.bluspring.kilt.injections.client.gui;

public interface AbstractWidgetInjection {
    int UNSET_FG_COLOR = -1;

    default int getFGColor() {
        throw new IllegalStateException();
    }

    default void setFGColor(int color) {
        throw new IllegalStateException();
    }

    default void clearFGColor() {
        throw new IllegalStateException();
    }

    default void setHeight(int value) {}
}
