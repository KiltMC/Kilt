package xyz.bluspring.kilt.injections.resources;

import java.util.List;
import java.util.Map;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public interface FileToIdConverterInjection {
    default Map<Identifier, Resource> listMatchingResourcesFromNamespace(ResourceManager manager, String namespace) {
        throw KiltHelper.createMixinException(FileToIdConverterInjection.class, "listMatchingResourcesFromNamespace");
    }

    default Map<Identifier, List<Resource>> listMatchingResourceStacksFromNamespace(ResourceManager manager, String namespace) {
        throw KiltHelper.createMixinException(FileToIdConverterInjection.class, "listMatchingResourceStacksFromNamespace");
    }
}
