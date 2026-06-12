package xyz.bluspring.kilt.compat.transfer.capability

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider
import xyz.bluspring.kilt.compat.transfer.FabricStorageWrapper

class FabricLookupAsSlottedBlockCapabilityProvider<T, C, R, V : TransferVariant<R>>(
    block: Block, lookup: BlockApiLookup<Storage<V>, C>,

    val isSelfStorage: (Storage<V>) -> Boolean,
    val slottedCapability: (SlottedStorage<V>) -> T,
) : IBlockCapabilityProvider<T, C> {
    val provider = lookup.getProvider(block)

    override fun getCapability(level: Level, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, context: C?): T? {
        val storage = this.provider?.find(level, pos, state, blockEntity, context)
            ?: return null

        // Ignore our own storage
        if (this.isSelfStorage.invoke(storage))
            return null

        // NeoForge's transfer API is effectively slot-based, so fallback to a wrapper for Fabric's transfer to be slotted.
        if (storage !is SlottedStorage<V>)
            return this.slottedCapability.invoke(FabricStorageWrapper(storage))

        return this.slottedCapability.invoke(storage)
    }
}
