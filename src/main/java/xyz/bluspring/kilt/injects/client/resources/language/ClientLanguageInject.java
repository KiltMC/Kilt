package xyz.bluspring.kilt.injects.client.resources.language;

import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.locale.LanguageInjection;

import java.util.Map;

@Mixin(ClientLanguage.class)
public abstract class ClientLanguageInject implements LanguageInjection {
    @Shadow @Final private Map<String, String> storage;

    @Override
    public Map<String, String> getLanguageData() {
        return storage;
    }
}
