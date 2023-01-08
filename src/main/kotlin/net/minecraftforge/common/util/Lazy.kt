package net.minecraftforge.common.util

import java.util.function.Supplier

interface Lazy<T> : Supplier<T> {
    class Fast<T>(private var supplier: Supplier<T>?) : Lazy<T> {
        private var instance: T? = null

        override fun get(): T {
            if (supplier != null) {
                instance = supplier?.get()
                supplier = null
            }

            return instance!!
        }
    }

    class Concurrent<T>(@Volatile private var supplier: Supplier<T>?) : Lazy<T> {
        @Volatile
        private var lock: Any? = Any()

        @Volatile
        private var instance: T? = null

        override fun get(): T {
            val localLock = this.lock

            if (supplier != null) {
                synchronized(localLock!!) {
                    if (supplier != null) {
                        instance = supplier?.get()
                        supplier = null
                        this.lock = null
                    }
                }
            }

            return instance!!
        }
    }

    companion object {
        @JvmStatic
        fun <T> of(supplier: Supplier<T>): Lazy<T> {
            return Lazy.Fast(supplier)
        }

        @JvmStatic
        fun <T> concurrentOf(supplier: Supplier<T>): Lazy<T> {
            return Lazy.Concurrent(supplier)
        }
    }
}