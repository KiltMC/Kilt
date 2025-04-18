package xyz.bluspring.kilt.compat.create.extensions

import net.minecraftforge.registries.RegisterEvent

interface RegistryObjectForgeExtension {
    fun updateReference(event: RegisterEvent)
}