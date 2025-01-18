package xyz.bluspring.kilt.injections.data;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.Map;

public interface DataGeneratorInjection {
    default Map<String, DataProvider> getProvidersView() {
        throw new IllegalStateException();
    }
    default PackOutput getPackOutput() {
        throw new IllegalStateException();
    }
    default PackOutput getPackOutput(String path) {
        throw new IllegalStateException();
    }
    default <T extends DataProvider> T addProvider(boolean run, DataProvider.Factory<T> factory) {
        throw new IllegalStateException();
    }
    default <T extends DataProvider> T addProvider(boolean run, T provider) {
        throw new IllegalStateException();
    }
}
