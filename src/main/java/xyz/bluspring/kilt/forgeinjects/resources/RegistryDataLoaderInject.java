package xyz.bluspring.kilt.forgeinjects.resources;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Consumer;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderInject {
    /*@ModifyReturnValue(method = "registryDirPath", at = @At("RETURN")) // Kilt: Fabric handles this for us
    private static String kilt$prefixNamespaceIfForge(String original, @Local(argsOnly = true) ResourceLocation location) {
        if (DataPackRegistriesHooks.kilt$forgeDatapackRegistryKeys.contains(location)) {
            return ForgeHooks.prefixNamespace(location);
        }

        return original;
    }*/

    @Inject(method = "loadRegistryContents", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Decoder;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", shift = At.Shift.BEFORE, remap = false))
    private static <E> void kilt$checkShouldRegisterEntry(RegistryOps.RegistryInfoLookup lookup, ResourceManager manager, ResourceKey<? extends Registry<E>> registryKey, WritableRegistry<E> registry, Decoder<E> decoder, Map<ResourceKey<?>, Exception> exceptions, CallbackInfo ci, @Share("shouldRegisterEntry") LocalBooleanRef shouldRegisterEntry, @Local JsonElement jsonElement) {
        shouldRegisterEntry.set(true);

        if (!ICondition.shouldRegisterEntry(jsonElement)) {
            shouldRegisterEntry.set(false);
        }
    }

    @WrapOperation(method = "loadRegistryContents", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DataResult;getOrThrow(ZLjava/util/function/Consumer;)Ljava/lang/Object;", remap = false))
    private static <R> R kilt$disableGetOrThrow(DataResult instance, boolean allowPartial, Consumer<String> onError, Operation<R> original, @Share("shouldRegisterEntry") LocalBooleanRef shouldRegisterEntry) {
        // hoping mods don't rely on this :V
        if (!shouldRegisterEntry.get())
            return null;

        return original.call(instance, allowPartial, onError);
    }

    @WrapWithCondition(method = "loadRegistryContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/Holder$Reference;"))
    private static <T> boolean kilt$disableEntryRegister(WritableRegistry<T> instance, ResourceKey<T> tResourceKey, T t, Lifecycle lifecycle, @Share("shouldRegisterEntry") LocalBooleanRef shouldRegisterEntry) {
        return shouldRegisterEntry.get();
    }
}
