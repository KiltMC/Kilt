package xyz.bluspring.kilt.forgeinjects.resources;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

@Mixin(HolderSetCodec.class)
public abstract class HolderSetCodecInject<E> {
    @Shadow @Final private Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;
    @Unique private Codec<ICustomHolderSet<E>> forgeDispatchCodec;
    @Unique private Codec<Either<ICustomHolderSet<E>, Either<TagKey<E>, List<Holder<E>>>>> combinedCodec;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initHolderSetCodecs(ResourceKey<? extends Registry<E>> registryKey, Codec<Holder<E>> elementCodec, boolean disallowInline, CallbackInfo ci) {
        this.forgeDispatchCodec = ExtraCodecs.lazyInitializedCodec(() -> ForgeRegistries.HOLDER_SET_TYPES.get().getCodec())
            .dispatch(ICustomHolderSet::type, type -> type.makeCodec(registryKey, elementCodec, disallowInline));

        this.combinedCodec = new ExtraCodecs.EitherCodec<>(this.forgeDispatchCodec, this.registryAwareCodec);
    }

    @ModifyReceiver(method = "decode", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;decode(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private <T> Codec kilt$useCombinedCodec(Codec instance, DynamicOps<T> dynamicOps, T o) {
        return this.combinedCodec;
    }

    @Redirect(method = "method_40385", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;"))
    private static <T, E> E kilt$mapEitherCalls(Either<ICustomHolderSet<T>, Either<TagKey<T>, List<Holder<T>>>> instance, Function<TagKey<T>, HolderSet.Named<T>> function, Function<List<Holder<T>>, HolderSet.Direct<T>> function2) {
        return (E) instance.map(Function.identity(), tagOrList -> tagOrList.map(function, function2));
    }
}
