package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.model.ItemTransformsInjection;
import xyz.bluspring.kilt.injections.world.item.ItemDisplayContextInjection;

import java.lang.reflect.Type;

@Mixin(ItemTransforms.class)
public class ItemTransformsInject implements ItemTransformsInjection {
    public ImmutableMap<ItemDisplayContext, ItemTransform> moddedTransforms = ImmutableMap.of();

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/block/model/ItemTransforms;)V", at = @At("TAIL"))
    private void kilt$setModdedTransformsInInit(ItemTransforms transforms, CallbackInfo ci) {
        this.moddedTransforms = ((ItemTransformsInjection) transforms).kilt$getModdedTransforms();
    }

    public ItemTransformsInject(ItemTransform thirdPersonLeftHand, ItemTransform thirdPersonRightHand, ItemTransform firstPersonLeftHand, ItemTransform firstPersonRightHand, ItemTransform head, ItemTransform gui, ItemTransform ground, ItemTransform fixed) {}

    @CreateInitializer
    public ItemTransformsInject(ItemTransform thirdPersonLeftHand, ItemTransform thirdPersonRightHand, ItemTransform firstPersonLeftHand, ItemTransform firstPersonRightHand, ItemTransform head, ItemTransform gui, ItemTransform ground, ItemTransform fixed, ImmutableMap<ItemDisplayContext, ItemTransform> moddedTransforms) {
        this(thirdPersonLeftHand, thirdPersonRightHand, firstPersonLeftHand, firstPersonRightHand, head, gui, ground, fixed);
        this.moddedTransforms = moddedTransforms;
    }

    @Override
    public ImmutableMap<ItemDisplayContext, ItemTransform> kilt$getModdedTransforms() {
        return this.moddedTransforms;
    }

    @Override
    public void kilt$setModdedTransforms(ImmutableMap<ItemDisplayContext, ItemTransform> moddedTransforms) {
        this.moddedTransforms = moddedTransforms;
    }

    @Mixin(ItemTransforms.Deserializer.class)
    public static abstract class DeserializerInject {
        @Shadow protected abstract ItemTransform getTransform(JsonDeserializationContext deserializationContext, JsonObject json, ItemDisplayContext displayContext);

        @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/ItemTransforms;", at = @At("RETURN"))
        private void kilt$addModdedTransforms(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<ItemTransforms> cir) {
            var builder = ImmutableMap.<ItemDisplayContext, ItemTransform>builder();
            var obj = json.getAsJsonObject();

            for (ItemDisplayContext value : ItemDisplayContext.values()) {
                if (((ItemDisplayContextInjection) (Object) value).isModded()) {
                    var transform = this.getTransform(context, obj, value);
                    var fallbackType = value;

                    while (transform == ItemTransform.NO_TRANSFORM && ((ItemDisplayContextInjection) (Object) fallbackType).fallback() != null) {
                        fallbackType = ((ItemDisplayContextInjection) (Object) fallbackType).fallback();
                        transform = this.getTransform(context, obj, fallbackType);
                    }

                    if (transform != ItemTransform.NO_TRANSFORM) {
                        builder.put(value, transform);
                    }
                }
            }

            var transforms = cir.getReturnValue();
            ((ItemTransformsInjection) transforms).kilt$setModdedTransforms(builder.build());
        }
    }
}
