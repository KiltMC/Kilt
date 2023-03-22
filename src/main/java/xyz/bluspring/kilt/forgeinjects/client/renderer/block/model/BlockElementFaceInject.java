package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElementFace;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.render.block.model.BlockElementFaceInjection;

@Mixin(BlockElementFace.class)
public class BlockElementFaceInject implements BlockElementFaceInjection {
    public boolean hasAmbientOcclusion;

    @Override
    public boolean hasAmbientOcclusion() {
        return hasAmbientOcclusion;
    }

    @Override
    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
    }
}
