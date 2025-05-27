package net.minecraftforge.jarjar.nio.util

import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

class Lazy<T> {
    companion object {
        @JvmStatic
        fun <T> of(): Lazy<T> {
            return Lazy<T>(null)
        }

        @JvmStatic
        fun <T> of(value: T): Lazy<T> {
            return Lazy(value)
        }

        @JvmStatic
        fun <T> of(provider: Supplier<T>): Lazy<T> {
            return Lazy(provider)
        }
    }

    private val lock = Object()
    private var value: T? = null
    private var initialized = false
    private val provider: Supplier<T>?

    constructor(value: T) {
        this.value = value
        this.initialized = true
        this.provider = Supplier { value }
    }

    constructor(provider: Supplier<T>?) {
        this.value = null
        this.initialized = false
        this.provider = provider
    }

    fun get(): T {
        synchronized(lock) {
            if (!initialized && provider != null) {
                initialized = true
                this.value = provider.get()
            }

            return this.value!!
        }
    }

    fun ifPresent(consumer: Consumer<T?>) {
        synchronized(lock) {
            if (!initialized)
                return

            consumer.accept(this.value)
        }
    }

    fun <R> map(mapper: Function<T, R>): Lazy<R> {
        synchronized(lock) {
            return of { mapper.apply(this.get()) }
        }
    }

    fun orElse(elseValue: T?): T? {
        synchronized(lock) {
            if (!initialized)
                return elseValue

            return this.value
        }
    }
}