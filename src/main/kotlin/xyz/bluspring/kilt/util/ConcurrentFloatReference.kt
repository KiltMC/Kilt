package xyz.bluspring.kilt.util

import java.util.Collections

// ah yes, this sounds like a great way to do "atomic floats" lmao.
class ConcurrentFloatReference {
    private var current = Collections.synchronizedMap(mutableMapOf<Thread, Float>())

    fun getOrElse(value: Float): Float {
        return current[Thread.currentThread()] ?: value
    }

    fun set(value: Float) {
        current[Thread.currentThread()] = value
    }

    fun reset() {
        current.remove(Thread.currentThread())
    }
}