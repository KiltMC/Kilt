package xyz.bluspring.kilt.compat.create.extensions

import net.minecraftforge.registries.RegisterEvent

interface RegistryEntryForgeExtension {
    fun updateReference(event: RegisterEvent)
}