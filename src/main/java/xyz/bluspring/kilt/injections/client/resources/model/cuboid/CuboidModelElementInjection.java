package xyz.bluspring.kilt.injections.client.resources.model.cuboid;

import java.util.Map;

import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.core.Direction;

public interface CuboidModelElementInjection {
    default ExtraFaceData faceData() {
        throw KiltHelper.createMixinException(CuboidModelElementInjection.class, "faceData");
    }

    static CuboidModelElement create(Vector3fc from, Vector3fc to, Map<Direction, CuboidFace> faces, @Nullable CuboidRotation rotation, boolean shade, int lightEmission, ExtraFaceData faceData) {
        var element = new CuboidModelElement(from, to, faces, rotation, shade, lightEmission);
        ClientRecordMixinRefMaps.EXTENDED_CUBOID_MODEL_ELEMENT.put(element, new ClientRecordMixinRefMaps.ExtendedCuboidModelElement(faceData));
        return element;
    }

    static CuboidModelElement create(Vector3fc from, Vector3fc to, Map<Direction, CuboidFace> faces, ExtraFaceData faceData) {
        return create(from, to, faces, null, true, 0, faceData);
    }
}
