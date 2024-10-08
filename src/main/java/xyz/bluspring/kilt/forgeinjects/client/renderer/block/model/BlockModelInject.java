// TRACKED HASH: 1d8a0b7284d1984f5569698b72ad22e422c65e9a
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.BlockGeometryBakingContext;
import net.minecraftforge.common.util.TransformationHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockModelInjection;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Mixin(BlockModel.class)
public class BlockModelInject implements BlockModelInjection {
    @Shadow @Nullable public ResourceLocation parentLocation;
    @Shadow @Final private List<ItemOverride> overrides;
    @Shadow public String name;
    @Shadow public static Gson GSON;
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

    @Inject(method = "resolveParents", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE))
    private void kilt$resolveCustomParents(Function<ResourceLocation, UnbakedModel> resolver, CallbackInfo ci) {
        if (customData.hasCustomGeometry()) {
            customData.getCustomGeometry().resolveParents(resolver, customData);
        }
    }

    /*public BakedModel bakeVanilla(ModelBakery modelBakery, BlockModel blockModel, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation, boolean bl, RenderTypeGroup renderTypes) {
        return UnbakedGeometryHelper.bakeVanilla((BlockModel) (Object) this, modelBakery, blockModel, function, modelState, resourceLocation);
    }*/

    @Inject(
            method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/resources/model/BakedModel;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void kilt$handleCustomModels(ModelBaker modelBaker, BlockModel ownerModel, Function<Material, TextureAtlasSprite> spriteGetter,
                                   ModelState modelTransform, ResourceLocation modelLocation, boolean guiLight3d, CallbackInfoReturnable<BakedModel> cir) {
        if (customData.hasCustomGeometry()) {
            cir.setReturnValue(customData.getCustomGeometry().bake(
                    customData, modelBaker, spriteGetter, modelTransform, getOverrides(modelBaker, ownerModel, spriteGetter), modelLocation
            ));
        }
    }

    @Override
    public ResourceLocation getParentLocation() {
        return this.parentLocation;
    }

    @Override
    public BlockGeometryBakingContext kilt$getCustomData() {
        return customData;
    }

    @Override
    public ItemOverrides getOverrides(ModelBaker baker, BlockModel blockModel, Function<Material, TextureAtlasSprite> spriteGetter) {
        return this.overrides.isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(baker, blockModel, this.overrides);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}