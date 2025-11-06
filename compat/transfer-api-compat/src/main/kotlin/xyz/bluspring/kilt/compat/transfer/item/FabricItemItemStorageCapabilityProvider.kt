package xyz.bluspring.kilt.compat.transfer.item

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemItemStorages
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class FabricItemItemStorageCapabilityProvider(val stack: ItemStack) : ICapabilityProvider {
    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            val fabricStorage = ItemItemStorages.ITEM.getProvider(stack.item) ?: return LazyOptional.empty()
            val storage = fabricStorage.find(stack, ContainerItemContext.withConstant(stack)) ?: return LazyOptional.empty()

            // Ignore our own storage
            if (storage is ForgeSlottedStorage)
                return LazyOptional.empty()

            // Forge's transfer API is effectively slot-based, so fallback to a wrapper for Fabric's transfer to be slotted.
            if (storage !is SlottedStorage<ItemVariant>)
                return LazyOptional.of { FabricItemStorageCapability(FabricStorageWrapper(storage)) }.cast()

            return LazyOptional.of { FabricItemStorageCapability(storage) }.cast()
        }

        return LazyOptional.empty()
    }
}