package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import xyz.bluspring.kilt.mixin.CreativeModeTabAccessor;
import xyz.bluspring.kilt.mixin.world.item.CreativeModeTabBuilderAccessor;

import java.util.List;
import java.util.function.Function;

public interface CreativeModeTabInjection {
    static CreativeModeTab create(CreativeModeTab.Builder builder) {
        var tab = CreativeModeTabAccessor.createCreativeModeTab(((CreativeModeTabBuilderAccessor) builder).getRow(), ((CreativeModeTabBuilderAccessor) builder).getColumn(), ((CreativeModeTabBuilderAccessor) builder).getType(), ((CreativeModeTabBuilderAccessor) builder).getDisplayName(), ((CreativeModeTabBuilderAccessor) builder).getIconGenerator(), ((CreativeModeTabBuilderAccessor) builder).getDisplayItemsGenerator());
        tab.kilt$assignValues(builder);

        return tab;
    }

    static CreativeModeTab.Builder builder() {
        return new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0);
    }

    // Helper method for both create() here and <init> in the mixin
    default void kilt$assignValues(CreativeModeTab.Builder builder) {}
    default void kilt$setBackgroundLocation(ResourceLocation location) {}

    default ResourceLocation getBackgroundLocation() {
        throw new IllegalStateException();
    }
    default boolean hasSearchBar() {
        throw new IllegalStateException();
    }
    default int getSearchBarWidth() {
        throw new IllegalStateException();
    }
    default ResourceLocation getTabsImage() {
        throw new IllegalStateException();
    }
    default int getLabelColor() {
        throw new IllegalStateException();
    }
    default int getSlotColor() {
        throw new IllegalStateException();
    }

    default List<ResourceLocation> kilt$getTabsBefore() {
        throw new IllegalStateException();
    }
    default List<ResourceLocation> kilt$getTabsAfter() {
        throw new IllegalStateException();
    }

    interface BuilderInjection {
        default CreativeModeTab.Builder withBackgroundLocation(ResourceLocation background) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withSearchBar() {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withSearchBar(int searchBarWidth) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withTabsImage(ResourceLocation tabsImage) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withLabelColor(int labelColor) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withSlotColor(int slotColor) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withTabFactory(Function<CreativeModeTab.Builder, CreativeModeTab> factory) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withTabsBefore(ResourceKey<CreativeModeTab>... tabs) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withTabsAfter(ResourceKey<CreativeModeTab>... tabs) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withTabsBefore(ResourceLocation... tabs) {
            throw new IllegalStateException();
        }
        default CreativeModeTab.Builder withTabsAfter(ResourceLocation... tabs) {
            throw new IllegalStateException();
        }

        default ResourceLocation kilt$getBackgroundLocation() {
            throw new IllegalStateException();
        }
        default boolean kilt$hasSearchBar() {
            throw new IllegalStateException();
        }
        default int kilt$searchBarWidth() {
            throw new IllegalStateException();
        }
        default ResourceLocation kilt$getTabsImage() {
            throw new IllegalStateException();
        }
        default int kilt$labelColor() {
            throw new IllegalStateException();
        }
        default int kilt$slotColor() {
            throw new IllegalStateException();
        }
        default Function<CreativeModeTab.Builder, CreativeModeTab> kilt$getTabFactory() {
            throw new IllegalStateException();
        }
        default List<ResourceLocation> kilt$getTabsBefore() {
            throw new IllegalStateException();
        }
        default List<ResourceLocation> kilt$getTabsAfter() {
            throw new IllegalStateException();
        }
    }
}
