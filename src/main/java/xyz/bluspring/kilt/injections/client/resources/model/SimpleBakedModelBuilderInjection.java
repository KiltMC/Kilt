package xyz.bluspring.kilt.injections.client.resources.model;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.RenderTypeGroup;

public interface SimpleBakedModelBuilderInjection {
    default BakedModel build(RenderTypeGroup renderTypeGroup) {
        return build(renderTypeGroup, RenderTypeGroup.EMPTY);
    }

    default BakedModel build(RenderTypeGroup renderTypeGroup, RenderTypeGroup renderTypesFast) {
        throw new IllegalStateException();
    }
}
