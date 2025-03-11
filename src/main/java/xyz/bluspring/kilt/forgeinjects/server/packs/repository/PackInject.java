package xyz.bluspring.kilt.forgeinjects.server.packs.repository;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.util.function.Supplier;

@Mixin(Pack.class)
public abstract class PackInject {
    @Unique private boolean hidden = false;

    public PackInject(String id, boolean required, Supplier<PackResources> supplier, Component title, Component description, PackCompatibility compatibility, Pack.Position defaultPosition, boolean fixedPosition, PackSource packSource) {}
    public PackInject(String id, Component title, boolean required, Supplier<PackResources> supplier, PackMetadataSection metadata, PackType type, Pack.Position defaultPosition, PackSource packSource) {}

    @CreateInitializer
    public PackInject(String id, boolean required, Supplier<PackResources> supplier, Component title, Component description, PackCompatibility compatibility, Pack.Position defaultPosition, boolean fixedPosition, PackSource packSource, boolean hidden) {
        this(id, required, supplier, title, description, compatibility, defaultPosition, fixedPosition, packSource);
        this.hidden = hidden;
    }

    @CreateInitializer
    public PackInject(String id, Component title, boolean required, Supplier<PackResources> supplier, PackMetadataSection metadata, PackType type, Pack.Position defaultPosition, PackSource packSource, boolean hidden) {
        this(id, title, required, supplier, metadata, type, defaultPosition, packSource);
        this.hidden = hidden;
    }

    @Intrinsic
    public boolean isHidden() {
        return this.hidden;
    }

    @Mixin(Pack.PackConstructor.class)
    public interface PackConstructorInject {
        @Shadow @Nullable Pack create(String string, Component component, boolean bl, Supplier<PackResources> supplier, PackMetadataSection packMetadataSection, Pack.Position position, PackSource packSource);

        default Pack create(String string, Component component, boolean bl, Supplier<PackResources> supplier, PackMetadataSection packMetadataSection, Pack.Position position, PackSource packSource, boolean hidden) {
            // TODO: handle hidden?
            return this.create(string, component, bl, supplier, packMetadataSection, position, packSource);
        }
    }
}
