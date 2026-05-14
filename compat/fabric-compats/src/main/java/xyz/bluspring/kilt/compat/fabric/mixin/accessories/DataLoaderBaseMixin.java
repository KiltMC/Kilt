package xyz.bluspring.kilt.compat.fabric.mixin.accessories;

import io.wispforest.accessories.DataLoaderBase;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.common.data.CuriosEntityManager;
import top.theillusivec4.curios.common.data.CuriosSlotManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(value = DataLoaderBase.class, priority = 950, remap = false)
public abstract class DataLoaderBaseMixin {
    // Blodhgarm told me to do this so I'm doing it
    @Inject(method = "registerListeners", at = @At("HEAD"))
    private void kilt$initAccessoriesCuriosLayer(CallbackInfo ci) {
        ResourceManagerHelper manager = ResourceManagerHelper.get(PackType.SERVER_DATA);
        manager.registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation("kilt", "accessories/curios/slot_manager");
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return CuriosSlotManager.SERVER.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }
        });

        manager.registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation("kilt", "accessories/curios/entity_manager");
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return CuriosEntityManager.SERVER.reload(preparationBarrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }
        });
    }
}
