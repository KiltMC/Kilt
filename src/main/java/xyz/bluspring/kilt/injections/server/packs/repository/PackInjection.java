package xyz.bluspring.kilt.injections.server.packs.repository;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;
import java.util.stream.Stream;

public interface PackInjection {
    PackSource CHILD_SOURCE = PackSource.create(
            name -> Component.translatable(
                    "pack.nameAndSource",
                    name,
                    Component.translatable("pack.neoforge.source.child")
            ).withStyle(net.minecraft.ChatFormatting.GRAY),
            false
    ); // Neo: Pack source for child packs; should not be otherwise used

    static Pack create(PackLocationInfo location, Pack.ResourcesSupplier resources, Pack.Metadata metadata, PackSelectionConfig selectionConfig, List<Pack> children) {
        var pack = new Pack(location, resources, metadata, selectionConfig);
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
                Pack adaptedChild = new Pack(
                        new PackLocationInfo(child.location().id(), child.location().title(), CHILD_SOURCE, child.location().knownPackInfo()),
                        child.resources,
                        PackMetadataInjection.create(child.metadata.description(), child.metadata.compatibility(), child.metadata.requestedFeatures(), child.metadata.overlays(), true),
                        new PackSelectionConfig(false, child.getDefaultPosition(), child.isFixedPosition())
                );
                adaptedChild.kilt$setChildren(List.of());
                flattenedChildren.add(adaptedChild);
                remainingChildren.addAll(child.getChildren());
            }
        }

        pack.kilt$setChildren(List.copyOf(flattenedChildren));
        return pack;
    }

    default Pack hidden() {
        throw KiltHelper.createMixinException(PackInjection.class, "hidden");
    }

    default boolean isHidden() {
        throw KiltHelper.createMixinException(PackInjection.class, "isHidden");
    }

    default List<Pack> getChildren() {
        throw KiltHelper.createMixinException(PackInjection.class, "getChildren");
    }

    default Stream<Pack> streamSelfAndChildren() {
        throw KiltHelper.createMixinException(PackInjection.class, "streamSelfAndChildren");
    }

    default Pack withChildren(List<Pack> children) {
        throw KiltHelper.createMixinException(PackInjection.class, "withChildren");
    }

    default void kilt$setChildren(List<Pack> children) {
        throw KiltHelper.createMixinException(PackInjection.class, "kilt$setChildren");
    }
}
