package xyz.bluspring.kilt.interop.transfer.fluid

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class FabricFluidItemStorageCapabilityProvider(val stack: ItemStack) : ICapabilityProvider {
    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.FLUID_HANDLER_ITEM) {
            val fabricStorage = FluidStorage.ITEM.getProvider(stack.item) ?: return LazyOptional.empty()
            val storage = fabricStorage.find(stack, null) ?: return LazyOptional.empty()

            if (storage is ForgeFluidStorage)
                return LazyOptional.empty()

            return LazyOptional.of { FabricFluidItemStorageCapability(storage, stack) }.cast()
        }

        return LazyOptional.empty()
    }
}