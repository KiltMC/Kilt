package xyz.bluspring.kilt.injects.resources;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.resources.RegistryDataLoaderInjection;

import java.util.List;
import java.util.function.Consumer;

@Mixin(RegistryDataLoader.class)
public abstract class RegistryDataLoaderInject implements RegistryDataLoaderInjection {
    @Shadow @Final @Mutable public static List<RegistryDataLoader.RegistryData<?>> SYNCHRONIZED_REGISTRIES;
    @Shadow @Final private static Logger LOGGER;

    @Inject(method= "<clinit>", at = @At("TAIL"))
    private static void kilt$grabNetworkableRegistries(CallbackInfo ci) {
        SYNCHRONIZED_REGISTRIES = DataPackRegistriesHooks.grabNetworkableRegistries(SYNCHRONIZED_REGISTRIES);
    }

    @ModifyReceiver(method = "loadElementFromResource", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Decoder;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private static <E, T> Decoder<E> kilt$parseWithCustomDecoder(Decoder<E> instance, DynamicOps<T> ops, T input) {
        return ConditionalOps.createConditionalCodec(NeoForgeExtraCodecs.decodeOnly(instance))
            .map(e -> e.orElse(null));
    }

    @WrapOperation(method = "loadElementFromResource", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;"))
    private static <T> Holder.Reference<T> kilt$onlyRegisterIfNotNull(WritableRegistry<T> instance, ResourceKey<T> tResourceKey, T t, RegistrationInfo registrationInfo, Operation<Holder.Reference<T>> original) {
        if (t != null)
            return original.call(instance, tResourceKey, t, registrationInfo);
        else {
            LOGGER.debug("Skipping loading registry entry {} as its conditions were not met", tResourceKey);
            return null;
        }
    }

    @ModifyExpressionValue(method = "loadContentsFromManager", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/RegistryOps;create(Lcom/mojang/serialization/DynamicOps;Lnet/minecraft/resources/RegistryOps$RegistryInfoLookup;)Lnet/minecraft/resources/RegistryOps;"))
    private static <T> RegistryOps<T> kilt$createConditionalOps(RegistryOps<T> original) {
        return new ConditionalOps<>(original, ICondition.IContext.TAGS_INVALID);
    }

    @Mixin(RegistryDataLoader.RegistryData.class)
    public abstract static class RegistryDataInject<T> implements RegistryDataLoaderInjection.RegistryDataInjection<T> {
        @Unique private Consumer<RegistryBuilder<T>> registryBuilderConsumer = registryBuilder -> {};

        public RegistryDataInject(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty) {
        }

        @CreateInitializer
        public RegistryDataInject(ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty, Consumer<RegistryBuilder<T>> registryBuilderConsumer) {
            this(key, elementCodec, requiredNonEmpty);
            this.registryBuilderConsumer = registryBuilderConsumer;
        }

        @Override
        public void kilt$setRegistryBuilderConsumer(Consumer<RegistryBuilder<T>> builderConsumer) {
            this.registryBuilderConsumer = builderConsumer;
        }

        @Redirect(method = "create", at = @At(value = "NEW", target = "(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/core/MappedRegistry;"))
        private MappedRegistry<T> kilt$createRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle) {
            var registryBuilder = new RegistryBuilder<>(resourceKey);
            this.registryBuilderConsumer.accept(registryBuilder);

            return (MappedRegistry<T>) registryBuilder.disableRegistrationCheck().create();
        }
    }
}
