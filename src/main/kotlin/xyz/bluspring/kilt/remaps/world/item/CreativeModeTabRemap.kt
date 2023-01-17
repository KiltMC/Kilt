package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.CreativeModeTab

object CreativeModeTabRemap {
    @JvmStatic
    @Synchronized
    fun getGroupCountSafe(): Int {
        return CreativeModeTab.TABS.size
    }
}