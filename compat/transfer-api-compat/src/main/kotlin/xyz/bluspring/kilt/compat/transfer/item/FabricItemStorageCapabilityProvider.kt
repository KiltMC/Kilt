package xyz.bluspring.kilt.compat.transfer.item

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider
import net.neoforged.neoforge.items.IItemHandler

class FabricItemStorageCapabilityProvider(block: Block) : IBlockCapabilityProvider<IItemHandler, Direction?> {
    val fabricStorage = ItemStorage.SIDED.getProvider(block)

    override fun getCapability(level: Level, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, context: Direction?): IItemHandler? {
        val storage = fabricStorage?.find(level, pos, state, blockEntity, context)
            ?: return null

        // Ignore our own storage
        if (storage is ForgeSlottedStorage)
            return null

        // Forge's transfer API is effectively slot-based, so fallback to a wrapper for Fabric's transfer to be slotted.
        if (storage !is SlottedStorage<ItemVariant>)
            return FabricItemStorageCapability(FabricStorageWrapper(storage))

        return FabricItemStorageCapability(storage)
    }
}
