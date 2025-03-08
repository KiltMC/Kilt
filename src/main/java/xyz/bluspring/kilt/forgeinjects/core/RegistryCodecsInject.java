package xyz.bluspring.kilt.forgeinjects.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegistryCodecs.class)
public abstract class RegistryCodecsInject {
    @Redirect(method = "directCodec", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;unboundedMap(Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/codecs/UnboundedMapCodec;"))
    private static <T> UnboundedMapCodec<? extends Registry<T>, T> kilt$useLenientUnbounded(Codec<? extends Registry<T>> keyCodec, Codec<T> elementCodec) {
        return new UnboundedMapCodec<>(keyCodec, elementCodec);
    }
}
