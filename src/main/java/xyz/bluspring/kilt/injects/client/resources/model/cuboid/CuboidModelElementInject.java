package xyz.bluspring.kilt.injects.client.resources.model.cuboid;

import java.util.Map;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.resources.model.cuboid.CuboidModelElementInjection;

import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.CuboidRotation;
import net.minecraft.core.Direction;

@Mixin(CuboidModelElement.class)
public abstract class CuboidModelElementInject implements CuboidModelElementInjection {
    @Override
    public ExtraFaceData faceData() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_CUBOID_MODEL_ELEMENT.get(this);
        if (extension != null) {
            return extension.faceData();
        }

        return ExtraFaceData.DEFAULT;
    }

    @Inject(method = "<init>(Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Ljava/util/Map;Lnet/minecraft/client/resources/model/cuboid/CuboidRotation;ZI)V", at = @At("TAIL"))
    private void kilt$setFacesParent(Vector3fc from, Vector3fc _to, Map<Direction, CuboidFace> faces, CuboidRotation rotation, boolean shade, int lightEmission, CallbackInfo ci) {
        faces.values().forEach(face -> face.parent().setValue((CuboidModelElement) (Object) this));
    }

    public CuboidModelElementInject(Vector3fc from, Vector3fc to, Map<Direction, CuboidFace> faces, @Nullable CuboidRotation rotation, boolean shade, int lightEmission) {}

    @CreateInitializer
    public CuboidModelElementInject(Vector3fc from, Vector3fc to, Map<Direction, CuboidFace> faces, @Nullable CuboidRotation rotation, boolean shade, int lightEmission, ExtraFaceData faceData) {
        this(from, to, faces, rotation, shade, lightEmission);
        ClientRecordMixinRefMaps.EXTENDED_CUBOID_MODEL_ELEMENT.put((CuboidModelElement) (Object) this, new ClientRecordMixinRefMaps.ExtendedCuboidModelElement(faceData));
    }

    public CuboidModelElementInject(Vector3fc from, Vector3fc to, Map<Direction, CuboidFace> faces, ExtraFaceData faceData) {
        this(from, to, faces, null, true, 0, faceData);
    }

    @Mixin(CuboidModelElement.Deserializer.class)
    public abstract static class DeserializerInject {
        @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/resources/model/cuboid/CuboidModelElement;", at = @At("RETURN"))
        private CuboidModelElement kilt$attachFaceData(CuboidModelElement original, @Local(name = "object") JsonObject object) {
            var faceData = ExtraFaceData.read(object.get("neoforge_data"), ExtraFaceData.DEFAULT);
            ClientRecordMixinRefMaps.EXTENDED_CUBOID_MODEL_ELEMENT.put(original, new ClientRecordMixinRefMaps.ExtendedCuboidModelElement(faceData));
            return original;
        }
    }
}
