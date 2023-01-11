package net.minecraftforge.event.entity

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.fml.event.IModBusEvent

class EntityAttributeCreationEvent(map: Map<EntityType<out LivingEntity>, AttributeSupplier>) : Event(), IModBusEvent {
    private val attributeMap = map.toMutableMap()

    fun put(entity: EntityType<out LivingEntity>, map: AttributeSupplier) {
        if (DefaultAttributes.hasSupplier(entity))
            throw IllegalStateException("Duplicate DefaultAttributes entry: $entity")

        attributeMap[entity] = map
    }
}