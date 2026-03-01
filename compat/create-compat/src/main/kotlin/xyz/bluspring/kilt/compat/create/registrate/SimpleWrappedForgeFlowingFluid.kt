package xyz.bluspring.kilt.compat.create.registrate

import com.tterrag.registrate.fabric.SimpleFlowableFluid
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraftforge.fluids.ForgeFlowingFluid
import xyz.bluspring.kilt.compat.create.mixin.registrate_fabric.ForgeFlowingFluidAccessor
import java.util.Optional

class SimpleWrappedForgeFlowingFluid(private val wrapped: ForgeFlowingFluid) : SimpleFlowableFluid(
    Properties({ wrapped.source }, { wrapped.flowing })
        .block((wrapped as ForgeFlowingFluidAccessor).block)
        .bucket { wrapped.bucket }
        .flowSpeed(wrapped.slopeFindDistance)
        .levelDecreasePerBlock(wrapped.levelDecreasePerBlock)
        .blastResistance(wrapped.explosionResistance)
        .tickRate(wrapped.tickRate)
) {
    override fun getPickupSound(): Optional<SoundEvent> {
        return wrapped.pickupSound
    }

    override fun getAmount(state: FluidState): Int {
        return wrapped.getAmount(state)
    }

    override fun isSource(state: FluidState): Boolean {
        return wrapped.isSource(state)
    }

    override fun canConvertToSource(level: Level): Boolean {
        return (wrapped as ForgeFlowingFluidAccessor).callCanConvertToSource(level)
    }

    override fun beforeDestroyingBlock(world: LevelAccessor, pos: BlockPos, state: BlockState) {
        (wrapped as ForgeFlowingFluidAccessor).callBeforeDestroyingBlock(world, pos, state)
    }

    override fun getSlopeFindDistance(world: LevelReader): Int {
        return (wrapped as ForgeFlowingFluidAccessor).callGetSlopeFindDistance(world)
    }

    override fun getDropOff(worldIn: LevelReader): Int {
        return (wrapped as ForgeFlowingFluidAccessor).callGetDropOff(worldIn)
    }

    override fun getBucket(): Item? {
        return wrapped.bucket
    }

    override fun canBeReplacedWith(state: FluidState, world: BlockGetter, pos: BlockPos, fluid: Fluid, direction: Direction): Boolean {
        return (wrapped as ForgeFlowingFluidAccessor).callCanBeReplacedWith(state, world, pos, fluid, direction)
    }

    override fun getTickDelay(world: LevelReader): Int {
        return wrapped.getTickDelay(world)
    }

    override fun getExplosionResistance(): Float {
        return (wrapped as ForgeFlowingFluidAccessor).callGetExplosionResistance()
    }

    override fun createLegacyBlock(state: FluidState): BlockState? {
        return (wrapped as ForgeFlowingFluidAccessor).callCreateLegacyBlock(state)
    }

    override fun isSame(fluid: Fluid): Boolean {
        return wrapped.isSame(fluid)
    }
}