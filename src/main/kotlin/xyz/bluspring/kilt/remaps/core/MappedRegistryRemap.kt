package xyz.bluspring.kilt.remaps.core

import com.mojang.serialization.Lifecycle
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

open class MappedRegistryRemap<T>(resourceKey: ResourceKey<out Registry<T>>, lifecycle: Lifecycle, function: java.util.function.Function<T, Holder.Reference<T>>) : MappedRegistry<T>(resourceKey, lifecycle, function) {
    companion object {
        @JvmStatic
        val knownRegistries = mutableSetOf<ResourceLocation>()
    }
}