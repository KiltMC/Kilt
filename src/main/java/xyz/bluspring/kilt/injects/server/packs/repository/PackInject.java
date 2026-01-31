// TRACKED HASH: 3c15b31dfec44b9332f86e2c6d5babfb7b181f59
package xyz.bluspring.kilt.injects.server.packs.repository;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.server.packs.OverlayMetadataSectionInjection;
import xyz.bluspring.kilt.injections.server.packs.repository.PackMetadataInjection;
import xyz.bluspring.kilt.injections.server.packs.repository.PackInjection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mixin(Pack.class)
public abstract class PackInject implements PackInjection {
    @Shadow
    @Final
    private PackSelectionConfig selectionConfig;
    @Shadow
    @Final
    private Pack.Metadata metadata;
    @Shadow
    @Final
    private Pack.ResourcesSupplier resources;
    @Shadow
    @Final
    private PackLocationInfo location;

    @Unique private boolean hidden;
    private List<Pack> children;

    public PackInject(PackLocationInfo location, Pack.ResourcesSupplier resources, Pack.Metadata metadata, PackSelectionConfig selectionConfig) {}

    @CreateInitializer
    private PackInject(PackLocationInfo location, Pack.ResourcesSupplier resources, Pack.Metadata metadata, PackSelectionConfig selectionConfig, List<Pack> children) {
        this(location, resources, metadata, selectionConfig);

        List<Pack> flattenedChildren = new java.util.ArrayList<>();
        List<Pack> remainingChildren = children;
        // recursively flatten children
        while (!remainingChildren.isEmpty()) {
            List<Pack> oldChildren = remainingChildren;
            remainingChildren = new java.util.ArrayList<>();
            for (Pack child : oldChildren) {
                // Adapts the child pack with the following changes:
                // - Must be hidden
                // - Must have no children
                // - Has a pack source of CHILD_SOURCE, which is not automatically added
                Pack adaptedChild = PackInjection.create(
                        new PackLocationInfo(child.location().id(), child.location().title(), CHILD_SOURCE, child.location().knownPackInfo()),
                        child.resources,
                        PackMetadataInjection.create(child.metadata.description(), child.metadata.compatibility(), child.metadata.requestedFeatures(), child.metadata.overlays(), true),
                        new PackSelectionConfig(false, child.getDefaultPosition(), child.isFixedPosition()),
                        List.of()
                );
                flattenedChildren.add(adaptedChild);
                remainingChildren.addAll(child.getChildren());
            }
        }
        this.children = List.copyOf(flattenedChildren);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setHidden(PackLocationInfo location, Pack.ResourcesSupplier resources, Pack.Metadata metadata, PackSelectionConfig selectionConfig, CallbackInfo ci) {
        this.hidden = metadata.hidden();
    }

    @WrapOperation(method = "readPackMetadata", at = @At(value = "NEW", target = "(Lnet/minecraft/network/chat/Component;Lnet/minecraft/server/packs/repository/PackCompatibility;Lnet/minecraft/world/flag/FeatureFlagSet;Ljava/util/List;)Lnet/minecraft/server/packs/repository/Pack$Metadata;"))
    private static Pack.Metadata kilt$addForgeDataToInfo(Component component, PackCompatibility packCompatibility, FeatureFlagSet featureFlagSet, List<String> list, Operation<Pack.Metadata> original, @Local(argsOnly = true) int version, @Local PackMetadataSection section, @Local PackResources resources) throws IOException {
        var neoOverlays = resources.getMetadataSection(OverlayMetadataSectionInjection.NEOFORGE_TYPE);
        if (neoOverlays != null) {
            list = new ArrayList<>(list);
            list.addAll(neoOverlays.overlaysForVersion(version));
            list = List.copyOf(list);
        }
        var info = original.call(component, packCompatibility, featureFlagSet, list);
        info.kilt$markForge();
        info.kilt$setHidden(resources.isHidden());

        return info;
    }

    @Override
    public Pack hidden() {
        return PackInjection.create(
            new PackLocationInfo(this.location.id(), this.location.title(), this.location.source(), this.location.knownPackInfo()),
            this.resources,
            PackMetadataInjection.create(this.metadata.description(), this.metadata.compatibility(), this.metadata.requestedFeatures(), this.metadata.overlays(), true),
            new PackSelectionConfig(this.selectionConfig.required(), this.selectionConfig.defaultPosition(), this.selectionConfig.fixedPosition()),
            this.children
        );
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public List<Pack> getChildren() {
        return children;
    }

    @Override
    public void kilt$setChildren(List<Pack> children) {
        this.children = children;
    }

    @Override
    public Stream<Pack> streamSelfAndChildren() {
        return Stream.concat(Stream.of((Pack) (Object) this), children.stream());
    }

    @Override
    public Pack withChildren(List<Pack> children) {
        return PackInjection.create(this.location, this.resources, this.metadata, this.selectionConfig, children);
    }

    @Mixin(Pack.Metadata.class)
    public abstract static class MetadataInject implements PackMetadataInjection {
        @Unique private boolean kilt$isForge = false;
        @Unique private boolean hidden;

        public MetadataInject(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays) {}

        @CreateInitializer
        public MetadataInject(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays, boolean hidden) {
            this(description, compatibility, requestedFeatures, overlays);
            this.hidden = hidden;
            this.kilt$isForge = true;
        }

        @Override
        public void kilt$markForge() {
            this.kilt$isForge = true;
        }

        @Override
        public boolean hidden() {
            return this.hidden;
        }

        @Override
        public void kilt$setHidden(boolean hidden) {
            this.hidden = hidden;
        }
    }
}