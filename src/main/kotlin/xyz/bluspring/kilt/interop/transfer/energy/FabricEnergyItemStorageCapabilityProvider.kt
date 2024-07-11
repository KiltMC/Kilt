package xyz.bluspring.kilt.interop.transfer.energy

import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional
import team.reborn.energy.api.EnergyStorage

class FabricEnergyItemStorageCapabilityProvider(val itemStack: ItemStack) : ICapabilityProvider {
    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.ENERGY) {
            val fabricStorage = EnergyStorage.ITEM.getProvider(itemStack.item) ?: return LazyOptional.empty()
            val storage = fabricStorage.find(itemStack, null) ?: return LazyOptional.empty()

            // Ignore our own storage
            if (storage is ForgeEnergyStorage)
                return LazyOptional.empty()

            return LazyOptional.of { FabricEnergyStorageCapability(storage) }.cast()
        }

        return LazyOptional.empty()
    }
}