package xyz.bluspring.kilt.injects.client.resources.model;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.resources.model.ModelResourceLocationInjection;

@Mixin(ModelResourceLocation.class)
public abstract class ModelResourceLocationInject {
    @CreateStatic
    private static final String STANDALONE_VARIANT = ModelResourceLocationInjection.STANDALONE_VARIANT;

    @CreateStatic
    private static ModelResourceLocation standalone(ResourceLocation id) {
        return ModelResourceLocationInjection.standalone(id);
    }
}
