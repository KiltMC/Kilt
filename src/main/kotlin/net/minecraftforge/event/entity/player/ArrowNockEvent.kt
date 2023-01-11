package net.minecraftforge.event.entity.player

import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class ArrowNockEvent(player: Player, item: ItemStack, val hand: InteractionHand, val level: Level, val hasAmmo: Boolean) : PlayerEvent(player) {
    val bow = item
    var action: InteractionResultHolder<ItemStack>? = null
}