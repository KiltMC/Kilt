package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.CreativeModeTab

abstract class CreativeModeTabRemap(i: Int, str: String) : CreativeModeTab(i, str) {
    companion object {
        @JvmStatic
        @Synchronized
        fun getGroupCountSafe(): Int {
            return CreativeModeTab.TABS.size
        }
    }
}