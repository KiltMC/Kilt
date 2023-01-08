package net.minecraftforge.common

import java.util.concurrent.ConcurrentHashMap

class SoundAction private constructor(private val name: String) {
    fun name(): String {
        return this.name
    }

    override fun toString(): String {
        return "SoundAction[$name]"
    }

    companion object {
        private val ACTIONS = ConcurrentHashMap<String, SoundAction>()

        @JvmStatic
        fun get(name: String): SoundAction {
            return ACTIONS.computeIfAbsent(name, ::SoundAction)
        }
    }
}