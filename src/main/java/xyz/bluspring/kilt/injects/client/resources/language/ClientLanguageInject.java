package xyz.bluspring.kilt.injects.client.resources.language;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.i18n.I18nManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.locale.LanguageInjection;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ClientLanguage.class)
public abstract class ClientLanguageInject implements LanguageInjection {
    @Shadow @Final private Map<String, String> storage;
    @Shadow private static void appendFrom(String string, List<Resource> list, Map<String, String> map) {}

    @Unique private Map<String, Component> componentStorage = Map.of();
    @Unique private static Map<String, Component> kilt$currentComponentStorage;

    private ClientLanguageInject(Map<String, String> storage, boolean defaultRightToLeft) {
    }

    @CreateInitializer
    private ClientLanguageInject(Map<String, String> storage, boolean defaultRightToLeft, Map<String, Component> componentStorage) {
        this(storage, defaultRightToLeft);
        this.componentStorage = componentStorage;
    }

    @Inject(method = "loadFrom", at = @At("HEAD"))
    private static void kilt$initComponentMap(ResourceManager resourceManager, List<String> filenames, boolean defaultRightToLeft, CallbackInfoReturnable<ClientLanguage> cir, @Share("componentMap") LocalRef<Map<String, Component>> componentMap) {
        componentMap.set(Maps.newHashMap());
    }

    @Inject(method = "loadFrom", at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", shift = At.Shift.AFTER))
    private static void kilt$loadNeoTranslations(ResourceManager resourceManager, List<String> filenames, boolean defaultRightToLeft, CallbackInfoReturnable<ClientLanguage> cir,
                                                 @Local String language, @Local Map<String, String> storage) {
        storage.putAll(I18nManager.loadTranslations(language));
    }

    @WrapOperation(method = "loadFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/language/ClientLanguage;appendFrom(Ljava/lang/String;Ljava/util/List;Ljava/util/Map;)V"))
    private static void kilt$appendDataToComponentMap(String string, List<Resource> list, Map<String, String> map, Operation<Void> original, @Share("componentMap") LocalRef<Map<String, Component>> componentMap) {
        kilt$currentComponentStorage = componentMap.get();
        original.call(string, list, map);
        kilt$currentComponentStorage = null;
    }

    @WrapOperation(method = "appendFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;loadFromJson(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V"))
    private static void kilt$appendJsonLoadToComponentMap(InputStream inputStream, BiConsumer<String, String> biConsumer, Operation<Void> original) {
        if (kilt$currentComponentStorage != null) {
            LanguageInjection.kilt$componentOutput.set(kilt$currentComponentStorage::put);
        }

        original.call(inputStream, biConsumer);

        LanguageInjection.kilt$componentOutput.remove();
    }

    @CreateStatic
    private static void appendFrom(String languageName, List<Resource> resources, Map<String, String> destinationMap, Map<String, Component> componentMap) {
        LanguageInjection.kilt$componentOutput.set(componentMap::put);
        appendFrom(languageName, resources, destinationMap);
        LanguageInjection.kilt$componentOutput.remove();
    }

    @Override
    public Map<String, String> getLanguageData() {
        return storage;
    }

    @Override
    public @Nullable Component getComponent(String key) {
        return this.componentStorage.get(key);
    }
}
