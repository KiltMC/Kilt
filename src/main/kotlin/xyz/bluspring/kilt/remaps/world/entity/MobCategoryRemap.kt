package xyz.bluspring.kilt.remaps.world.entity

import net.minecraft.world.entity.MobCategory

object MobCategoryRemap {
    @JvmStatic
    fun byName(name: String): MobCategory {
        // TODO: Make this use extensible enum stuff
        return MobCategory.valueOf(name)
    }
}