package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.geometry.GeometryLoaderManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.bluspring.kilt.injections.client.resources.model.ModelBakeryInjection;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryInject implements ModelBakeryInjection {
    @Shadow @Nullable public abstract BakedModel bake(ResourceLocation location, ModelState transform);

    @Shadow @Nullable private AtlasSet atlasSet;

    @Shadow public abstract UnbakedModel getModel(ResourceLocation modelLocation);

    @Shadow @Final private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Unique
    private final AtomicReference<Function<Material, TextureAtlasSprite>> sprites = new AtomicReference<>();

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.BEFORE))
    private void kilt$initGeometryLoader(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profiler, int maxMipmapLevel, CallbackInfo ci) {
        GeometryLoaderManager.init();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 4, shift = At.Shift.BEFORE))
    private void kilt$loadAdditionalModels(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profiler, int maxMipmapLevel, CallbackInfo ci) {
        Set<ResourceLocation> additionalModels = Sets.newHashSet();
        ForgeHooksClient.onRegisterAdditionalModels(additionalModels);

        for (ResourceLocation location : additionalModels) {
            var unbaked = this.getModel(location);
            this.unbakedCache.put(location, unbaked);
            this.topLevelModels.put(location, unbaked);
        }
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Set;addAll(Ljava/util/Collection;)Z", shift = At.Shift.AFTER))
    private void kilt$gatherFluidTextures(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profiler, int maxMipmapLevel, CallbackInfo ci, @Local(ordinal = 1) Set<Material> materials) {
        ForgeHooksClient.gatherFluidTextures(materials);
    }

    @Inject(method = "uploadTextures", at = @At("TAIL"))
    public void kilt$resetGetSprite(TextureManager resourceManager, ProfilerFiller profiler, CallbackInfoReturnable<AtlasSet> cir) {
        this.sprites.set(this.atlasSet::getSprite);
    }

    @Override
    public BakedModel bake(ResourceLocation loc, ModelState state, Function<Material, TextureAtlasSprite> sprites) {
        this.sprites.set(sprites);
        var value = this.bake(loc, state);
        this.sprites.set(this.atlasSet::getSprite);
        return value;
    }

    @Override
    public @Nullable AtlasSet getAtlasSet() {
        return atlasSet;
    }

    @Inject(method = "bake", at = @At("HEAD"))
    public void kilt$checkIfSpritesAreEmpty(ResourceLocation location, ModelState transform, CallbackInfoReturnable<BakedModel> cir) {
        if (this.sprites.get() == null) {
            this.sprites.set(this.atlasSet::getSprite);
        }
    }

    @ModifyArgs(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/ItemModelGenerator;generateBlockModel(Ljava/util/function/Function;Lnet/minecraft/client/renderer/block/model/BlockModel;)Lnet/minecraft/client/renderer/block/model/BlockModel;"))
    public void kilt$useForgeSpritesForBlockModel(Args args) {
        args.set(0, this.sprites.get());
    }

    @ModifyArgs(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/UnbakedModel;bake(Lnet/minecraft/client/resources/model/ModelBakery;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/BakedModel;"))
    public void kilt$useForgeSpritesForBake(Args args) {
        args.set(1, this.sprites.get());
    }
}
