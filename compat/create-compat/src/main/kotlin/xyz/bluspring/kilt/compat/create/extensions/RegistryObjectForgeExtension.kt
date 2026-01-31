package xyz.bluspring.kilt.compat.create.extensions

import net.neoforged.neoforge.registries.RegisterEvent

interface RegistryObjectForgeExtension {
    fun updateReference(event: RegisterEvent)
}