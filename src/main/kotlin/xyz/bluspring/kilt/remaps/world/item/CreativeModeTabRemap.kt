package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.CreativeModeTab

abstract class CreativeModeTabRemap : CreativeModeTab {
    constructor(i: Int, str: String) : super(i.apply {
        if (this >= TABS.size) {
            val temp = TABS.copyOf(TABS.size + 1)
            CreativeModeTab.TABS = temp
        }
    }, str)
    constructor(str: String) : this(CreativeModeTab.TABS.size, str)

    companion object {
        @JvmStatic
        @Synchronized
        fun getGroupCountSafe(): Int {
            return CreativeModeTab.TABS.size
        }
    }
}