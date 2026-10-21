package xyz.bluspring.kilt.injects.client.resources.model.cuboid;

import com.mojang.math.Quadrant;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.injections.client.resources.model.cuboid.CuboidFaceInjection;

import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.core.Direction;

@Mixin(CuboidFace.class)
public abstract class CuboidFaceInject implements CuboidFaceInjection {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initDefaultFaceData(Direction cullForDirection, int tintIndex, String texture, CuboidFace.UVs uvs, Quadrant rotation, CallbackInfo ci) {
        ClientRecordMixinRefMaps.EXTENDED_CUBOID_FACE.put((CuboidFace) (Object) this, new ClientRecordMixinRefMaps.ExtendedCuboidFace(null, new MutableObject<>()));
    }

    @Override
    public @Nullable ExtraFaceData faceData() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_CUBOID_FACE.get(this);
        if (extension != null && extension.faceData() != null) {
            return extension.faceData();
        } else if (extension != null && extension.parent().get() != null) {
            return extension.parent().get().faceData();
        } else {
            return ExtraFaceData.DEFAULT;
        }
    }

    @Override
    public MutableObject<CuboidModelElement> parent() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_CUBOID_FACE.get(this);
        if (extension != null) {
            return extension.parent();
        }

        return new MutableObject<>();
    }
}
