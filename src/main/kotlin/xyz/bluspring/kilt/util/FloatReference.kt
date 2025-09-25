package xyz.bluspring.kilt.util

class FloatReference {
    private var current: Float? = null

    fun getOrElse(value: Float): Float {
        return current ?: value
    }

    fun set(value: Float) {
        current = value
    }

    fun reset() {
        current = null
    }
}