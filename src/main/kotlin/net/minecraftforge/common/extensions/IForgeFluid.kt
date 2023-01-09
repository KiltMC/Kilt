package net.minecraftforge.common.extensions

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.vehicle.Boat
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.pathfinder.BlockPathTypes
import net.minecraft.world.phys.Vec3
import net.minecraftforge.fluids.FluidType

interface IForgeFluid {
    private fun self(): Fluid {
        return this as Fluid
    }

    fun getExplosionResistance(state: FluidState, level: BlockGetter, pos: BlockPos, explosion: Explosion): Float {
        return state.explosionResistance
    }

    val fluidType: FluidType

    fun move(state: FluidState, entity: LivingEntity, movementVector: Vec3, gravity: Double): Boolean {
        return fluidType.move(state, entity, movementVector, gravity)
    }

    fun canConvertToSource(state: FluidState, reader: LevelReader, pos: BlockPos): Boolean {
        return fluidType.canConvertToSource(state, reader, pos)
    }

    fun supportsBoating(state: FluidState, boat: Boat): Boolean {
        return fluidType.supportsBoating(state, boat)
    }

    fun getBlockPathType(state: FluidState, level: BlockGetter, pos: BlockPos, mob: Mob?, canFluidLog: Boolean): BlockPathTypes? {
        return fluidType.getBlockPathType(state, level, pos, mob, canFluidLog)
    }

    fun getAdjacentBlockPathType(state: FluidState, level: BlockGetter, pos: BlockPos, mob: Mob?, originalType: BlockPathTypes): BlockPathTypes? {
        return fluidType.getAdjacentBlockPathType(state, level, pos, mob, originalType)
    }

    fun canHydrate(state: FluidState, getter: BlockGetter, pos: BlockPos, source: BlockState, sourcePos: BlockPos): Boolean {
        return fluidType.canHydrate(state, getter, pos, source, sourcePos)
    }

    fun canExtinguish(state: FluidState, getter: BlockGetter, pos: BlockPos): Boolean {
        return fluidType.canExtinguish(state, getter, pos)
    }
}