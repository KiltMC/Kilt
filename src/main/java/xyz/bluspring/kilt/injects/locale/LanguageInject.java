// TRACKED HASH: 803f79832294bb467746850ed2c5d03a86aa08e3
package xyz.bluspring.kilt.injects.locale;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.JsonOps;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.neoforged.neoforge.server.LanguageHook;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.bluspring.kilt.injections.locale.LanguageInjection;
import xyz.bluspring.kilt.util.IteratorWrapper;

import java.util.*;
import java.util.function.BiConsumer;

@Mixin(Language.class)
public abstract class LanguageInject implements LanguageInjection {
    @WrapOperation(method = "loadDefault", at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;parseTranslations(Ljava/util/function/BiConsumer;Ljava/lang/String;)V"))
    private static void kilt$setupNeoComponentConsumer(BiConsumer<String, String> biConsumer, String string, Operation<Void> original) {
        LanguageInjection.kilt$componentOutput.set(kilt$componentMap.get()::put);
        original.call(biConsumer, string);
        LanguageInjection.kilt$componentOutput.remove();
    }

    @ModifyVariable(method = "loadDefault", at = @At("STORE"))
    private static Map<String, String> kilt$captureLanguageMap(Map<String, String> original) {
        var map = new HashMap<>(original);
        LanguageHook.captureLanguageMap(map, kilt$componentMap.get());

        return map;
    }

    @Mixin(targets = "net.minecraft.locale.Language$1")
    public abstract static class AnonymousLanguageInject implements LanguageInjection {
        @Shadow @Final Map<String, String> val$storage;

        @Override
        public Map<String, String> getLanguageData() {
            return this.val$storage;
        }

        @Override
        public @Nullable Component getComponent(String key) {
            return LanguageInjection.kilt$componentMap.get().get(key);
        }
    }

    @ModifyExpressionValue(method = "loadFromJson", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"))
    private static <E extends Map.Entry<String, JsonElement>> Iterator<E> kilt$tryParseNeoTranslation(Iterator<E> original, @Local(argsOnly = true) BiConsumer<String, String> output) {
        return new IteratorWrapper<>(original, entry -> {
            if (entry.getValue().isJsonArray()) {
                var component = ComponentSerialization.CODEC
                    .parse(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow(msg -> new JsonParseException("Error parsing translation for " + entry.getKey() + ": " + msg));

                output.accept(entry.getKey(), component.getString());

                if (kilt$componentOutput.get() != null)
                    kilt$componentOutput.get().accept(entry.getKey(), component);

                return null;
            }

            return entry;
        });
    }

    @Override
    public Map<String, String> getLanguageData() {
        return ImmutableMap.of();
    }

    @Override
    public @Nullable Component getComponent(String key) {
        return null;
    }
}