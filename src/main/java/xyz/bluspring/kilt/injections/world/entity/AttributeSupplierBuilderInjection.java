package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import xyz.bluspring.kilt.mixin.AttributeSupplierAccessor;
import xyz.bluspring.kilt.mixin.AttributeSupplierBuilderAccessor;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(AttributeSupplier.Builder.class)
public interface AttributeSupplierBuilderInjection {
    static AttributeSupplier.Builder create(AttributeSupplier attributeMap) {
        var builder = new AttributeSupplier.Builder();
        ((AttributeSupplierBuilderAccessor) builder).getBuilder().putAll(((AttributeSupplierAccessor) attributeMap).getInstances());

        return builder;
    }

    default void combine(AttributeSupplier.Builder other) {
        throw KiltHelper.createMixinException(AttributeSupplierBuilderInjection.class, "combine");
    }

    default boolean hasAttribute(Attribute attribute) {
        throw KiltHelper.createMixinException(AttributeSupplierBuilderInjection.class, "hasAttribute");
    }
}
