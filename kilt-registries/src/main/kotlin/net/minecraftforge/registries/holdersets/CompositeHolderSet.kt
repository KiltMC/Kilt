package net.minecraftforge.registries.holdersets

import com.mojang.datafixers.util.Either
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.tags.TagKey
import net.minecraft.util.RandomSource
import xyz.bluspring.kilt.injections.HolderSetInjection
import java.util.*
import java.util.stream.Stream

abstract class CompositeHolderSet<T>(
    val components: MutableList<HolderSet<T>>
) : ICustomHolderSet<T> {
    init {
        components.forEach {
            (it as HolderSetInjection).addInvalidationListener(this::invalidate)
        }
    }

    private val owners = mutableListOf<Runnable>()

    private var set: MutableSet<Holder<T>>? = null
    private var list: MutableList<Holder<T>>? = null

    protected abstract fun createSet(): MutableSet<Holder<T>>

    open fun getSet(): MutableSet<Holder<T>> {
        return set ?: createSet().apply {
            this@CompositeHolderSet.set = this
        }
    }

    open fun getList(): MutableList<Holder<T>> {
        return list ?: this.getSet().toMutableList().apply {
            this@CompositeHolderSet.list = this
        }
    }

    override fun addInvalidationListener(runnable: Runnable) {
        owners.add(runnable)
    }

    fun invalidate() {
        set = null
        list = null
        owners.forEach {
            it.run()
        }
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

    override fun get(i: Int): Holder<T>? {
        return getList()[i]
    }

    override fun contains(holder: Holder<T>): Boolean {
        return getSet().contains(holder)
    }

    override fun isValidInRegistry(registry: Registry<T>): Boolean {
        components.forEach {
            if (!it.isValidInRegistry(registry))
                return false
        }

        return true
    }

    override fun iterator(): MutableIterator<Holder<T>> {
        return getList().iterator()
    }
}