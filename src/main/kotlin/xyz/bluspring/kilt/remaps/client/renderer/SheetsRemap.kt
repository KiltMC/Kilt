package xyz.bluspring.kilt.remaps.client.renderer

import net.minecraft.client.renderer.Sheets
import net.minecraft.client.resources.model.Material
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.WoodType

object SheetsRemap : Sheets() {
    @JvmStatic
    fun addWoodType(woodType: WoodType) {
        Sheets.SIGN_MATERIALS[woodType] = createSignMaterial(woodType)
    }

    private fun createSignMaterial(woodType: WoodType): Material {
        val location = ResourceLocation(woodType.name())
        return Material(Sheets.SIGN_SHEET, ResourceLocation(location.namespace, "entity/signs/${location.path}"))
    }
}