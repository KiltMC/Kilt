package xyz.bluspring.kilt.injections.data;

import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.Map;

public interface DataGeneratorInjection {
    Map<String, DataProvider> getProvidersView();
    PackOutput getPackOutput();
    PackOutput getPackOutput(String path);
    <T extends DataProvider> T addProvider(boolean run, DataProvider.Factory<T> factory);
    <T extends DataProvider> T addProvider(boolean run, T provider);
}
