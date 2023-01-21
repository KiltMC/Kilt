package xyz.bluspring.kilt.injections;

import java.util.List;

public interface DataPackConfigInjection {
    default void addModPacks(List<String> modPacks) {
        throw new IllegalStateException();
    }
}
