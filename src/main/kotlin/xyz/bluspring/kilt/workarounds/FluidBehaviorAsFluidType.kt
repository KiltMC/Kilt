package xyz.bluspring.kilt.workarounds

import net.fabricmc.fabric.api.registry.fluid.FluidBehavior
import net.fabricmc.fabric.impl.content.registry.fluid.SimpleConfiguredFluidBehavior
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.vehicle.boat.AbstractBoat
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.fluids.FluidType
import java.util.*

class FluidBehaviorAsFluidType(val fabricBehaviour: FluidBehavior, val fluidTag: TagKey<Fluid>) : FluidType(tryCreateFromConfigured(fabricBehaviour)) {
    override fun move(entity: LivingEntity, movementVector: Vec3, gravity: Double): Boolean {
        this.fabricBehaviour.travelInFluid(this.fluidTag, entity, movementVector, gravity, movementVector.y <= 0, entity.y)
        return true
    }

    override fun canSwim(entity: Entity): Boolean {
        return this.fabricBehaviour.canSwimInFluid(this.fluidTag, entity)
    }

    override fun canDrownIn(entity: LivingEntity): Boolean {
        return this.fabricBehaviour.canDrownInFluid(this.fluidTag, entity)
    }

    override fun supportsBoating(boat: AbstractBoat): Boolean {
        return this.fabricBehaviour.canSupportBoat(this.fluidTag, boat)
    }

    override fun canPushEntity(entity: Entity): Boolean {
        return entity.isPushedByFluid
    }

    companion object {
        fun tryCreateFromConfigured(behavior: FluidBehavior): Properties {
            val properties = Properties.create()

            if (behavior is SimpleConfiguredFluidBehavior) {
                properties.canSwim(behavior.allowSwimming)
                properties.fallDistanceModifier(behavior.fallDistanceMultiplier)
                properties.canDrown(behavior.drowning)
                properties.supportsBoating(behavior.allowBoats)
            }

            return properties
        }

        private val fluidTypes: MutableMap<TagKey<Fluid>, FluidBehaviorAsFluidType> = Collections.synchronizedMap(mutableMapOf())

        @JvmStatic
        fun computeIfAbsent(behavior: FluidBehavior, tag: TagKey<Fluid>): FluidBehaviorAsFluidType {
            synchronized(this.fluidTypes) {
                return this.fluidTypes.computeIfAbsent(tag) {
                    FluidBehaviorAsFluidType(behavior, it)
                }
            }
        }
    }
}
