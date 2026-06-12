package xyz.bluspring.kilt.compat.transfer.capability

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider

class FabricLookupAsBlockCapabilityProvider<S, T, C>(
    block: Block, lookup: BlockApiLookup<S, C>,

    val isSelfStorage: (S) -> Boolean,
    val capability: (S) -> T,
) : IBlockCapabilityProvider<T, C> {
    val provider = lookup.getProvider(block)

    override fun getCapability(level: Level, pos: BlockPos, state: BlockState, blockEntity: BlockEntity?, context: C & Any): T? {
        val storage = this.provider?.find(level, pos, state, blockEntity, context)
            ?: return null

        // Ignore our own storage
        if (this.isSelfStorage.invoke(storage))
            return null

        return this.capability.invoke(storage)
    }
}
