package xyz.bluspring.kilt.injections.client.render.block.model;

import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface BlockElementFaceInjection {
    static BlockElementFace create(@Nullable Direction direction, int i, String string, BlockFaceUV blockFaceUV, int emissivity, boolean hasAmbientOcclusion) {
        var face = new BlockElementFace(direction, i, string, blockFaceUV);
        face.setEmissivity(emissivity);
        ((BlockElementFaceInjection) face).setHasAmbientOcclusion(hasAmbientOcclusion);

        return face;
    }

    default void setHasAmbientOcclusion(boolean value) {
        throw new IllegalStateException();
    }

    default boolean hasAmbientOcclusion() {
        throw new IllegalStateException();
    }
}
