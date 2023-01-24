package xyz.bluspring.kilt.remaps.world.entity.ai.attributes

import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import xyz.bluspring.kilt.mixin.AttributeSupplierAccessor
import xyz.bluspring.kilt.mixin.AttributeSupplierBuilderAccessor

open class AttributeSupplierBuilderRemap() : AttributeSupplier.Builder() {
    constructor(attributeMap: AttributeSupplier) : this() {
        (this as AttributeSupplierBuilderAccessor).builder.putAll((attributeMap as AttributeSupplierAccessor).instances)
    }
}