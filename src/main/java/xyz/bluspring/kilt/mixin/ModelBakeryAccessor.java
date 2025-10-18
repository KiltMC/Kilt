package xyz.bluspring.kilt.mixin;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
    @Accessor
    Map<ModelResourceLocation, UnbakedModel> getTopLevelModels();
}
