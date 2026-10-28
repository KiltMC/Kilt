package xyz.bluspring.kilt.injections.server.packs;

import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionType;

public interface OverlayMetadataSectionInjection {
    MetadataSectionType<OverlayMetadataSection> NEOFORGE_TYPE = MetadataSectionType.fromCodec("neoforge:overlays", OverlayMetadataSection.CODEC);
}
