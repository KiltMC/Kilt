package xyz.bluspring.kilt.mixin.compat.fabric_api.resource.conditions;

import java.util.function.Function;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;

@Mixin(ResourceCondition.class)
public interface ResourceConditionMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static <A, E> Codec<E> kilt$fabric_api$tryHandleNeoTypeName(Codec<A> instance, String typeKey, Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec, Operation<Codec<E>> original) {
        return Codec.withAlternative(
            original.call(instance, typeKey, type, codec),
            original.call(instance, "type", type, codec)
        );
    }
}
