package xyz.bluspring.kilt.injections.gametest.framework;

public interface GameTestInjection {
    default String templateNamespace() {
        return "";
    }
}
