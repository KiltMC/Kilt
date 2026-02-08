package xyz.bluspring.kilt.injections.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

public interface BlockElementFaceInjection {
    static BlockElementFace create(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable ExtraFaceData faceData) {
        return create(cullForDirection, tintIndex, texture, uv, faceData, new MutableObject<>());
    }

    static BlockElementFace create(@Nullable Direction cullForDirection, int tintIndex, String texture, BlockFaceUV uv, @Nullable ExtraFaceData faceData, MutableObject<BlockElement> parent) {
        var face = new BlockElementFace(cullForDirection, tintIndex, texture, uv);
        face.kilt$setFaceData(faceData);
        face.kilt$setParent(parent);
        return face;
    }

    default MutableObject<BlockElement> parent() {
        throw KiltHelper.createMixinException(BlockElementFaceInjection.class, "parent");
    }

    default void kilt$setParent(MutableObject<BlockElement> parent) {
        throw KiltHelper.createMixinException(BlockElementFaceInjection.class, "kilt$setParent");
    }

    default ExtraFaceData faceData() {
        throw KiltHelper.createMixinException(BlockElementFaceInjection.class, "faceData");
    }

    default void kilt$setFaceData(ExtraFaceData faceData) {
        throw KiltHelper.createMixinException(BlockElementFaceInjection.class, "kilt$setFaceData");
    }
}
