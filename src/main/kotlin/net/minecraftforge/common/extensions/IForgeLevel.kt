package net.minecraftforge.common.extensions

import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.entity.PartEntity

interface IForgeLevel : ICapabilityProvider {
    val maxEntityRadius: Double
    fun increaseMaxEntityRadius(value: Double): Double

    val partEntities: Collection<PartEntity<*>>
        get() = mutableListOf()
}