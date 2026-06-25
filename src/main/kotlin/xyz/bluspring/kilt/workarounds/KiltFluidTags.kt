package xyz.bluspring.kilt.workarounds

import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid
import xyz.bluspring.kilt.Kilt

object KiltFluidTags {
    @JvmField val EMPTY: TagKey<Fluid> = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(Kilt.MOD_ID, "empty"))
    @JvmField val EMPTY_NONVANILLA: TagKey<Fluid> = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath(Kilt.MOD_ID, "empty_nonvanilla"))

    @JvmStatic
    fun isTagForFluidTypePushing(tag: TagKey<Fluid>): Boolean {
        return tag == EMPTY || tag == EMPTY_NONVANILLA
    }
}
