package xyz.bluspring.kilt.compat.create.extensions

import net.minecraft.resources.ResourceKey
import net.minecraftforge.registries.RegisterEvent

interface RegistryObjectForgeExtension<T> {
    fun updateReference(event: RegisterEvent)

    fun getKey(): ResourceKey<T>
}