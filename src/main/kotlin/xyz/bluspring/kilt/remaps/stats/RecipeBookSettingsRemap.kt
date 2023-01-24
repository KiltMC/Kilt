package xyz.bluspring.kilt.remaps.stats

import com.mojang.datafixers.util.Pair
import net.minecraft.stats.RecipeBookSettings
import net.minecraft.world.inventory.RecipeBookType
import xyz.bluspring.kilt.mixin.RecipeBookSettingsAccessor

class RecipeBookSettingsRemap : RecipeBookSettings() {
    companion object {
        @JvmStatic
        fun addTagsForType(type: RecipeBookType, openTag: String, filteringTag: String) {
            RecipeBookSettingsAccessor.getTagFields()[type] = Pair(openTag, filteringTag)
        }
    }
}