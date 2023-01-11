package net.minecraftforge.event.entity.item

import net.minecraft.world.entity.item.ItemEntity
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class ItemExpireEvent(entityItem: ItemEntity, var extraLife: Int) : ItemEvent(entityItem) {
}