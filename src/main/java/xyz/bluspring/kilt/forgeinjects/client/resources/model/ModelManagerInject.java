package xyz.bluspring.kilt.forgeinjects.client.resources.model;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.include.com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

@Mixin(ModelManager.class)
public class ModelManagerInject {
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

    @Inject(method = "apply(Lnet/minecraft/client/resources/model/ModelBakery;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", shift = At.Shift.BEFORE))
    public void kilt$addModelBakery(ModelBakery object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        this.modelBakery = object;
        // ForgeHooksClient.onModelBake
        ModLoader.get().postEvent(new ModelEvent.BakingCompleted((ModelManager) (Object) this, this.bakedRegistry, object));
    }

    public ModelBakery getModelBakery() {
        return Preconditions.checkNotNull(modelBakery, "Attempted to query model bakery before it has been initialized.");
    }
}
