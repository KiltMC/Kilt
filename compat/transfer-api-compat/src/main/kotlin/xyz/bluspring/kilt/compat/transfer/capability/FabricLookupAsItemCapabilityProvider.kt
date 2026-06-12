package xyz.bluspring.kilt.compat.transfer.capability

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.capabilities.ICapabilityProvider

class FabricLookupAsItemCapabilityProvider<S, T, C>(
    item: Item, lookup: ItemApiLookup<S, C>,

    val context: (ItemStack) -> C,
    val isSelfStorage: (S) -> Boolean,
    val capability: (S, ItemStack) -> T,
) : ICapabilityProvider<ItemStack, Void?, T> {
    val provider = lookup.getProvider(item)

    override fun getCapability(stack: ItemStack, context: Void?): T? {
        val storage = this.provider?.find(stack, this.context.invoke(stack))
            ?: return null

        // Ignore our own storage
        if (this.isSelfStorage.invoke(storage))
            return null

        return this.capability.invoke(storage, stack)
    }
}
