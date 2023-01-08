package net.minecraftforge.registries.holderset

import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

fun interface HolderSetType<T> {
    fun makeCodec(registryKey: ResourceKey<out Registry<T>>, holderCodec: Codec<Holder<T>>, forceList: Boolean): Codec<out ICustomHolderSet<T>>
}