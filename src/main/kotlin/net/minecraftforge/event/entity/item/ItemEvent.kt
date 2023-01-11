package net.minecraftforge.event.entity.item

import net.minecraft.world.entity.item.ItemEntity
import net.minecraftforge.event.entity.EntityEvent

open class ItemEvent(itemEntity: ItemEntity) : EntityEvent(itemEntity) {
    override val entity: ItemEntity = itemEntity
}