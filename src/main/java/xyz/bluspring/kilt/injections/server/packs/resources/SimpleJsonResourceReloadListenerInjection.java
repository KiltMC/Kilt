package xyz.bluspring.kilt.injections.server.packs.resources;

import net.minecraft.resources.ResourceLocation;

public interface SimpleJsonResourceReloadListenerInjection {
    ResourceLocation getPreparedPath(ResourceLocation loc);
}
