package xyz.bluspring.kilt.injections.client.resources.language;

import net.minecraft.client.resources.language.LanguageManager;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import java.util.Locale;

@FabricInjectedInterface(LanguageManager.class)
public interface LanguageManagerInjection {
    Locale getJavaLocale();
}
