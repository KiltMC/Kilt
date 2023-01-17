package xyz.bluspring.kilt.remaps.tags

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

object BlockTagsRemap {
    @JvmStatic
    fun create(name: ResourceLocation): TagKey<Block> {
        return TagKey.create(Registry.BLOCK_REGISTRY, name)
    }
}