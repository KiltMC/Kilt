package net.minecraftforge.fml

import com.google.common.base.Predicate
import xyz.bluspring.kilt.loader.KiltLoader
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

class OptionalMod<T> private constructor (var searched: Boolean, val modId: String) {
    private constructor(modId: String) : this(false, modId)
    private constructor(searched: Boolean) : this(searched, "")

    private var value: T? = null

    private fun getValue(): T? {
        if (!searched) {
            this.value = KiltLoader.instance.getMod(this.modId)?.modObject as? T
            searched = true
        }

        return this.value
    }

    fun get(): T {
        if (getValue() == null)
            throw NoSuchElementException("No value present for $modId")

        return getValue()!!
    }

    fun isPresent(): Boolean {
        return getValue() != null
    }

    fun ifPresent(consumer: Consumer<in T>) {
        if (getValue() != null)
            consumer.accept(getValue()!!)
    }

    fun filter(predicate: Predicate<in T>): OptionalMod<T> {
        return if (!isPresent())
            this
        else
            if (predicate.test(getValue()!!))
                this
            else
                empty()
    }

    fun <U> map(mapper: Function<in T, out U>): Optional<out U> {
        return if (!isPresent())
            Optional.empty()
        else
            Optional.ofNullable(mapper.apply(getValue()!!))
    }

    fun <U> flatMap(mapper: Function<in T, Optional<U>>): Optional<out U> {
        return if (!isPresent())
            Optional.empty()
        else
            mapper.apply(getValue()!!)!!
    }

    fun orElse(other: T?): T? {
        return this.getValue() ?: other
    }

    fun orElseGet(other: Supplier<out T?>): T? {
        return this.getValue() ?: other.get()
    }

    fun <X : Throwable> orElseThrow(exceptionSupplier: Supplier<X>): T {
        return getValue() ?: throw exceptionSupplier.get()
    }

    override fun equals(other: Any?): Boolean {
        if (this == other)
            return true

        if (super.equals(other))
            return true

        if (other is OptionalMod<*>)
            return other.modId == this.modId

        return false
    }

    override fun hashCode(): Int {
        return Objects.hashCode(this.modId)
    }

    companion object {
        private val EMPTY = OptionalMod<Any>(true)

        @JvmStatic
        private fun <T> empty(): OptionalMod<T> {
            return EMPTY as OptionalMod<T>
        }

        @JvmStatic
        fun <T> of(modId: String): OptionalMod<T> {
            return OptionalMod(modId)
        }
    }
}