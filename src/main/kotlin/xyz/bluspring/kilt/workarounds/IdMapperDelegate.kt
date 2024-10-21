package xyz.bluspring.kilt.workarounds

import net.minecraft.core.Holder
import net.minecraft.core.IdMapper
import net.minecraft.core.Registry
import java.util.AbstractMap.SimpleEntry

class IdMapperDelegate<T, U>(private val registry: Registry<T>, private val mapper: IdMapper<U>) : Map<Holder.Reference<T>, U> {
    override val entries: Set<Map.Entry<Holder.Reference<T>, U>>
        get() = mapper.mapNotNull {
            val holder = registry.getHolder(mapper.getId(it ?: return@mapNotNull null)).orElseThrow()
            SimpleEntry(holder, it)
        }.toSet()

    override val keys: Set<Holder.Reference<T>>
        get() = mapper.mapNotNull { registry.getHolder(mapper.getId(it ?: return@mapNotNull null)).orElseThrow() }.toSet()
    override val size: Int
        get() = mapper.size()
    override val values: Collection<U>
        get() = mapper.idToT

    override fun isEmpty(): Boolean {
        return mapper.size() <= 0
    }

    override fun get(key: Holder.Reference<T>): U? {
        val id = registry.getId(registry.get(key.key()))
        return mapper.byId(id)
    }

    override fun containsValue(value: U): Boolean {
        return mapper.contains(value)
    }

    override fun containsKey(key: Holder.Reference<T>): Boolean {
        val id = registry.getId(registry.get(key.key()))
        return mapper.contains(id)
    }
}