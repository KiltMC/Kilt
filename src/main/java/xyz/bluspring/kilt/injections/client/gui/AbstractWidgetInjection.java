package xyz.bluspring.kilt.injections.client.gui;

public interface AbstractWidgetInjection {
    default int getFGColor() {
        throw new IllegalStateException();
    }

    default void setFGColor(int color) {
        throw new IllegalStateException();
    }

    default void clearFGColor() {
        throw new IllegalStateException();
    }
}
