package xyz.bluspring.kilt.injections.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;
import java.util.Map;

public interface FileToIdConverterInjection {
    default Map<ResourceLocation, Resource> listMatchingResourcesFromNamespace(ResourceManager manager, String namespace) {
        throw KiltHelper.createMixinException(FileToIdConverterInjection.class, "listMatchingResourcesFromNamespace");
    }

    default Map<ResourceLocation, List<Resource>> listMatchingResourceStacksFromNamespace(ResourceManager manager, String namespace) {
        throw KiltHelper.createMixinException(FileToIdConverterInjection.class, "listMatchingResourceStacksFromNamespace");
    }
}
