// TRACKED HASH: 400a9b89f8ca95162963fb4fdbbc9877f5ae0add
package xyz.bluspring.kilt.injects.resources;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.ICustomHolderSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Function;

@Mixin(HolderSetCodec.class)
public abstract class HolderSetCodecInject<E> {
    @Shadow @Final private Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;
    @Unique private Codec<ICustomHolderSet<E>> forgeDispatchCodec;
    @Unique private Codec<Either<ICustomHolderSet<E>, Either<TagKey<E>, List<Holder<E>>>>> combinedCodec;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initHolderSetCodecs(ResourceKey<? extends Registry<E>> registryKey, Codec<Holder<E>> elementCodec, boolean disallowInline, CallbackInfo ci) {
        this.forgeDispatchCodec = NeoForgeRegistries.HOLDER_SET_TYPES.byNameCodec()
            .dispatch(ICustomHolderSet::type, type -> type.makeCodec(registryKey, elementCodec, disallowInline));

        this.combinedCodec = Codec.either(this.forgeDispatchCodec, this.registryAwareCodec);
    }

    @ModifyReceiver(method = "decode", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;decode(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"))
    private <T> Codec kilt$useCombinedCodec(Codec<E> instance, DynamicOps<T> dynamicOps, T o) {
        return Codec.either(this.forgeDispatchCodec, instance);
    }

    @Redirect(method = "method_40386", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;"))
    private static <T, E> E kilt$mapEitherCalls(Either<ICustomHolderSet<T>, Either<TagKey<T>, List<Holder<T>>>> instance, Function<TagKey<T>, HolderSet.Named<T>> function, Function<List<Holder<T>>, HolderSet.Direct<T>> function2) {
        return (E) instance.map(DataResult::success, tagOrList -> tagOrList.map(function, function2));
    }

    @Inject(method = "encode(Lnet/minecraft/core/HolderSet;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;encode(Ljava/lang/Object;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;"), cancellable = true)
    private <T> void kilt$encodeForgeCustomHolderSet(HolderSet<E> input, DynamicOps<T> ops, T prefix, CallbackInfoReturnable<DataResult<T>> cir) {
        if (input instanceof ICustomHolderSet<E> customHolderSet)
            cir.setReturnValue(this.forgeDispatchCodec.encode(customHolderSet, ops, prefix));
    }
}