package xyz.bluspring.kilt.injections.client.resources.model.cuboid;

import com.mojang.math.Quadrant;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

public interface FaceBakeryInjection {
    ThreadLocal<ExtraFaceData> kilt$faceData = ThreadLocal.withInitial(() -> ExtraFaceData.DEFAULT);
    ThreadLocal<int @Nullable []> kilt$colors = new ThreadLocal<>();
    ThreadLocal<int @Nullable []> kilt$normals = new ThreadLocal<>();

    static BakedQuad bakeQuad(final ModelBaker.Interner interner, final Vector3fc from, final Vector3fc to, final CuboidFace.UVs uvs, final Quadrant uvRotation, final BakedQuad.MaterialInfo materialInfo, final Direction facing, final ModelState modelState, final @Nullable CuboidRotation elementRotation, ExtraFaceData faceData) {
        try {
            kilt$faceData.set(faceData);
            return FaceBakery.bakeQuad(interner, from, to, uvs, uvRotation, materialInfo, facing, modelState, elementRotation);
        } finally {
            kilt$faceData.set(ExtraFaceData.DEFAULT);
        }
    }

    static void recalculateWinding(Vector3fc[] positions, long[] uvs, Direction direction, int @Nullable [] colors, int @Nullable [] normals) {
        try {
            kilt$colors.set(colors);
            kilt$normals.set(normals);

            FaceBakery.recalculateWinding(positions, uvs, direction);
        } finally {
            kilt$colors.set(null);
            kilt$normals.set(null);
        }
    }
}
