package xyz.bluspring.kilt.workarounds

class ResyncingHashMap<K, V>(
    val original: Map<K, V>,
    val compareAgainst: Collection<K>,
    val valueBuilder: (K) -> V
) : MutableMap<K, V> {
    private val internal = HashMap<K, V>(original)
    private val dontReadd = mutableSetOf<K>()

    override val keys: MutableSet<K>
        get() {
            resync()
            return internal.keys
        }

    override val values: MutableCollection<V>
        get() {
            resync()
            return internal.values
        }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() {
            resync()
            return internal.entries
        }

    override val size: Int
        get() {
            resync()
            return internal.size
        }

    override fun put(key: K, value: V): V? = internal.put(key, value)
    override fun remove(key: K): V? {
        dontReadd.add(key)
        return internal.remove(key)
    }
    override fun putAll(from: Map<out K, V>) = internal.putAll(from)
    override fun clear() = internal.clear()
    override fun isEmpty(): Boolean = internal.isEmpty()
    override fun containsKey(key: K): Boolean {
        resync()
        return internal.containsKey(key)
    }

    override fun containsValue(value: V): Boolean {
        resync()
        return internal.containsValue(value)
    }

    override fun get(key: K): V? {
        resync()
        return internal.get(key)
    }

    private fun resync() {
        if (compareAgainst.size > internal.size) {
            for (key in compareAgainst.filter { !internal.contains(it) }) {
                if (dontReadd.contains(key))
                    continue

                internal[key] = valueBuilder.invoke(key)
            }
        }
    }
}