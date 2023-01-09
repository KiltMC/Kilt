package net.minecraftforge.common.extensions

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.vehicle.Boat
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.pathfinder.BlockPathTypes
import net.minecraft.world.phys.Vec3
import net.minecraftforge.fluids.FluidType

interface IForgeFluidState {
    private fun self(): FluidState {
        // "This cast can never succeed" NOT WITH THAT ATTITUDE!
        return this as FluidState
    }

    private fun type(): IForgeFluid {
        return self().type as IForgeFluid
    }

    fun getExplosionResistance(level: BlockGetter, pos: BlockPos, explosion: Explosion): Float {
        return type().getExplosionResistance(self(), level, pos, explosion)
    }

    val fluidType: FluidType
        get() = type().fluidType

    fun move(entity: LivingEntity, movementVector: Vec3, gravity: Double): Boolean {
        return type().move(self(), entity, movementVector, gravity)
    }

    fun canConvertToSource(reader: LevelReader, pos: BlockPos): Boolean {
        return type().canConvertToSource(self(), reader, pos)
    }

    fun supportsBoating(boat: Boat): Boolean {
        return type().supportsBoating(self(), boat)
    }

    fun getBlockPathType(level: BlockGetter, pos: BlockPos, mob: Mob?, canFluidLog: Boolean): BlockPathTypes? {
        return type().getBlockPathType(self(), level, pos, mob, canFluidLog)
    }

    fun getAdjacentBlockPathType(level: BlockGetter, pos: BlockPos, mob: Mob?, originalType: BlockPathTypes): BlockPathTypes? {
        return type().getAdjacentBlockPathType(self(), level, pos, mob, originalType)
    }

    fun canHydrate(getter: BlockGetter, pos: BlockPos, source: BlockState, sourcePos: BlockPos): Boolean {
        return type().canHydrate(self(), getter, pos, source, sourcePos)
    }

    fun canExtinguish(getter: BlockGetter, pos: BlockPos): Boolean {
        return type().canExtinguish(self(), getter, pos)
    }
}