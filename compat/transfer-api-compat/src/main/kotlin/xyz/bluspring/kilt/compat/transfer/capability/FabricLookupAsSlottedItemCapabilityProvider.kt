package xyz.bluspring.kilt.compat.transfer.capability

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.capabilities.ICapabilityProvider
import xyz.bluspring.kilt.compat.transfer.FabricStorageWrapper

class FabricLookupAsSlottedItemCapabilityProvider<T, C, R, V : TransferVariant<R>>(
    item: Item, lookup: ItemApiLookup<Storage<V>, C>,

    val context: (ItemStack) -> C,
    val isSelfStorage: (Storage<V>) -> Boolean,
    val slottedCapability: (SlottedStorage<V>, ItemStack) -> T,
) : ICapabilityProvider<ItemStack, Void?, T> {
    val provider = lookup.getProvider(item)

    override fun getCapability(stack: ItemStack, context: Void?): T? {
        val storage = this.provider?.find(stack, this.context.invoke(stack))
            ?: return null

        // Ignore our own storage
        if (this.isSelfStorage.invoke(storage))
            return null

        // NeoForge's transfer API is effectively slot-based, so fallback to a wrapper for Fabric's transfer to be slotted.
        if (storage !is SlottedStorage<V>)
            return this.slottedCapability.invoke(FabricStorageWrapper(storage), stack)

        return this.slottedCapability.invoke(storage, stack)
    }
}
