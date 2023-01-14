package net.minecraftforge.registries.holdersets

import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.resources.HolderSetCodec
import net.minecraft.resources.ResourceKey
import net.minecraftforge.common.ForgeMod

open class AndHolderSet<T>(values: MutableList<HolderSet<T>>) : CompositeHolderSet<T>(values) {
    override fun createSet(): MutableSet<Holder<T>> {
        if (components.isEmpty())
            return mutableSetOf()

        if (components.size == 1)
            return components[0].toMutableSet()

        val remainingComponents = components.subList(1, components.size)
        return components[0].filter {
            remainingComponents.all { comp -> comp.contains(it) }
        }.toMutableSet()
    }

    override fun type(): HolderSetType {
        return ForgeMod.AND_HOLDER_SET.get() as HolderSetType
    }

    companion object {
        @JvmStatic
        fun <T> codec(registryKey: ResourceKey<out Registry<T>>, holderCodec: Codec<Holder<T>>, forceList: Boolean): Codec<out ICustomHolderSet<T>> {
            return HolderSetCodec.create(registryKey, holderCodec, forceList)
                .listOf()
                .xmap(::AndHolderSet) {
                    it.components
                }
                .fieldOf("values")
                .codec()
        }
    }
}