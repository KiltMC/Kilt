package net.minecraftforge.registries.holdersets

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.resources.HolderSetCodec
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import net.minecraftforge.common.ForgeMod
import xyz.bluspring.kilt.injections.HolderSetInjection
import java.util.*
import java.util.stream.Stream

class NotHolderSet<T>(
    private val registry: Registry<T>,
    private val value: HolderSet<T>
) : ICustomHolderSet<T> {
    init {
        (value as HolderSetInjection).addInvalidationListener(this::invalidate)
    }

    override fun type(): HolderSetType<T> {
        return ForgeMod.NOT_HOLDER_SET.get() as HolderSetType<T>
    }

    private val owners = mutableListOf<Runnable>()
    private var list: MutableList<Holder<T>>? = null

    fun registry(): Registry<T> {
        return registry
    }

    fun value(): HolderSet<T> {
        return value
    }

    override fun addInvalidationListener(runnable: Runnable) {
        owners.add(runnable)
    }

    override fun iterator(): MutableIterator<Holder<T>> {
        return getList().iterator()
    }

    override fun stream(): Stream<Holder<T>> {
        return getList().stream()
    }

    override fun size(): Int {
        return getList().size
    }

    override fun unwrap(): Either<TagKey<T>, MutableList<Holder<T>>> {
        return Either.right(getList())
    }

    override fun getRandomElement(randomSource: RandomSource): Optional<Holder<T>> {
        val list = getList()
        val size = list.size

        return Optional.ofNullable(list[randomSource.nextInt(size)])
    }

    override fun get(i: Int): Holder<T> {
        return getList()[i]
    }

    override fun isValidInRegistry(registry: Registry<T>): Boolean {
        return this.registry == registry
    }

    override fun contains(holder: Holder<T>): Boolean {
        return !value.contains(holder)
    }

    private fun getList(): MutableList<Holder<T>> {
        return list ?: registry.holders()
            .filter { !value.contains(it) }
            .map { it as Holder<T> }
            .toList().toMutableList()
            .apply {
                this@NotHolderSet.list = this
            }
    }

    private fun invalidate() {
        list = null
        owners.forEach {
            it.run()
        }
    }

    companion object {
        @JvmStatic
        fun <T> codec(registryKey: ResourceKey<out Registry<T>>, holderCodec: Codec<Holder<T>>, forceList: Boolean): Codec<out ICustomHolderSet<T>> {
            return RecordCodecBuilder.create {
                it.group(
                    RegistryOps.retrieveRegistry(registryKey)
                        .forGetter { getter ->
                            (getter as NotHolderSet<T>).registry()
                        },
                    HolderSetCodec.create(registryKey, holderCodec, forceList)
                        .fieldOf("value")
                        .forGetter { getter ->
                            (getter as NotHolderSet<T>).value()
                        }
                ).apply(it, ::NotHolderSet)
            }
        }
    }
}