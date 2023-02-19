package xyz.bluspring.kilt.workarounds

import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.builders.LayerDefinition
import java.util.function.Supplier

object LayerDefinitionsWorkaround {
    @JvmField
    val layerDefinitions = mutableMapOf<ModelLayerLocation, Supplier<LayerDefinition>>()
}