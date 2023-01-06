package net.minecraftforge.common.util

import io.github.fabricators_of_create.porting_lib.util.NonNullConsumer
import java.util.*

class LazyOptional<T>(private val fabricLazyOptional: io.github.fabricators_of_create.porting_lib.util.LazyOptional<T>) {
    fun addListener(listener: NonNullConsumer<LazyOptional<T>>) {
        fabricLazyOptional.addListener {
            listener.accept(LazyOptional(it))
        }
    }

    fun <X> cast(): LazyOptional<X> {
        return LazyOptional(fabricLazyOptional.cast())
    }

    fun filter(predicate: NonNullPredicate<in T>): Optional<T> {
        return fabricLazyOptional.filter {
            predicate.test(it)
        }
    }

    fun ifPresent(consumer: NonNullConsumer<in T>) {
        fabricLazyOptional.ifPresent {
            consumer.accept(it)
        }
    }

    fun invalidate() {
        fabricLazyOptional.invalidate()
    }

    // in T = ? super T, out U = ? extends U
    fun <U> lazyMap(mapper: NonNullFunction<in T, out U>): LazyOptional<U> {
        return LazyOptional(fabricLazyOptional.lazyMap(mapper))
    }

    fun <U> map(mapper: NonNullFunction<in T, out U>): Optional<U> {
        return fabricLazyOptional.map(mapper)
    }

    fun orElse(other: T): T {
        return fabricLazyOptional.orElse(other)
    }

    fun orElseGet(other: NonNullSupplier<out T>): T {
        return fabricLazyOptional.orElseGet(other)
    }

    fun <X : Throwable> orElseThrow(exceptionSupplier: NonNullSupplier<out X>): T {
        return fabricLazyOptional.orElseThrow(exceptionSupplier)
    }

    fun resolve(): Optional<T> {
        return fabricLazyOptional.resolve()
    }

    companion object {
        private val EMPTY = LazyOptional<Void>(io.github.fabricators_of_create.porting_lib.util.LazyOptional.empty())

        @JvmStatic
        fun <T> empty(): LazyOptional<T> {
            return EMPTY.cast()
        }

        @JvmStatic
        fun <T> of(instanceSupplier: NonNullSupplier<T>): LazyOptional<T> {
            return LazyOptional(io.github.fabricators_of_create.porting_lib.util.LazyOptional.of(instanceSupplier))
        }
    }
}