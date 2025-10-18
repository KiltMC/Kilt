package xyz.bluspring.kilt.injections.locale;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

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

    default Map<String, String> getLanguageData() {
        throw KiltHelper.createMixinException(LanguageInjection.class, "getLanguageData");
    }

    default @Nullable Component getComponent(String key) {
        throw KiltHelper.createMixinException(LanguageInjection.class, "getComponent");
    }
}
