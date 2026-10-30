package xyz.bluspring.kilt.util

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class FallbackDelegate<T : Any>(private val fallbackProvider: () -> T) : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: fallbackProvider()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

fun <T : Any> fallback(provider: () -> T): FallbackDelegate<T> = FallbackDelegate(provider)
