package xyz.bluspring.kilt.remaps.world.level.block.state.properties

import net.minecraft.world.level.block.state.properties.WoodType
import xyz.bluspring.kilt.mixin.WoodTypeAccessor

object WoodTypeRemap {
    @JvmStatic
    fun create(name: String): WoodType {
        return WoodTypeAccessor.createWoodType(name)
    }
}