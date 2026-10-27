package xyz.bluspring.kilt.injects.client.resources.model.cuboid;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.common.util.TransformationHelper;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.resources.model.cuboid.ItemTransformInjection;

import net.minecraft.client.resources.model.cuboid.ItemTransform;

@Mixin(ItemTransform.class)
public abstract class ItemTransformInject implements ItemTransformInjection {
    @Shadow
    public ItemTransformInject(Vector3fc rotation, Vector3fc translation, Vector3fc scale) {}

    @CreateInitializer
    public ItemTransformInject(Vector3fc rotation, Vector3fc translation, Vector3fc scale, Vector3fc rightRotation) {
        this(rotation, translation, scale);
        ClientRecordMixinRefMaps.EXTENDED_ITEM_TRANSFORM.put((ItemTransform) (Object) this, new ClientRecordMixinRefMaps.ExtendedItemTransform(rightRotation));
    }

    @Override
    public Vector3fc rightRotation() {
        var extension = ClientRecordMixinRefMaps.EXTENDED_ITEM_TRANSFORM.get(this);
        if (extension != null)
            return extension.rightRotation();

        return ItemTransform.Deserializer.DEFAULT_ROTATION;
    }

    @Definition(id = "pose", local = @Local(type = PoseStack.Pose.class, name = "pose", argsOnly = true))
    @Definition(id = "translate", method = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;translate(FFF)Lorg/joml/Matrix4f;")
    @Expression("pose.translate(-0.5, -0.5, -0.5)")
    @Inject(method = "apply", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$applyRightRotations(boolean applyLeftHandFix, PoseStack.Pose pose, CallbackInfo ci) {
        var extension = ClientRecordMixinRefMaps.EXTENDED_ITEM_TRANSFORM.get(this);
        if (extension != null && !extension.rightRotation().equals(0, 0, 0)) {
            var rightRotation = extension.rightRotation();
            pose.rotate(TransformationHelper.quatFromXYZ(rightRotation.x(), rightRotation.y() * (applyLeftHandFix ? -1 : 1), rightRotation.z() * (applyLeftHandFix ? -1 : 1), true));
        }
    }

    @Mixin(ItemTransform.Deserializer.class)
    public abstract static class DeserializerInject {
        @Shadow protected abstract Vector3f getVector3f(JsonObject object, String key, Vector3f def);
        @Shadow public static Vector3f DEFAULT_ROTATION;

        @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/resources/model/cuboid/ItemTransform;", at = @At("RETURN"))
        private ItemTransform kilt$handleRightRotation(ItemTransform original, @Local(name = "object") JsonObject object) {
            ClientRecordMixinRefMaps.EXTENDED_ITEM_TRANSFORM.put(original, new ClientRecordMixinRefMaps.ExtendedItemTransform(this.getVector3f(object, "right_rotation", DEFAULT_ROTATION)));
            return original;
        }
    }
}
