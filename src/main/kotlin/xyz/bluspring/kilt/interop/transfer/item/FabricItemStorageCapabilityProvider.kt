package xyz.bluspring.kilt.interop.transfer.item

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.minecraft.core.Direction
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.util.LazyOptional

class FabricItemStorageCapabilityProvider(val blockEntity: BlockEntity) : ICapabilityProvider {
    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            val fabricStorage = ItemStorage.SIDED.getProvider(blockEntity.blockState.block) ?: return LazyOptional.empty()
            val storage = fabricStorage.find(blockEntity.level!!, blockEntity.blockPos, blockEntity.blockState, blockEntity, side) ?: return LazyOptional.empty()

            // Forge's transfer API is effectively slot-based, so let's respect that.
            if (storage !is SlottedStorage<ItemVariant> || storage is ForgeSlottedStorage)
                return LazyOptional.empty()

            return LazyOptional.of { FabricItemStorageCapability(storage) }.cast()
        }

        return LazyOptional.empty()
    }
}