package xyz.bluspring.kilt.injections.entity;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import xyz.bluspring.kilt.mixin.AttributeSupplierAccessor;
import xyz.bluspring.kilt.mixin.AttributeSupplierBuilderAccessor;

public interface AttributeSupplierBuilderInjection {
    static AttributeSupplier.Builder create(AttributeSupplier attributeMap) {
        var builder = new AttributeSupplier.Builder();
        ((AttributeSupplierBuilderAccessor) builder).getBuilder().putAll(((AttributeSupplierAccessor) attributeMap).getInstances());

        return builder;
    }

    default void combine(AttributeSupplier.Builder other) {
        throw new IllegalStateException();
    }

    default boolean hasAttribute(Attribute attribute) {
        throw new IllegalStateException();
    }
}
