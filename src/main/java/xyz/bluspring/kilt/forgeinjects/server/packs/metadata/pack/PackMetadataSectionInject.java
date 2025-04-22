// TRACKED HASH: c3fbda8033be117044af2f7df8b9938868361aaa
package xyz.bluspring.kilt.forgeinjects.server.packs.metadata.pack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.packs.metadata.pack.PackMetadataSectionInjection;

import java.util.Map;

@Mixin(PackMetadataSection.class)
public abstract class PackMetadataSectionInject implements PackMetadataSectionInjection {
    @Mutable @Shadow @Final private Component description;
    @Mutable @Shadow @Final private int packFormat;
    @Unique private Map<PackType, Integer> packTypeVersions;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setEmptyPackTypeVersions(Component description, int packFormat, CallbackInfo ci) {
        this.packTypeVersions = Map.of();
    }

    public PackMetadataSectionInject(Component description, int packFormat, Map<PackType, Integer> packTypeVersions) {
        this.description = description;
        this.packFormat = packFormat;
        this.packTypeVersions = packTypeVersions;
    }

    @Override
    public int getPackFormat(PackType packType) {
        return packTypeVersions.getOrDefault(packType, this.packFormat);
    }
}