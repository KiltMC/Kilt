package xyz.bluspring.kilt.remaps.gametest.framework

import net.minecraft.gametest.framework.GameTestRegistry
import java.lang.reflect.Method

object GameTestRegistryRemap : GameTestRegistry() {
    @JvmStatic
    fun register(method: Method, allowedNamespaces: Set<String>) {
        GameTestRegistry.register(method)
    }
}