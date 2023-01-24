package xyz.bluspring.kilt.remaps.client.gui.components

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component

abstract class AbstractWidgetRemap(i: Int, j: Int, k: Int, l: Int, component: Component) : AbstractWidget(i, j, k, l, component) {
    companion object {
        const val UNSET_FG_COLOR = -1
    }
}