package net.minecraftforge.event.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class EntityMountEvent(val entityMounting: Entity, val entityBeingMounted: Entity, val level: Level, val isMounting: Boolean) : EntityEvent(entityMounting) {
    val isDismounting = !isMounting
}