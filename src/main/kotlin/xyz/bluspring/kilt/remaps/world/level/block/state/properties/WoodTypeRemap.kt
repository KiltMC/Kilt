package xyz.bluspring.kilt.remaps.world.level.block.state.properties

import net.minecraft.world.level.block.state.properties.WoodType
import xyz.bluspring.kilt.mixin.WoodTypeAccessor

open class WoodTypeRemap(string: String) : WoodType(string) {
    companion object {
        @JvmStatic
        fun create(name: String): WoodType {
            return WoodTypeAccessor.createWoodType(name)
        }
    }
}