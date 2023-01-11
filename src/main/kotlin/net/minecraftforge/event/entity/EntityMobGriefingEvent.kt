package net.minecraftforge.event.entity

import net.minecraft.world.entity.Entity
import net.minecraftforge.eventbus.api.Event.HasResult

@HasResult
class EntityMobGriefingEvent(entity: Entity) : EntityEvent(entity) {
}