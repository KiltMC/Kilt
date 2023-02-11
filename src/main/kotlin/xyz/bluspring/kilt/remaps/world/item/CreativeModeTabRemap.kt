package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.CreativeModeTab

// this isn't actually a remap, but i'm keeping it here to make things easier
object CreativeModeTabRemap {
    @JvmStatic
    fun updateIndex(i: Int): Int {
        var index = i

        if (index == -1)
            index = CreativeModeTab.TABS.size

        if (index >= CreativeModeTab.TABS.size) {
            val temp = CreativeModeTab.TABS.copyOf(CreativeModeTab.TABS.size + 1)
            CreativeModeTab.TABS = temp
        }

        return index
    }
}