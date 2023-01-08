package net.minecraftforge.common.capabilities

import net.minecraft.core.Direction
import net.minecraftforge.common.util.LazyOptional

interface ICapabilityProvider {
    fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T>
    fun <T> getCapability(cap: Capability<T>): LazyOptional<T> {
        return getCapability(cap, null)
    }
}