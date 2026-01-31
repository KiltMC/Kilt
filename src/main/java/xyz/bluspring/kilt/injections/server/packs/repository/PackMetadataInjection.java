package xyz.bluspring.kilt.injections.server.packs.repository;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.world.flag.FeatureFlagSet;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;

public interface PackMetadataInjection {
    static Pack.Metadata create(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays, boolean hidden) {
        var metadata = new Pack.Metadata(description, compatibility, requestedFeatures, overlays);
        metadata.kilt$markForge();
        metadata.kilt$setHidden(hidden);
        return metadata;
    }

    default void kilt$setHidden(boolean hidden) {
        throw KiltHelper.createMixinException(PackMetadataInjection.class, "kilt$setHidden");
    }

    default void kilt$markForge() {
        throw KiltHelper.createMixinException(PackMetadataInjection.class, "kilt$markForge");
    }

    default boolean hidden() {
        throw KiltHelper.createMixinException(PackMetadataInjection.class, "hidden");
    }
}
