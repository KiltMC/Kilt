package xyz.bluspring.kilt.injections.client.renderer.block.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.item.ItemDisplayContext;

public interface ItemTransformsInjection {
    ImmutableMap<ItemDisplayContext, ItemTransform> kilt$getModdedTransforms();
    void kilt$setModdedTransforms(ImmutableMap<ItemDisplayContext, ItemTransform> moddedTransforms);
}
