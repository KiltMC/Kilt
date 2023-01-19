package xyz.bluspring.kilt.remaps.tags

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object ItemTagsRemap {
    @JvmStatic
    fun create(name: ResourceLocation): TagKey<Item> {
        return TagKey.create(Registry.ITEM_REGISTRY, name)
    }
}