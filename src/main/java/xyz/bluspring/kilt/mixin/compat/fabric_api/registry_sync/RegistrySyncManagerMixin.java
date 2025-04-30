package xyz.bluspring.kilt.mixin.compat.fabric_api.registry_sync;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RegistrySyncManager.class, remap = false)
public abstract class RegistrySyncManagerMixin {
    @Inject(method = "apply", at = @At("HEAD"), remap = false)
    private static void kilt$trySyncForgeRegistries(Map<ResourceLocation, Object2IntMap<ResourceLocation>> map, RemappableRegistry.RemapMode mode, CallbackInfo ci) {
        GameData.kilt$rebuildBlockMaps();
    }
}
