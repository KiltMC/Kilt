package xyz.bluspring.kilt.forgeinjects.client.resources.language;

import net.minecraft.client.resources.language.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.resources.language.LanguageManagerInjection;

import java.util.Locale;

@Mixin(LanguageManager.class)
public abstract class LanguageManagerInject implements LanguageManagerInjection {
    @Shadow public abstract void setSelected(String selected);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setJavaLocaleOnInit(String currentCode, CallbackInfo ci) {
        this.setSelected(currentCode);
    }

    @Unique
    private Locale javaLocale;

    @Override
    public Locale getJavaLocale() {
        return javaLocale;
    }

    @Inject(method = "setSelected", at = @At("TAIL"))
    private void kilt$setJavaLocale(String selected, CallbackInfo ci) {
        var langSplit = selected.split("_", 2);
        this.javaLocale = langSplit.length == 1 ? new Locale(langSplit[0]) : new Locale(langSplit[0], langSplit[1]);
    }
}
