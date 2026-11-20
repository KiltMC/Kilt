package xyz.bluspring.kilt.injects.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.WorldVersion;
import net.minecraft.data.HashCache;

@Mixin(HashCache.class)
public abstract class HashCacheInject {
    @Shadow @Final private Map<String, HashCache.ProviderCache> caches;
    @Unique private Map<String, HashCache.ProviderCache> originalCaches;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$storeOriginalCaches(Path rootDir, Collection providers, WorldVersion version, CallbackInfo ci) {
        this.originalCaches = Map.copyOf(this.caches);
    }

    @WrapWithCondition(method = "lambda$purgeStaleAndWrite$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/HashCache$ProviderCache;save(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/lang/String;)V"))
    private boolean kilt$checkShouldRewriteCache(HashCache.ProviderCache instance, Path rootDir, Path cacheFile, String extraHeaderInfo, @Local(argsOnly = true, name = "providerId") String name) {
        return !instance.equals(this.originalCaches.get(name)) || !Files.exists(cacheFile);
    }
}
