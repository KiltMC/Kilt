package net.minecraftforge.event.level

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.portal.PortalShape
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.common.ToolAction
import net.minecraftforge.common.extensions.IForgeBlockState
import net.minecraftforge.common.util.BlockSnapshot
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event
import java.util.EnumSet

open class BlockEvent(val level: LevelAccessor, val pos: BlockPos, val state: BlockState?) : Event() {
    @Cancelable
    class BreakEvent(level: Level, pos: BlockPos, state: BlockState?, val player: Player) : BlockEvent(level, pos, state) {
        private var exp = if (state == null || !ForgeHooks.isCorrectToolForDrops(state, player))
            0
        else {
            (state as IForgeBlockState).getExpDrop(
                level,
                level.random,
                pos,
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.mainHandItem),
                EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, player.mainHandItem)
            )
        }

        var expToDrop: Int
            get() = if (isCanceled) 0 else exp
            set(value) {
                exp = value
            }
    }

    @Cancelable
    open class EntityPlaceEvent(val blockSnapshot: BlockSnapshot, val placedAgainst: BlockState, val entity: Entity?) : BlockEvent(
        blockSnapshot.level!!, blockSnapshot.pos, if (entity !is Player) blockSnapshot.replacedBlock else blockSnapshot.currentBlock
    ) {
        val placedBlock = if (entity !is Player) blockSnapshot.replacedBlock else blockSnapshot.currentBlock
    }

    @Cancelable
    class EntityMultiPlaceEvent(
        blockSnapshots: List<BlockSnapshot>, placedAgainst: BlockState, entity: Entity?
    ) : EntityPlaceEvent(blockSnapshots[0], placedAgainst, entity) {
        val replacedBlockSnapshots = blockSnapshots
    }

    @Cancelable
    class NeighborNotifyEvent(level: Level, pos: BlockPos, state: BlockState, val notifiedSides: EnumSet<Direction>, val forceRedstoneUpdate: Boolean) : BlockEvent(level, pos, state)

    @HasResult
    class CreateFluidSourceEvent(val level: LevelReader, val pos: BlockPos, val state: BlockState) : Event()

    @Cancelable
    class FluidPlaceBlockEvent(level: LevelAccessor, pos: BlockPos, val liquidPos: BlockPos, state: BlockState) : BlockEvent(level, pos, state) {
        var newState = state
        val originalState: BlockState = level.getBlockState(pos)
    }

    open class CropGrowEvent(level: Level, pos: BlockPos, state: BlockState) : BlockEvent(level, pos, state) {
        @HasResult
        class Pre(level: Level, pos: BlockPos, state: BlockState) : CropGrowEvent(level, pos, state)

        class Post(level: Level, pos: BlockPos, original: BlockState, state: BlockState) : CropGrowEvent(level, pos, state) {
            val originalState = original
        }
    }

    @Cancelable
    class FarmlandTrampleEvent(level: Level, pos: BlockPos, state: BlockState, val fallDistance: Float, val entity: Entity) : BlockEvent(level, pos, state)

    @Cancelable
    class PortalSpawnEvent(level: LevelAccessor, pos: BlockPos, state: BlockState, size: PortalShape) : BlockEvent(level, pos, state) {
        val portalSize = size
    }

    @Cancelable
    class BlockToolModificationEvent(originalState: BlockState, val context: UseOnContext, val toolAction: ToolAction, simulate: Boolean) : BlockEvent(context.level, context.clickedPos, originalState) {
        val player = context.player
        val heldItemStack: ItemStack = context.itemInHand
        val isSimulated = simulate
        var finalState = originalState
    }
}