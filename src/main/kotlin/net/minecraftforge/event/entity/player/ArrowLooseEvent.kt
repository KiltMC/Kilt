package net.minecraftforge.event.entity.player

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.eventbus.api.Cancelable

@Cancelable
class ArrowLooseEvent(player: Player, val bow: ItemStack, val level: Level, var charge: Int, val hasAmmo: Boolean) : PlayerEvent(player) {
}