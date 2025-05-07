package xyz.bluspring.kilt.util

import com.google.common.cache.CacheBuilder
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class CacheSet<E> : MutableSet<E> {
    private val deferred = Collections.newSetFromMap(CacheBuilder.newBuilder()
        .expireAfterAccess(5.minutes.toJavaDuration())
        .build<E, Boolean>()
        .asMap())

    override fun iterator(): MutableIterator<E> {
        return deferred.iterator()
    }

    override fun add(element: E): Boolean {
        return deferred.add(element)
    }

    override fun remove(element: E): Boolean {
        return deferred.remove(element)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        return deferred.addAll(elements)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        return deferred.removeAll(elements)
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        return deferred.retainAll(elements)
    }

    override fun clear() {
        deferred.clear()
    }

    override val size: Int
        get() = deferred.size

    override fun isEmpty(): Boolean {
        return deferred.isEmpty()
    }

    override fun contains(element: E): Boolean {
        return deferred.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return deferred.containsAll(elements)
    }
}