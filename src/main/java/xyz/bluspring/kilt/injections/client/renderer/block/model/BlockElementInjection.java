package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.ForgeFaceData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;

public interface BlockElementInjection {
    static BlockElement create(Vector3f from, Vector3f to, Map<Direction, BlockElementFace> faces, @Nullable BlockElementRotation rotation, boolean shade, ForgeFaceData faceData) {
        var element = new BlockElement(from, to, faces, rotation, shade);
        ((BlockElementInjection) element).setFaceData(faceData);
        ((BlockElementInjection) element).kilt$setFaces();
        return element;
    }

    ForgeFaceData getFaceData();
    void setFaceData(ForgeFaceData faceData);
    void kilt$setFaces();
}
