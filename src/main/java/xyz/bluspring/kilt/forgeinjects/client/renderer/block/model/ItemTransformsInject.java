package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.IExtensibleEnum;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.render.block.model.ItemTransformsTransformTypeInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.model.ItemTransformsInjection;

@Mixin(ItemTransforms.class)
public abstract class ItemTransformsInject implements ItemTransformsInjection {
    @Unique public ImmutableMap<ItemTransforms.TransformType, ItemTransform> moddedTransforms = ImmutableMap.of();

    @Override
    public ImmutableMap<ItemTransforms.TransformType, ItemTransform> kilt$getModdedTransforms() {
        return moddedTransforms;
    }

    @Override
    public void kilt$setModdedTransforms(ImmutableMap<ItemTransforms.TransformType, ItemTransform> moddedTransforms) {
        this.moddedTransforms = moddedTransforms;
    }

    public ItemTransformsInject(ItemTransform thirdPersonLeftHand, ItemTransform thirdPersonRightHand, ItemTransform firstPersonLeftHand, ItemTransform firstPersonRightHand, ItemTransform head, ItemTransform gui, ItemTransform ground, ItemTransform fixed) {}

    @CreateInitializer
    public ItemTransformsInject(ItemTransform thirdPersonLeftHand, ItemTransform thirdPersonRightHand, ItemTransform firstPersonLeftHand, ItemTransform firstPersonRightHand, ItemTransform head, ItemTransform gui, ItemTransform ground, ItemTransform fixed, ImmutableMap<ItemTransforms.TransformType, ItemTransform> moddedTransforms) {
        this(thirdPersonLeftHand, thirdPersonRightHand, firstPersonLeftHand, firstPersonRightHand, head, gui, ground, fixed);
        this.kilt$setModdedTransforms(moddedTransforms);
    }

    @ModifyExpressionValue(method = "getTransform", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/block/model/ItemTransform;NO_TRANSFORM:Lnet/minecraft/client/renderer/block/model/ItemTransform;"))
    private ItemTransform kilt$tryGetModdedTransform(ItemTransform original, @Local(argsOnly = true) ItemTransforms.TransformType type) {
        return moddedTransforms.getOrDefault(type, original);
    }

    @Mixin(ItemTransforms.Deserializer.class)
    public abstract static class DeserializerInject {
        @Shadow protected abstract ItemTransform getTransform(JsonDeserializationContext context, JsonObject json, String name);

        @ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/ItemTransforms;", at = @At("RETURN"))
        private ItemTransforms kilt$addModdedTransforms(ItemTransforms original, @Local JsonObject json, @Local(argsOnly = true) JsonDeserializationContext ctx) {
            var builder = ImmutableMap.<ItemTransforms.TransformType, ItemTransform>builder();

            for (ItemTransforms.TransformType type : ItemTransforms.TransformType.values()) {
                if (((ItemTransformsTransformTypeInjection) (Object) type).isModded()) {
                    var transform = this.getTransform(ctx, json, ((ItemTransformsTransformTypeInjection) (Object) type).getSerializeName());
                    var fallbackType = type;

                    while (transform == ItemTransform.NO_TRANSFORM && ((ItemTransformsTransformTypeInjection) (Object) fallbackType).fallback() != null) {
                        fallbackType = ((ItemTransformsTransformTypeInjection) (Object) fallbackType).fallback();
                        transform = this.getTransform(ctx, json, ((ItemTransformsTransformTypeInjection) (Object) fallbackType).getSerializeName());
                    }

                    if (transform != ItemTransform.NO_TRANSFORM) {
                        builder.put(type, transform);
                    }
                }
            }

            ((ItemTransformsInjection) original).kilt$setModdedTransforms(builder.build());

            return original;
        }
    }

    @Mixin(ItemTransforms.TransformType.class)
    public static class TransformTypeInject implements ItemTransformsTransformTypeInjection, IExtensibleEnum {
        @CreateStatic
        private static ItemTransforms.TransformType create(String keyName, ResourceLocation serializeName) {
            return ItemTransformsTransformTypeInjection.create(keyName, serializeName);
        }

        @CreateStatic
        private static ItemTransforms.TransformType create(String keyName, ResourceLocation serializeName, ItemTransforms.TransformType fallback) {
            return ItemTransformsTransformTypeInjection.create(keyName, serializeName, fallback);
        }

        private String serializeName;
        private boolean isModded = false;
        @javax.annotation.Nullable
        private ItemTransforms.TransformType fallback;

        @Override
        public String getSerializeName() {
            if (serializeName == null) {
                // Make the serialized name later, since we can't do it as an initializer.
                var transformType = ((ItemTransforms.TransformType) (Object) this);

                switch (transformType.name()) {
                    case "THIRD_PERSON_LEFT_HAND" ->
                            serializeName = "thirdperson_lefthand";
                    case "THIRD_PERSON_RIGHT_HAND" ->
                            serializeName = "thirdperson_righthand";
                    case "FIRST_PERSON_LEFT_HAND" ->
                            serializeName = "firstperson_lefthand";
                    case "FIRST_PERSON_RIGHT_HAND" ->
                            serializeName = "firstperson_righthand";
                    default ->
                            serializeName = transformType.name().toLowerCase();
                }
            }

            return serializeName;
        }

        @Nullable
        @Override
        public ItemTransforms.TransformType fallback() {
            return fallback;
        }

        @Override
        public boolean isModded() {
            return isModded;
        }

        @Override
        public void setSerializeName(String name) {
            serializeName = name;
        }

        @Override
        public void setFallback(ItemTransforms.TransformType fallback) {
            this.fallback = fallback;
        }

        @Override
        public void setModded(boolean modded) {
            this.isModded = modded;
        }
    }
}
