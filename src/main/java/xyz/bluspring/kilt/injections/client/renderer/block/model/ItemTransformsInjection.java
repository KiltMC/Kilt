package xyz.bluspring.kilt.injections.client.renderer.block.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;

public interface ItemTransformsInjection {
    ImmutableMap<ItemTransforms.TransformType, ItemTransform> kilt$getModdedTransforms();
    void kilt$setModdedTransforms(ImmutableMap<ItemTransforms.TransformType, ItemTransform> moddedTransforms);
}
