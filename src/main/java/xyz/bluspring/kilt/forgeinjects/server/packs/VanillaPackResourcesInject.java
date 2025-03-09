package xyz.bluspring.kilt.forgeinjects.server.packs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraftforge.resource.ResourceCacheManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(VanillaPackResources.class)
public abstract class VanillaPackResourcesInject implements PackResources {
    @Shadow @Final private static Map<PackType, Path> ROOT_DIR_BY_TYPE;
    @Unique private final ResourceCacheManager cacheManager = new ResourceCacheManager(false, "indexVanillaPackCachesOnThread", (packType, namespace) -> ROOT_DIR_BY_TYPE.get(packType).resolve(namespace));

    @WrapOperation(method = "getResources(Lnet/minecraft/server/packs/PackType;Ljava/lang/String;Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Collection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/VanillaPackResources;getResources(Ljava/util/Collection;Ljava/lang/String;Ljava/nio/file/Path;Ljava/lang/String;Ljava/util/function/Predicate;)V", ordinal = 2))
    private void kilt$tryUseCache(Collection<ResourceLocation> collection, String namespace, Path path, String string, Predicate<ResourceLocation> predicate, Operation<Void> original, @Local(argsOnly = true) PackType packType) {
        if (ResourceCacheManager.shouldUseCache() && this.cacheManager.hasCached(packType, namespace))
            collection.addAll(this.cacheManager.getResources(packType, namespace, path.getFileSystem().getPath(string), predicate));
        else
            original.call(collection, namespace, path, string, predicate);
    }

    @Override
    public void initForNamespace(String nameSpace) {
        if (ResourceCacheManager.shouldUseCache())
            this.cacheManager.index(nameSpace);
    }

    @Override
    public void init(PackType packType) {
        initForNamespace("minecraft");
        initForNamespace("realms");
    }
}
