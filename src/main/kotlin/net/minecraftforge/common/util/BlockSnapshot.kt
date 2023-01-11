package net.minecraftforge.common.util

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import io.github.fabricators_of_create.porting_lib.util.BlockSnapshot as FabricBlockSnapshot

class BlockSnapshot private constructor (private val fabric: FabricBlockSnapshot) {
    val currentBlock: BlockState
        get() = fabric.currentBlock

    val level: LevelAccessor?
        get() = fabric.level

    val replacedBlock: BlockState
        get() = fabric.replacedBlock

    val blockEntity: BlockEntity?
        get() = fabric.blockEntity

    val pos: BlockPos
        get() = fabric.pos

    val flag: Int
        get() = fabric.flag

    val tag: CompoundTag?
        get() = fabric.tag

    fun restore(force: Boolean): Boolean {
        return fabric.restore(force)
    }

    fun restore(): Boolean {
        return fabric.restore()
    }

    fun restore(force: Boolean, notifyNeighbors: Boolean): Boolean {
        return fabric.restore(force, notifyNeighbors)
    }

    fun restoreToLocation(world: LevelAccessor, pos: BlockPos, force: Boolean, notifyNeighbors: Boolean): Boolean {
        return fabric.restoreToLocation(world, pos, force, notifyNeighbors)
    }

    override fun equals(other: Any?): Boolean {
        return fabric == other
    }

    override fun toString(): String {
        return fabric.toString()
    }

    override fun hashCode(): Int {
        return fabric.hashCode()
    }

    companion object {
        @JvmStatic
        fun create(dim: ResourceKey<Level>, world: LevelAccessor, pos: BlockPos): BlockSnapshot {
            return BlockSnapshot(FabricBlockSnapshot.create(dim, world, pos))
        }

        @JvmStatic
        fun create(dim: ResourceKey<Level>, world: LevelAccessor, pos: BlockPos, flag: Int): BlockSnapshot {
            return BlockSnapshot(FabricBlockSnapshot.create(dim, world, pos, flag))
        }
    }
}