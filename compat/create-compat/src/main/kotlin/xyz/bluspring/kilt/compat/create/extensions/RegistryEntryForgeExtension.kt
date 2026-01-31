package xyz.bluspring.kilt.compat.create.extensions

import net.neoforged.neoforge.registries.RegisterEvent

interface RegistryEntryForgeExtension {
    fun updateReference(event: RegisterEvent)
//    fun <R, E : R> getSibling(registry: IForgeRegistry<R>): RegistryEntry<E>
}