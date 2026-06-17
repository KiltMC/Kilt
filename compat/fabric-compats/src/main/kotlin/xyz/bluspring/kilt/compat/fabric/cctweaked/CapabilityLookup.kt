package xyz.bluspring.kilt.compat.fabric.cctweaked

import dan200.computercraft.shared.peripheral.generic.ComponentLookup
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.BlockCapability


/**
 * A [ComponentLookup] for [capabilities][BlockCapability].
 *
 *
 * This is a record to ensure that adding the same capability multiple times only results in one lookup being
 * present in the resulting list.
 *
 * @param capability The capability to lookup
 * @param <T>        The type of the capability we look up.
 */
@JvmRecord
data class CapabilityLookup<T>(val capability: BlockCapability<T, Direction?>) : ComponentLookup {
    override fun find(
        level: ServerLevel,
        pos: BlockPos,
        state: BlockState,
        blockEntity: BlockEntity,
        side: Direction?
    ): T? {
        val cap = level.getCapability<T, Direction?>(capability, pos, state, blockEntity, null)
        return if (cap == null && side != null) level.getCapability<T, Direction?>(
            capability,
            pos,
            state,
            blockEntity,
            side
        ) else cap
    }
}
