package xyz.bluspring.kilt.injects.client.resources.model;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.resources.model.UnbakedModelInjection;

import net.minecraft.client.resources.model.UnbakedModel;

@Mixin(UnbakedModel.class)
public interface UnbakedModelInject {
    @Mixin(UnbakedModel.GuiLight.class)
    abstract class GuiLightInject implements UnbakedModelInjection.GuiLightInjection {
        @Shadow @Final private String name;

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
