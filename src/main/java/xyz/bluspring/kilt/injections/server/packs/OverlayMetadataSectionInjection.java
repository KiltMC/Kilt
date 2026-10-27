package xyz.bluspring.kilt.injections.server.packs;

import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public interface OverlayMetadataSectionInjection {
    MetadataSectionType<OverlayMetadataSection> NEOFORGE_CLIENT_TYPE = new MetadataSectionType<>("neoforge:overlays", OverlayMetadataSection.CLIENT_TYPE.codec());
    MetadataSectionType<OverlayMetadataSection> NEOFORGE_SERVER_TYPE = new MetadataSectionType<>("neoforge:overlays", OverlayMetadataSection.SERVER_TYPE.codec());

    static MetadataSectionType<OverlayMetadataSection> forPackTypeNeoForge(PackType packType) {
        return switch (packType) {
            case CLIENT_RESOURCES -> NEOFORGE_CLIENT_TYPE;
            case SERVER_DATA -> NEOFORGE_SERVER_TYPE;
        };
    }
}
