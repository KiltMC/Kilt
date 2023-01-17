package xyz.bluspring.kilt.remaps.stats

import com.mojang.datafixers.util.Pair
import net.minecraft.world.inventory.RecipeBookType
import xyz.bluspring.kilt.mixin.RecipeBookSettingsAccessor

object RecipeBookSettingsRemap {
    @JvmStatic
    fun addTagsForType(type: RecipeBookType, openTag: String, filteringTag: String) {
        RecipeBookSettingsAccessor.getTagFields()[type] = Pair(openTag, filteringTag)
    }
}