// TRACKED HASH: 9e81b9914e73796e61fbdf90eb1c997d3da3bf81
package xyz.bluspring.kilt.injects.client.resources.model;

import com.google.common.collect.Sets;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.ClientHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.resources.model.ModelBakeryInjection;
import xyz.bluspring.kilt.mixin.ModelBakeryAccessor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryInject implements ModelBakeryInjection {
    @Shadow public abstract UnbakedModel getModel(ResourceLocation modelLocation);
    @Shadow protected abstract void registerModelAndLoadDependencies(ModelResourceLocation modelResourceLocation, UnbakedModel unbakedModel);

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadSpecialItemModelAndDependencies(Lnet/minecraft/client/resources/model/ModelResourceLocation;)V", ordinal = 1, shift = At.Shift.AFTER))
    private void kilt$registerForgeModels(BlockColors blockColors, ProfilerFiller profilerFiller, Map<ResourceLocation, BlockModel> modelResources, Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStateResources, CallbackInfo ci) {
        var additionalModels = Sets.<ModelResourceLocation>newHashSet();
        ClientHooks.onRegisterAdditionalModels(additionalModels);

        for (ModelResourceLocation loc : additionalModels) {
            var unbaked = this.getModel(loc.id());
            this.registerModelAndLoadDependencies(loc, unbaked);
        }
    }

    @Mixin(targets = "net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl")
    public static abstract class ModelBakerImplInject implements ModelBaker, ModelBakerImplInjection {
        @Shadow @Final @Mutable private Function<Material, TextureAtlasSprite> modelTextureGetter;
        @Shadow public abstract BakedModel bake(ResourceLocation location, ModelState transform);
        @Shadow @Final private ModelBakery field_40571;
        @Shadow @Nullable abstract BakedModel bakeUncached(UnbakedModel unbakedModel, ModelState modelState);

        @Override
        public @Nullable UnbakedModel getTopLevelModel(ModelResourceLocation location) {
            return ((ModelBakeryAccessor) field_40571).getTopLevelModels().get(location);
        }

        @Override
        public Function<Material, TextureAtlasSprite> getModelTextureGetter() {
            return this.modelTextureGetter;
        }

        @Override
        public BakedModel bake(ResourceLocation loc, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
            var original = this.modelTextureGetter;
            this.modelTextureGetter = sprites;
            var result = this.bake(loc, state);
            this.modelTextureGetter = original;

            return result;
        }

        @Override
        public @Nullable BakedModel bakeUncached(UnbakedModel model, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
            var original = this.modelTextureGetter;
            this.modelTextureGetter = sprites;
            var result = this.bakeUncached(model, state);
            this.modelTextureGetter = original;

            return result;
        }
    }
}