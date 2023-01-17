package xyz.bluspring.kilt.injections.client.gui;

public interface AbstractSelectionListInjection {
    default int getWidth() {
        throw new IllegalStateException();
    }

    default int getHeight() {
        throw new IllegalStateException();
    }

    default int getTop() {
        throw new IllegalStateException();
    }

    default int getBottom() {
        throw new IllegalStateException();
    }

    default int getLeft() {
        throw new IllegalStateException();
    }

    default int getRight() {
        throw new IllegalStateException();
    }
}
