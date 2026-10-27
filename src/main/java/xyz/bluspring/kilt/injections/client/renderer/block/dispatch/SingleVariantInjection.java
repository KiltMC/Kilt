package xyz.bluspring.kilt.injections.client.renderer.block.dispatch;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.block.dispatch.SingleVariant;

public interface SingleVariantInjection {
    interface UnbakedInjection {
        MapCodec<SingleVariant.Unbaked> MAP_CODEC = MapCodec.assumeMapUnsafe(SingleVariant.Unbaked.CODEC); // Kilt TODO: is this actually correct?
    }
}
