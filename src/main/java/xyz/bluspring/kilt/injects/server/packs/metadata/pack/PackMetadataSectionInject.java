// TRACKED HASH: c3fbda8033be117044af2f7df8b9938868361aaa
package xyz.bluspring.kilt.injects.server.packs.metadata.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.server.packs.metadata.pack.PackMetadataSectionInjection;

import java.util.Map;
import java.util.Optional;

@Mixin(PackMetadataSection.class)
public abstract class PackMetadataSectionInject implements PackMetadataSectionInjection {
    public PackMetadataSectionInject(Component description, int packFormat, Optional<InclusiveRange<Integer>> supportedFormats) {
    }

    @CreateInitializer
    public PackMetadataSectionInject(Component description, int packFormat) {
        this(description, packFormat, Optional.empty());
    }
}