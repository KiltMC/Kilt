package xyz.bluspring.kilt.injections.server.packs.metadata.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

import java.util.Map;
import java.util.Optional;

public interface PackMetadataSectionInjection {
    static PackMetadataSection create(Component description, int packFormat) {
        return new PackMetadataSection(description, packFormat, Optional.empty());
    }
}
