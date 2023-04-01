package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.render.model.BakedQuadInjection;

@Mixin(BakedQuad.class)
public class BakedQuadInject implements BakedQuadInjection {
    private boolean hasAmbientOcclusion;

    @Override
    public void kilt$setAmbientOcclusion(boolean hasAo) {
        hasAmbientOcclusion = hasAo;
    }

    @Override
    public boolean hasAmbientOcclusion() {
        return hasAmbientOcclusion;
    }
}
