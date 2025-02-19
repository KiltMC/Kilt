package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public interface ModelManagerInjection {
    default BakedModel getModel(ResourceLocation modelLocation) {
        throw new IllegalStateException();
    }
}
