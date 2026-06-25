package xyz.bluspring.kilt.injections.server.packs.resources;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.Identifier;

public interface SimpleJsonResourceReloadListenerInjection {
    default Identifier getPreparedPath(Identifier loc) {
        throw KiltHelper.createMixinException(SimpleJsonResourceReloadListenerInjection.class, "getPreparedPath");
    }
}
