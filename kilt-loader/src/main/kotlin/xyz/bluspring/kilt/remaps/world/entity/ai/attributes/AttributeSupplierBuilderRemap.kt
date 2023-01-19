package xyz.bluspring.kilt.remaps.world.entity.ai.attributes

import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import xyz.bluspring.kilt.mixin.AttributeSupplierAccessor
import xyz.bluspring.kilt.mixin.AttributeSupplierBuilderAccessor

class AttributeSupplierBuilderRemap() : AttributeSupplier.Builder() {
    private val others = mutableListOf<AttributeSupplier.Builder>()

    constructor(attributeMap: AttributeSupplier) : this() {
        (this as AttributeSupplierBuilderAccessor).builder.putAll((attributeMap as AttributeSupplierAccessor).instances)
    }

    fun combine(other: AttributeSupplier.Builder) {
        (this as AttributeSupplierBuilderAccessor).builder.putAll((other as AttributeSupplierBuilderAccessor).builder)
        others.add(other)
    }

    fun hasAttribute(attribute: Attribute): Boolean {
        return (this as AttributeSupplierBuilderAccessor).builder.contains(attribute)
    }

    override fun build(): AttributeSupplier {
        (this as AttributeSupplierBuilderAccessor).isInstanceFrozen = true
        others.forEach {
            (it as AttributeSupplierBuilderAccessor).isInstanceFrozen = true
        }

        return AttributeSupplier((this as AttributeSupplierBuilderAccessor).builder)
    }
}