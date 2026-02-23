package xyz.bluspring.kilt.mixin.compat.fabric_api;

import net.fabricmc.fabric.impl.resource.loader.ServerLanguageUtil;
import net.fabricmc.loader.api.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;
import java.util.Collections;
import java.util.List;

@Mixin(value = ServerLanguageUtil.class, remap = false)
public class ServerLanguageUtilMixin {
    @Redirect(method = "getModLanguageFiles", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/api/ModContainer;getRootPaths()Ljava/util/List;"))
    private static List<Path> kilt$skipMissingJarFileSystems(ModContainer instance) {
        try {
            return instance.getRootPaths();
        } catch (ProviderNotFoundException exception) {
            return Collections.emptyList();
        }
    }
}
