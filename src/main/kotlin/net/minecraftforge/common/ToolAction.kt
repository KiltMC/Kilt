package net.minecraftforge.common

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class ToolAction private constructor(private val name: String) {
    val fabricToolAction = io.github.fabricators_of_create.porting_lib.util.ToolAction.get(name)

    fun name(): String {
        return name
    }

    companion object {
        private val actions = ConcurrentHashMap<String, ToolAction>()

        @JvmStatic
        fun getActions(): Collection<ToolAction> {
            return Collections.unmodifiableCollection(actions.values)
        }

        @JvmStatic
        fun get(name: String): ToolAction {
            return actions.computeIfAbsent(name, ::ToolAction)
        }
    }
}