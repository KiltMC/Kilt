package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(ModelManager.class)
public interface ModelManagerInjection {
    ModelBakery getModelBakery();
}
