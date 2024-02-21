package xyz.bluspring.kilt.injections.server.packs.metadata.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

import java.util.Map;

public interface PackMetadataSectionInjection {
    static PackMetadataSection create(Component description, int packFormat, Map<PackType, Integer> packTypeVersions) {
        var section = new PackMetadataSection(description, packFormat);
        ((PackMetadataSectionInjection) section).kilt$setPackTypeVersions(packTypeVersions);
        return section;
    }

    void kilt$setPackTypeVersions(Map<PackType, Integer> packTypeVersions);

    int getPackFormat(PackType packType);
}
