package cpw.mods.modlauncher.api

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TypesafeMap {
    private val maps = ConcurrentHashMap<Class<*>, TypesafeMap>()
    private val map = ConcurrentHashMap<Key<*>, Any>()
    private val keys = ConcurrentHashMap<String, Key<Any>>()

    constructor() {}
    constructor(owner: Class<*>) {
        KeyBuildersAccess.keyBuilders.getOrDefault(owner, emptyList()).forEach(Consumer { kb ->
            kb?.buildKey(
                this
            )
        })
        maps[owner] = this
    }

    operator fun <V> get(key: Key<V>): Optional<V & Any> {
        return Optional.ofNullable(key.clz.cast(map.get(key)))
    }

    fun <V> computeIfAbsent(key: Key<V>, valueFunction: Function<in Key<V>, out V>): V {
        return computeIfAbsent(map, key, valueFunction)
    }

    private fun <C1, C2, V> computeIfAbsent(
        map: ConcurrentHashMap<C1, C2>,
        key: Key<V>,
        valueFunction: Function<in Key<V>, out V>
    ): V {
        return map.computeIfAbsent(key as C1, valueFunction as Function<in C1, out C2>) as V
    }

    private fun getKeyIdentifiers(): ConcurrentHashMap<String, Key<Any>> {
        return keys
    }

    /**
     * Unique blackboard key
     */
    class Key<T> private constructor(private val name: String, val clz: Class<T>) : Comparable<Key<T>?> {
        private val uniqueId: Long = idGenerator.getAndIncrement()

        fun name(): String {
            return name
        }

        override fun hashCode(): Int {
            return (uniqueId xor (uniqueId ushr 32)).toInt()
        }

        override fun equals(obj: Any?): Boolean {
            return try {
                uniqueId == (obj as Key<*>?)!!.uniqueId
            } catch (cc: ClassCastException) {
                false
            }
        }

        override operator fun compareTo(o: Key<T>?): Int {
            if (o == null)
                throw RuntimeException("bruh")

            if (this == o) {
                return 0
            }
            if (uniqueId < o.uniqueId) {
                return -1
            }
            if (uniqueId > o.uniqueId) {
                return 1
            }
            throw RuntimeException("Huh?")
        }

        companion object {
            private val idGenerator: AtomicLong = AtomicLong()
            fun <V> getOrCreate(owner: TypesafeMap, name: String, clazz: Class<in V>): Key<V> {
                val result = owner.getKeyIdentifiers().computeIfAbsent(
                    name
                ) { n: String ->
                    Key(
                        n,
                        clazz as Class<Any>
                    )
                } as Key<V>
                require(result.clz == clazz) { "Invalid type" }
                return result
            }

            fun <V> getOrCreate(owner: Supplier<TypesafeMap>, name: String, clazz: Class<V>): Supplier<Key<V>> {
                return Supplier<Key<V>> { getOrCreate(owner.get(), name, clazz) }
            }
        }
    }

    private object KeyBuildersAccess {
        val keyBuilders: MutableMap<Class<*>, MutableList<KeyBuilder<*>?>> = HashMap()
    }

    inner class KeyBuilder<T>(private val name: String, private val clazz: Class<in T>, private val owner: Class<*>) : Supplier<Key<T>?> {
        private var key: Key<T>? = null

        init {
            KeyBuildersAccess.keyBuilders.computeIfAbsent(
                owner
            ) { ArrayList() }.add(this)
        }

        fun buildKey(map: TypesafeMap) {
            key = Key.getOrCreate(map, name, clazz)
        }

        override fun get(): Key<T> {
            if (key == null && maps.containsKey(owner)) {
                buildKey(maps[owner]!!)
            }
            if (key == null) {
                throw NullPointerException("Missing map")
            }

            return key!!
        }
    }
}