package xyz.bluspring.kilt.injections.client.resources.language;

import net.minecraft.client.resources.language.LanguageManager;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Locale;

@FabricInjectedInterface(LanguageManager.class)
public interface LanguageManagerInjection {
    default Locale getJavaLocale() {
        throw KiltHelper.createMixinException(LanguageManagerInjection.class, "getJavaLocale");
    }
}
