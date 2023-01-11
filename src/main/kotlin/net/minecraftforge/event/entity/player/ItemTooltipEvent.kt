package net.minecraftforge.event.entity.player

import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

class ItemTooltipEvent(val itemStack: ItemStack, player: Player?, list: List<Component>, val flags: TooltipFlag) : PlayerEvent(player) {
    val toolTip = list
}