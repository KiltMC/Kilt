package xyz.bluspring.kilt.compat.transfer.energy

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import team.reborn.energy.api.EnergyStorage
import java.util.*

class FabricEnergyItemStorageCapabilityProvider(val itemStack: ItemStack) : ICapabilityProvider {
    private val capabilityCache = Collections.synchronizedMap(mutableMapOf<EnergyStorage, FabricEnergyStorageCapability>())

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.ENERGY) {
            val fabricStorage = EnergyStorage.ITEM.getProvider(itemStack.item) ?: return LazyOptional.empty()
            val storage = fabricStorage.find(itemStack, ContainerItemContext.withConstant(itemStack)) ?: return LazyOptional.empty()

            // Ignore our own storage
            if (storage is ForgeEnergyStorage)
                return LazyOptional.empty()

            return LazyOptional.of { capabilityCache.computeIfAbsent(storage) { s -> FabricEnergyStorageCapability(s) } }.cast()
        }

        return LazyOptional.empty()
    }
}