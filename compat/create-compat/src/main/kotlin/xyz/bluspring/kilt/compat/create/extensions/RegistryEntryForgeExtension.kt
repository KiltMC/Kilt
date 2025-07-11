package xyz.bluspring.kilt.compat.create.extensions

import com.tterrag.registrate.util.entry.RegistryEntry
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.RegisterEvent

interface RegistryEntryForgeExtension {
    fun updateReference(event: RegisterEvent)
    fun <R, E : R> getSibling(registry: IForgeRegistry<R>): RegistryEntry<E>
}