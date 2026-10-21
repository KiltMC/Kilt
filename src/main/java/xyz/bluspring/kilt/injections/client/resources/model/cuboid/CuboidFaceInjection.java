package xyz.bluspring.kilt.injections.client.resources.model.cuboid;

import com.mojang.math.Quadrant;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.core.Direction;

public interface CuboidFaceInjection {
    default @Nullable ExtraFaceData faceData() {
        throw KiltHelper.createMixinException(CuboidFaceInjection.class, "faceData");
    }

    default MutableObject<CuboidModelElement> parent() {
        throw KiltHelper.createMixinException(CuboidFaceInjection.class, "parent");
    }

    static CuboidFace create(@Nullable Direction cullForDirection, int tintIndex, String texture, CuboidFace.@Nullable UVs uvs, Quadrant rotation, @Nullable ExtraFaceData faceData, MutableObject<CuboidModelElement> parent) {
        var face = new CuboidFace(cullForDirection, tintIndex, texture, uvs, rotation);
        ClientRecordMixinRefMaps.EXTENDED_CUBOID_FACE.put(face, new ClientRecordMixinRefMaps.ExtendedCuboidFace(faceData, parent));
        return face;
    }
}
