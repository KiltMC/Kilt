// TRACKED HASH: 57e42230249e35464dc40d8cca2321d0e4608ae8
package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.geometry.GeometryLoaderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.include.com.google.common.base.Preconditions;
import xyz.bluspring.kilt.injections.client.resources.model.ModelManagerInjection;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
public class ModelManagerInject implements ModelManagerInjection {
    @Shadow private Map<ResourceLocation, BakedModel> bakedRegistry;

    @Shadow private BakedModel missingModel;
    @Unique
    private ModelBakery modelBakery;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void kilt$initializeBakedRegistry(TextureManager textureManager, BlockColors blockColors, int i, CallbackInfo ci) {
        this.bakedRegistry = new HashMap<>();
    }

    public BakedModel getModel(ResourceLocation modelLocation) {
        return this.bakedRegistry.getOrDefault(modelLocation, this.missingModel);
    }

    @Inject(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;startTick()V", shift = At.Shift.AFTER))
    private void kilt$initGeometryLoader(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        GeometryLoaderManager.init();
    }

    @Inject(method = "loadModels", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.BEFORE))
    private void kilt$modifyBakingResult(ProfilerFiller profilerFiller, Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations, ModelBakery modelBakery, CallbackInfoReturnable<ModelManager.ReloadState> cir) {
        profilerFiller.popPush("forge_modify_baking_result");
        ForgeHooksClient.onModifyBakingResult(modelBakery.getBakedTopLevelModels(), modelBakery);
    }

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = At.Shift.BEFORE))
    public void kilt$addModelBakery(ModelManager.ReloadState reloadState, ProfilerFiller profiler, CallbackInfo ci, @Local ModelBakery bakery) {
        this.modelBakery = bakery;
        ForgeHooksClient.onModelBake((ModelManager) (Object) this, this.bakedRegistry, bakery);
    }

    public ModelBakery getModelBakery() {
        return Preconditions.checkNotNull(modelBakery, "Attempted to query model bakery before it has been initialized.");
    }
}