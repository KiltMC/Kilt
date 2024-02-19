package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.ForgeFaceData;
import org.jetbrains.annotations.Nullable;

public interface BlockElementFaceInjection {
    static BlockElementFace create(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable ForgeFaceData faceData) {
        var face = new BlockElementFace(cullForDirection, tintIndex, texture, uv);
        ((BlockElementFaceInjection) face).kilt$setFaceData(faceData);
        return face;
    }

    void kilt$setParent(BlockElement parent);
    ForgeFaceData getFaceData();

    void kilt$setFaceData(ForgeFaceData faceData);
}
