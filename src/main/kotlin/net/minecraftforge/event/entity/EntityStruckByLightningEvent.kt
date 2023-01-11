package net.minecraftforge.event.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LightningBolt
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class EntityStruckByLightningEvent(entity: Entity, val lightning: LightningBolt) : EntityEvent(entity) {
}