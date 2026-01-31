package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Map;

public interface BlockElementInjection {
    static BlockElement create(Vector3f from, Vector3f to, Map<Direction, BlockElementFace> faces, @Nullable BlockElementRotation rotation, boolean shade, ExtraFaceData faceData) {
        var element = new BlockElement(from, to, faces, rotation, shade);
        element.setFaceData(faceData);
        element.kilt$setFaces();
        return element;
    }

    default ExtraFaceData getFaceData() {
        throw KiltHelper.createMixinException(BlockElementInjection.class, "getFaceData");
    }

    default void setFaceData(ExtraFaceData faceData) {
        throw KiltHelper.createMixinException(BlockElementInjection.class, "setFaceData");
    }

    default void kilt$setFaces() {
        throw KiltHelper.createMixinException(BlockElementInjection.class, "kilt$setFaces");
    }
}
