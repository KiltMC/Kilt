package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;

public interface ModelManagerInjection {
    ModelBakery getModelBakery();
    BakedModel getModel(ResourceLocation modelLocation);
}
