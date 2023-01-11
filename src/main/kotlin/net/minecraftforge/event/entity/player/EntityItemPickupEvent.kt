package net.minecraftforge.event.entity.player

import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event.HasResult

@Cancelable
@HasResult
class EntityItemPickupEvent(player: Player, val item: ItemEntity) : PlayerEvent(player) {
}