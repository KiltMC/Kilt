package net.minecraftforge.event.level

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.piston.PistonStructureResolver
import net.minecraftforge.eventbus.api.Cancelable

abstract class PistonEvent(world: Level, pos: BlockPos, val direction: Direction, moveType: PistonMoveType) : BlockEvent(world, pos, world.getBlockState(pos)) {
    val pistonMoveType = moveType
    val faceOffsetPos = pos.relative(direction)
    val structureHelper: PistonStructureResolver?
        get() {
            return if (level is Level)
                PistonStructureResolver(level, pos, direction, pistonMoveType.isExtend)
            else null
        }

    class Post(world: Level, pos: BlockPos, direction: Direction, moveType: PistonMoveType) : PistonEvent(world, pos, direction, moveType)

    @Cancelable
    class Pre(world: Level, pos: BlockPos, direction: Direction, moveType: PistonMoveType) : PistonEvent(world, pos, direction, moveType)

    enum class PistonMoveType(@JvmField val isExtend: Boolean) {
        EXTEND(true), RETRACT(false)
    }
}