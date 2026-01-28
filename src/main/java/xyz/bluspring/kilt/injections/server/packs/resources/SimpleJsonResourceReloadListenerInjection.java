package xyz.bluspring.kilt.injections.server.packs.resources;

import net.minecraft.resources.ResourceLocation;
import xyz.bluspring.kilt.util.KiltHelper;

public interface SimpleJsonResourceReloadListenerInjection {
    default ResourceLocation getPreparedPath(ResourceLocation loc) {
        throw KiltHelper.createMixinException(SimpleJsonResourceReloadListenerInjection.class, "getPreparedPath");
    }
}
