package xyz.bluspring.kilt.workarounds

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.FluidTags
import net.minecraft.tags.TagKey
import net.minecraft.world.level.material.Fluid
import net.neoforged.neoforge.common.NeoForgeMod
import net.neoforged.neoforge.fluids.FluidType
import xyz.bluspring.kilt.Kilt

object KiltFluidTags {
    @JvmField val EMPTY: TagKey<Fluid> = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(Kilt.MOD_ID, "empty"))
    @JvmField val EMPTY_NONVANILLA: TagKey<Fluid> = TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(Kilt.MOD_ID, "empty_nonvanilla"))

    @JvmStatic
    fun isTagForFluidTypePushing(tag: TagKey<Fluid>): Boolean {
        return tag == EMPTY || tag == EMPTY_NONVANILLA
    }

    @JvmStatic
    fun getTagForFluidType(fluidType: FluidType): TagKey<Fluid> {
        return when (fluidType) {
            NeoForgeMod.WATER_TYPE.value() -> FluidTags.WATER
            NeoForgeMod.LAVA_TYPE.value() -> FluidTags.LAVA
            else -> EMPTY_NONVANILLA
        }
    }
}
