package xyz.bluspring.kilt.forgeinjects.server;

import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.WorldLoader;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(WorldLoader.class)
public class WorldLoaderInject {
    @ModifyArg(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/WorldLoader;loadAndReplaceLayer(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/server/RegistryLayer;Ljava/util/List;)Lnet/minecraft/core/LayeredRegistryAccess;"))
    private static List<RegistryDataLoader.RegistryData<?>> kilt$replaceRegistryData(List<RegistryDataLoader.RegistryData<?>> registryData) {
        return DataPackRegistriesHooks.getDataPackRegistries();
    }
}
