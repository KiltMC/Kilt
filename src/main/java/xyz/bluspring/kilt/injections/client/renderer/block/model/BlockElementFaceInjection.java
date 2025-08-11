package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.jetbrains.annotations.Nullable;

public interface BlockElementFaceInjection {
    static BlockElementFace create(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable ExtraFaceData faceData) {
        var face = new BlockElementFace(cullForDirection, tintIndex, texture, uv);
        ((BlockElementFaceInjection) (Object) face).kilt$setFaceData(faceData);
        return face;
    }

    void kilt$setParent(BlockElement parent);
    ExtraFaceData getFaceData();

    void kilt$setFaceData(ExtraFaceData faceData);
}
