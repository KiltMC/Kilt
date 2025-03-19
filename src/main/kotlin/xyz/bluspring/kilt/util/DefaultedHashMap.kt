package xyz.bluspring.kilt.util

class DefaultedHashMap<K, V>(capacity: Int, factor: Float) : HashMap<K, V>(capacity, factor) {
    var defaultValue: V? = null

    override fun get(key: K): V? {
        return super.get(key) ?: defaultValue
    }
}