package xyz.bluspring.kilt.injects.resources;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.resources.FileToIdConverterInjection;

import java.util.List;
import java.util.Map;

@Mixin(FileToIdConverter.class)
public abstract class FileToIdConverterInject implements FileToIdConverterInjection {
    @Shadow @Final private String prefix;
    @Shadow @Final private String extension;

    @Override
    public Map<ResourceLocation, Resource> listMatchingResourcesFromNamespace(ResourceManager manager, String namespace) {
        String extension = this.extension;
        return manager.listResources(this.prefix, path -> path.getNamespace().equals(namespace) && path.getPath().endsWith(extension));
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listMatchingResourceStacksFromNamespace(ResourceManager manager, String namespace) {
        String extension = this.extension;
        return manager.listResourceStacks(this.prefix, path -> path.getNamespace().equals(namespace) && path.getPath().endsWith(extension));
    }
}
