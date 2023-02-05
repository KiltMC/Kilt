package xyz.bluspring.kilt.remaps.world.item

import net.minecraft.world.item.CreativeModeTab

abstract class CreativeModeTabRemap : CreativeModeTab {
    constructor(i: Int, str: String) : super(i.run {
        var index = this

        if (index == -1)
            index = TABS.size

        if (index >= TABS.size) {
            val temp = TABS.copyOf(TABS.size + 1)
            CreativeModeTab.TABS = temp
        }

        return@run index
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