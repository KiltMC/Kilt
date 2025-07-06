package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public interface ModelResourceLocationInjection {
    static String STANDALONE_VARIANT = "standalone";

    static ModelResourceLocation standalone(ResourceLocation id) {
        return new ModelResourceLocation(id, STANDALONE_VARIANT);
    }
}
