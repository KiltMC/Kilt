package xyz.bluspring.kilt.interop.transfer.item

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.minecraft.core.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class FabricItemStorageCapabilityProvider(val storage: SlottedStorage<ItemVariant>) : ICapabilityProvider {
    val itemHandler = LazyOptional.of { FabricItemStorageCapability(storage) }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast()
        }

        return LazyOptional.empty()
    }
}