package xyz.bluspring.kilt.injections.locale;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public interface LanguageInjection {
    ThreadLocal<Map<String, Component>> kilt$componentMap = ThreadLocal.withInitial(HashMap::new);
    ThreadLocal<BiConsumer<String, Component>> kilt$componentOutput = new ThreadLocal<>();

    static void loadFromJson(InputStream stream, BiConsumer<String, String> output, BiConsumer<String, Component> componentOutput) {
        kilt$componentOutput.set(componentOutput);
        Language.loadFromJson(stream, output);
        kilt$componentOutput.remove();
    }

    Map<String, String> getLanguageData();
    @Nullable Component getComponent(String key);
}
