// TRACKED HASH: 84f534b4bbc58f38de24da827b479b0d0083faaf
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.ForgeFaceData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockElementFaceInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockElementInjection;

import java.util.Map;

@Mixin(BlockElement.class)
public class BlockElementInject implements BlockElementInjection {
    @Shadow @Final public Map<Direction, BlockElementFace> faces;
    @Unique private ForgeFaceData faceData = ForgeFaceData.DEFAULT;

    public BlockElementInject(Vector3f from, Vector3f to, Map<Direction, BlockElementFace> faces, @Nullable BlockElementRotation rotation, boolean shade) {}

    @CreateInitializer
    public BlockElementInject(Vector3f from, Vector3f to, Map<Direction, BlockElementFace> faces, @Nullable BlockElementRotation rotation, boolean shade, ForgeFaceData faceData) {
        this(from, to, faces, rotation, shade);
        this.setFaceData(faceData);
        this.kilt$setFaces();
    }

    @Override
    public void kilt$setFaces() {
        this.faces.values().forEach(face -> ((BlockElementFaceInjection) face).kilt$setParent((BlockElement) (Object) this));
    }

    @Override
    public ForgeFaceData getFaceData() {
        return faceData;
    }

    @Override
    public void setFaceData(ForgeFaceData faceData) {
        this.faceData = faceData;
    }
}