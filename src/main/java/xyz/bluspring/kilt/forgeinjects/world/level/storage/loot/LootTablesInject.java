package xyz.bluspring.kilt.forgeinjects.world.level.storage.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.packs.resources.SimpleJsonResourceReloadListenerInjection;

import java.util.Map;

@Mixin(LootTables.class)
public abstract class LootTablesInject extends SimpleJsonResourceReloadListener {
    @Unique private static final ThreadLocal<LootTables> kilt$instance = new ThreadLocal<>();
    @Unique private static final ThreadLocal<ResourceManager> kilt$resourceManager = new ThreadLocal<>();

    public LootTablesInject(Gson gson, String directory) {
        super(gson, directory);
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void kilt$storeResourceManager(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        kilt$instance.set((LootTables) (Object) this);
        kilt$resourceManager.set(resourceManager);
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
    private void kilt$clearResourceManager(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        kilt$instance.remove();
        kilt$resourceManager.remove();
    }

    @Redirect(method = "method_20711", at = @At(value = "INVOKE", target = "Lcom/google/gson/Gson;fromJson(Lcom/google/gson/JsonElement;Ljava/lang/Class;)Ljava/lang/Object;"))
    private static <T> T kilt$forgeLoadLootTable(Gson instance, JsonElement json, Class<T> classOfT, @Local(argsOnly = true) ResourceLocation location) {
        var resource = kilt$resourceManager.get().getResource(((SimpleJsonResourceReloadListenerInjection) kilt$instance.get()).getPreparedPath(location)).orElse(null);
        return (T) ForgeHooks.loadLootTable(instance, location, json, resource == null || !resource.sourcePackId().equals("Default"), kilt$instance.get());
    }
}
