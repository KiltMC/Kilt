package net.minecraftforge.registries.holderset

import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.resources.HolderSetCodec
import net.minecraft.resources.ResourceKey
import net.minecraftforge.common.ForgeMod

open class OrHolderSet<T>(values: MutableList<HolderSet<T>>) : CompositeHolderSet<T>(values) {
    override fun createSet(): MutableSet<Holder<T>> {
        return components.stream().flatMap {
            it.stream()
        }.toList().toMutableSet()
    }

    override fun type(): HolderSetType<T> {
        return ForgeMod.OR_HOLDER_SET.get() as HolderSetType<T>
    }

    companion object {
        @JvmStatic
        fun <T> codec(registryKey: ResourceKey<out Registry<T>>, holderCodec: Codec<Holder<T>>, forceList: Boolean): Codec<out ICustomHolderSet<T>> {
            return HolderSetCodec.create(registryKey, holderCodec, forceList)
                .listOf()
                .xmap(::OrHolderSet) {
                    it.components
                }
                .fieldOf("values")
                .codec()
        }
    }
}