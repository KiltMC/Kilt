package xyz.bluspring.kilt.compat.create.registrate

import com.google.common.collect.ImmutableMap
import com.mojang.serialization.MapCodec
import com.tterrag.registrate.fabric.SimpleFlowableFluid
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.Item
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraftforge.fluids.ForgeFlowingFluid
import xyz.bluspring.kilt.compat.create.extensions.SimpleFlowableFluidPropertiesExtension
import xyz.bluspring.kilt.compat.create.mixin.registrate_fabric.FluidAccessor
import xyz.bluspring.kilt.compat.create.mixin.registrate_fabric.ForgeFlowingFluidAccessor
import xyz.bluspring.kilt.compat.create.mixin.registrate_fabric.MappedRegistryAccessor
import java.util.Optional

class SimpleWrappedForgeFlowingFluid(private val wrapped: ForgeFlowingFluid) : SimpleFlowableFluid(
    copyFluidType(
        Properties({ wrapped.source }, { wrapped.flowing })
            .block((wrapped as ForgeFlowingFluidAccessor).block)
            .bucket { wrapped.bucket }
            .flowSpeed(wrapped.slopeFindDistance)
            .levelDecreasePerBlock(wrapped.levelDecreasePerBlock)
            .blastResistance(wrapped.explosionResistance)
            .tickRate(wrapped.tickRate),
        wrapped
    )
) {

    @Suppress("UNCHECKED_CAST")
    private fun <T : Comparable<T>> setValue(
        state: FluidState, property: Property<T>, value: Any
    ) {
        state.setValue(property, value as T)
    }

    init {
        (BuiltInRegistries.FLUID as MappedRegistryAccessor<*>).unregisteredIntrusiveHolders.remove(wrapped)
        val wrappedAccessor = wrapped as FluidAccessor
        @Suppress("DEPRECATION")
        wrappedAccessor.setBuiltInRegistryHolder(this.builtInRegistryHolder())

        val builder: StateDefinition.Builder<Fluid?, FluidState?> = StateDefinition.Builder(this)
        wrappedAccessor.callCreateFluidStateDefinition(builder)
        @Suppress("CAST_NEVER_SUCCEEDS")
        (this as FluidAccessor).setStateDefinition(
            builder.create(
                { obj: Fluid? -> obj!!.defaultFluidState() },
                { owner: Fluid?, values: ImmutableMap<Property<*>?, Comparable<*>?>?, propertiesCodec: MapCodec<FluidState?>? ->
                    FluidState(
                        owner,
                        values,
                        propertiesCodec
                    )
                }
            )
        )

        val state = getStateDefinition().any()
        val wrappedState = wrapped.defaultFluidState()

        for ((key, value) in wrappedState.values) {
            setValue(state, key, value)
        }

        registerDefaultState(state)
    }

    override fun createFluidStateDefinition(builder: StateDefinition.Builder<Fluid?, FluidState?>) {
        // Skip this because we replace it completely above.
    }

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

private fun copyFluidType(properties: SimpleFlowableFluid.Properties, wrapped: ForgeFlowingFluid): SimpleFlowableFluid.Properties {
    properties as SimpleFlowableFluidPropertiesExtension
    properties.`kilt$setFluidType`{wrapped.fluidType}
    return properties
}