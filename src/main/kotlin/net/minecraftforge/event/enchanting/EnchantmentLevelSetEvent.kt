package net.minecraftforge.event.enchanting

import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.eventbus.api.Event

class EnchantmentLevelSetEvent(val level: Level, val pos: BlockPos, val enchantRow: Int, val power: Int, itemStack: ItemStack, var enchantLevel: Int) : Event() {
    val item = itemStack
}