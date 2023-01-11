package net.minecraftforge.event.entity.living

import net.minecraft.world.entity.Mob
import net.minecraftforge.eventbus.api.Event.HasResult

@HasResult
class LivingPackSizeEvent(entity: Mob) : LivingEvent(entity) {
    var maxPackSize = 0
}