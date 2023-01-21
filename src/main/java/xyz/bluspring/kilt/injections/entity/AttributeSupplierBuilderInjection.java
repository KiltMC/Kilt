package xyz.bluspring.kilt.injections.entity;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public interface AttributeSupplierBuilderInjection {
    default void combine(AttributeSupplier.Builder other) {
        throw new IllegalStateException();
    }

    default boolean hasAttribute(Attribute attribute) {
        throw new IllegalStateException();
    }
}
