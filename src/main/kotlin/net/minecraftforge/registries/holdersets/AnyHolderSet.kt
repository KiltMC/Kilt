package net.minecraftforge.registries.holdersets

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import net.minecraftforge.common.ForgeMod
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

@JvmRecord
data class AnyHolderSet<T>(val registry: Registry<T>) : ICustomHolderSet<T> {
    override fun type(): HolderSetType<T> {
        return ForgeMod.ANY_HOLDER_SET.get() as HolderSetType<T>
    }

    override fun iterator(): MutableIterator<Holder<T>> {
        return stream().iterator()
    }

    override fun stream(): Stream<Holder<T>> {
        return registry.holders().map(Function.identity())
    }

    override fun size(): Int {
        return registry.size()
    }

    override fun unwrap(): Either<TagKey<T>, MutableList<Holder<T>>> {
        return Either.right(stream().toList())
    }

    override fun getRandomElement(randomSource: RandomSource): Optional<Holder<T>> {
        return registry.getRandom(randomSource)
    }

    override fun get(i: Int): Holder<T> {
        return registry.getHolder(i).orElseThrow {
            NoSuchElementException("No element $i in registry ${registry.key()}")
        }
    }

    override fun isValidInRegistry(registry: Registry<T>): Boolean {
        return this.registry == registry
    }

    override fun contains(holder: Holder<T>): Boolean {
        return holder.unwrapKey().map(registry::containsKey).orElse(false)
    }

    companion object {
        @JvmStatic
        fun <T> codec(registryKey: ResourceKey<out Registry<T>>, holderCodec: Codec<Holder<T>>, forceList: Boolean): Codec<out ICustomHolderSet<T>> {
            return RegistryOps.retrieveRegistry(registryKey)
                .xmap(::AnyHolderSet) {
                    it.registry
                }.codec()
        }
    }
}