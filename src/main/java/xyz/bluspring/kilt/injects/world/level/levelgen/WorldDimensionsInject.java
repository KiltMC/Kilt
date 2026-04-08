package xyz.bluspring.kilt.injects.world.level.levelgen;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.levelgen.WorldDimensions;

@Mixin(WorldDimensions.class)
public abstract class WorldDimensionsInject {
    // Kilt TODO: do we want to do this?
    /*@Redirect(method = "method_45516", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;unboundedMap(Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/codecs/UnboundedMapCodec;"))
    private static <K, V> UnboundedMapCodec<K, V> kilt$useLenientCodec(Codec<K> keyCodec, Codec<V> elementCodec) {
        return new LenientUnboundedMapCodec<>(keyCodec, elementCodec);
    }*/
}
