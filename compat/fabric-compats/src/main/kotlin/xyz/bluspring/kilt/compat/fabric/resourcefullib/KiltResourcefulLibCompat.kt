package xyz.bluspring.kilt.compat.fabric.resourcefullib

import net.neoforged.neoforge.registries.DeferredRegister
import xyz.bluspring.kilt.Kilt

object KiltResourcefulLibCompat {
    @JvmStatic
    fun <T> attachToModContainer(modId: String, register: DeferredRegister<T>) {
        val container = Kilt.loader.getMod(modId) ?: throw IllegalArgumentException("Could not find NeoForge mod by ID $modId!")
        register.register(container.eventBus)
    }
}
