package xyz.bluspring.kilt.compat.transfer

import com.google.common.collect.ForwardingMap

class AlternativeCapabilityMap<K, V>(private val delegate: MutableMap<K, V>, val provider: (K) -> V) : ForwardingMap<K, V>() {
    override fun delegate(): Map<K, V> = this.delegate

    override fun get(key: K?): V? {
        var value = super.get(key)

        if (key != null && value == null) {
            value = this.provider(key)
            this[key] = value
            return value
        }

        return value
    }
}
