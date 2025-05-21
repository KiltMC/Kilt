package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.bawnorton.mixinsquared.TargetHandler;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import net.minecraftforge.common.util.TransformationHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.render.block.model.ItemTransformsTransformTypeInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockModelInjection;
import xyz.bluspring.kilt.injections.client.renderer.block.model.ItemTransformsInjection;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Mixin(value = BlockModel.class, priority = 1050)
public abstract class BlockModelInject implements BlockModelInjection {
    @Shadow @Nullable public ResourceLocation parentLocation;
    @Shadow @Final private List<ItemOverride> overrides;
    @Shadow public String name;
    @Shadow public static Gson GSON;

    @Shadow protected abstract ItemTransform getTransform(ItemTransforms.TransformType type);

    public final BlockGeometryBakingContext customData = new BlockGeometryBakingContext((BlockModel) (Object) this);

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/gson/GsonBuilder;registerTypeAdapter(Ljava/lang/reflect/Type;Ljava/lang/Object;)Lcom/google/gson/GsonBuilder;", ordinal = 0, remap = false), remap = false)
    private static GsonBuilder kilt$useForgeExtendedBlockModelDeserializer(GsonBuilder instance, Type factory, Object o, Operation<GsonBuilder> original) {
        // Keeping the factory here might be a little unsafe as another mixin could possibly change it, but that's likely never going to happen.
        return original.call(instance, factory, new ExtendedBlockModelDeserializer())
            .registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer());
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$storeExtendedBlockModelDeserializer(CallbackInfo ci) {
        ExtendedBlockModelDeserializer.INSTANCE = GSON;
    }

    @Inject(method = "getElements", at = @At("HEAD"), cancellable = true)
    private void kilt$cancelIfContainingCustomGeometry(CallbackInfoReturnable<List<BlockElement>> cir) {
        if (this.customData.hasCustomGeometry())
            cir.setReturnValue(Collections.emptyList());
    }

    @WrapOperation(method = "getMaterials", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockModel;getElements()Ljava/util/List;"))
    private List<BlockElement> kilt$useCustomGeometryMaterialData(BlockModel instance, Operation<List<BlockElement>> original, @Local(ordinal = 1) Set<Material> materialSet, @Local(argsOnly = true) Function<ResourceLocation, UnbakedModel> modelGetter, @Local(argsOnly = true) Set<Pair<String, String>> missingTextureErrors) {
        if (((BlockModelInjection) instance).kilt$getCustomData().hasCustomGeometry()) {
            materialSet.addAll(((BlockModelInjection) instance).kilt$getCustomData().getTextureDependencies(modelGetter, missingTextureErrors));
            return Collections.emptyList();
        }

        return original.call(instance);
    }

    @SuppressWarnings("CancellableInjectionUsage")
    @TargetHandler(mixin = "io.github.fabricators_of_create.porting_lib.mixin.client.BlockModelMixin", name = "port_lib$getModelMaterials", prefix = "handler")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/Set;addAll(Ljava/util/Collection;)Z", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$avoidPortingLibCancel(Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors, CallbackInfoReturnable<Collection<Material>> cir, Set set, BlockModel blockModel, Set<Material> materials, CallbackInfo ci) {
        // Kilt: we're cancelling the cancel here, as it is incredibly mod-incompatible. We're using Kilt's instead.
        ci.cancel();
    }

    public BakedModel bakeVanilla(ModelBakery modelBakery, BlockModel blockModel, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation, boolean bl, RenderTypeGroup renderTypes) {
        return UnbakedGeometryHelper.bakeVanilla((BlockModel) (Object) this, modelBakery, blockModel, function, modelState, resourceLocation);
    }

    @Inject(
        method = "bake(Lnet/minecraft/client/resources/model/ModelBakery;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/resources/model/BakedModel;",
        at = @At("HEAD"),
        cancellable = true
    )
    public void kilt$handleCustomModels(ModelBakery bakery, BlockModel model, Function<Material, TextureAtlasSprite> spriteGetter, ModelState transform, ResourceLocation location, boolean guiLight3d, CallbackInfoReturnable<BakedModel> cir) {
        // Avoid replacing the bake process entirely, unless there are any obvious tells that
        // the model data is from a Forge model
        var customData = ((BlockModelInjection) model).kilt$getCustomData();
        if (customData.getRenderTypeHint() != null || !customData.getRootTransform().isIdentity() || customData.visibilityData.kilt$hasAnyData()) {
            cir.setReturnValue(UnbakedGeometryHelper.bake((BlockModel) (Object) this, bakery, model, spriteGetter, transform, location, guiLight3d));
        }
    }

    @ModifyReturnValue(method = "getTransforms", at = @At("RETURN"))
    private ItemTransforms kilt$addExtraTransforms(ItemTransforms original) {
        var builder = ImmutableMap.<ItemTransforms.TransformType, ItemTransform>builder();

        for (ItemTransforms.TransformType type : ItemTransforms.TransformType.values()) {
            if (((ItemTransformsTransformTypeInjection) (Object) type).isModded()) {
                var transform = this.getTransform(type);
                if (transform != ItemTransform.NO_TRANSFORM) {
                    builder.put(type, transform);
                }
            }
        }

        ((ItemTransformsInjection) original).kilt$setModdedTransforms(builder.build());
        return original;
    }

    public ResourceLocation getParentLocation() {
        return this.parentLocation;
    }

    @Override
    public BlockGeometryBakingContext kilt$getCustomData() {
        return customData;
    }

    public ItemOverrides getOverrides(ModelBakery baker, BlockModel blockModel, Function<Material, TextureAtlasSprite> spriteGetter) {
        return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(baker, blockModel, baker::getModel, this.overrides);
    }

    public String getSerializedName() {
        return this.name;
    }

    @Mixin(BlockModel.Deserializer.class)
    public static class DeserializerInject {
        @Unique private static final ExtendedBlockModelDeserializer EXTENDED_BLOCK_MODEL_DESERIALIZER = new ExtendedBlockModelDeserializer();

        @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;", at = @At("RETURN"))
        private void kilt$attachExtendedForgeData(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<BlockModel> cir) {
            EXTENDED_BLOCK_MODEL_DESERIALIZER.kilt$deserialize(json, type, context, cir.getReturnValue());
        }
    }
}