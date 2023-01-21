package xyz.bluspring.kilt.injections.data;

import net.minecraft.data.DataProvider;

import java.nio.file.Path;
import java.util.List;

public interface DataGeneratorInjection {
    default List<DataProvider> getProviders() {
        throw new IllegalStateException();
    }

    default void addInput(Path value) {
        throw new IllegalStateException();
    }
}
