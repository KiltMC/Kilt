package xyz.bluspring.kilt.injections.client.render.block.model;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.mixin.TransformTypeAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

import javax.annotation.Nullable;

public interface ItemTransformsTransformTypeInjection {
    static ItemTransforms.TransformType create(String keyName, ResourceLocation serializeName) {
        var value = EnumUtils.addEnumToClass(
                ItemTransforms.TransformType.class, TransformTypeAccessor.getValues(),
                keyName, (size) -> TransformTypeAccessor.createTransformType(keyName, size),
                (values) -> TransformTypeAccessor.setValues(values.toArray(new ItemTransforms.TransformType[0]))
        );

        var injected = ((ItemTransformsTransformTypeInjection) (Object) value);
        injected.setSerializeName(serializeName.toString());
        injected.setModded(true);

        return value;
    }

    static ItemTransforms.TransformType create(String keyName, ResourceLocation serializeName, ItemTransforms.TransformType fallback) {
        var value = EnumUtils.addEnumToClass(
                ItemTransforms.TransformType.class, TransformTypeAccessor.getValues(),
                keyName, (size) -> TransformTypeAccessor.createTransformType(keyName, size),
                (values) -> TransformTypeAccessor.setValues(values.toArray(new ItemTransforms.TransformType[0]))
        );

        var injected = ((ItemTransformsTransformTypeInjection) (Object) value);
        injected.setSerializeName(serializeName.toString());
        injected.setModded(true);
        injected.setFallback(fallback);

        return value;
    }

    default String getSerializeName() {
        throw new IllegalStateException();
    }
    @Nullable
    default ItemTransforms.TransformType fallback() {
        throw new IllegalStateException();
    }
    default boolean isModded() {
        throw new IllegalStateException();
    }

    default void setSerializeName(String name) {
        throw new IllegalStateException();
    }

    default void setFallback(ItemTransforms.TransformType fallback) {
        throw new IllegalStateException();
    }

    default void setModded(boolean modded) {
        throw new IllegalStateException();
    }
}
