package xyz.bluspring.kilt.injections.server.packs.repository;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.world.flag.FeatureFlagSet;

import java.util.List;

public interface PackMetadataInjection {
    static Pack.Metadata create(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays, boolean hidden) {
        var metadata = new Pack.Metadata(description, compatibility, requestedFeatures, overlays);
        ((PackMetadataInjection) (Object) metadata).kilt$markForge();
        ((PackMetadataInjection) (Object) metadata).kilt$setHidden(hidden);
        return metadata;
    }

    void kilt$setHidden(boolean hidden);
    void kilt$markForge();

    boolean hidden();
}
