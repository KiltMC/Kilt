package xyz.bluspring.kilt.injects.client.resources.language;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.neoforged.neoforge.common.ForgeI18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.locale.LanguageInjection;

@Mixin(I18n.class)
public abstract class I18nInject {
    @Inject(method = "setLanguage", at = @At("TAIL"))
    private static void kilt$loadForgeLanguageData(Language language, CallbackInfo ci) {
        ForgeI18n.loadLanguageData(((LanguageInjection) language).getLanguageData());
    }
}
