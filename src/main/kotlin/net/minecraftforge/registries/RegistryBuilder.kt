package net.minecraftforge.registries

import net.minecraft.resources.ResourceLocation

class RegistryBuilder<V> {
    internal lateinit var registryName: ResourceLocation
    internal lateinit var optionalDefaultKey: ResourceLocation
}