package xyz.bluspring.kilt.injections.world.item;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import xyz.bluspring.kilt.mixin.CreativeModeTabAccessor;
import xyz.bluspring.kilt.mixin.world.item.CreativeModeTabBuilderAccessor;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;

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
    default void kilt$setScrollerSprite(ResourceLocation location) {}

    default ResourceLocation getScrollerSprite() {
        throw KiltHelper.createMixinException(CreativeModeTabInjection.class, "getScrollerSprite");
    }

    default boolean hasSearchBar() {
        throw KiltHelper.createMixinException(CreativeModeTabInjection.class, "hasSearchBar");
    }

    default int getSearchBarWidth() {
        throw KiltHelper.createMixinException(CreativeModeTabInjection.class, "getSearchBarWidth");
    }

    default ResourceLocation getTabsImage() {
        throw KiltHelper.createMixinException(CreativeModeTabInjection.class, "getTabsImage");
    }

    default int getLabelColor() {
        throw KiltHelper.createMixinException(CreativeModeTabInjection.class, "getLabelColor");
    }

    default int getSlotColor() {
        throw KiltHelper.createMixinException(CreativeModeTabInjection.class, "");
    }

    default List<ResourceLocation> kilt$getTabsBefore() {
        throw KiltHelper.createMixinException(CreativeModeTabInjection.class, "kilt$getTabsBefore");
    }

    default List<ResourceLocation> kilt$getTabsAfter() {
        throw KiltHelper.createMixinException(CreativeModeTabInjection.class, "kilt$getTabsAfter");
    }

    interface BuilderInjection {
        default CreativeModeTab.Builder withScrollBarSpriteLocation(ResourceLocation location) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withScrollBarSpriteLocation");
        }

        default CreativeModeTab.Builder withSearchBar() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withSearchBar");
        }

        default CreativeModeTab.Builder withSearchBar(int searchBarWidth) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withSearchBar");
        }

        default CreativeModeTab.Builder withTabsImage(ResourceLocation tabsImage) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withTabsImage");
        }

        default CreativeModeTab.Builder withLabelColor(int labelColor) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withLabelColor");
        }

        default CreativeModeTab.Builder withSlotColor(int slotColor) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withSlotColor");
        }

        default CreativeModeTab.Builder withTabFactory(Function<CreativeModeTab.Builder, CreativeModeTab> factory) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withTabFactory");
        }

        default CreativeModeTab.Builder withTabsBefore(ResourceKey<CreativeModeTab>... tabs) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withTabsBefore");
        }

        default CreativeModeTab.Builder withTabsAfter(ResourceKey<CreativeModeTab>... tabs) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withTabsAfter");
        }

        default CreativeModeTab.Builder withTabsBefore(ResourceLocation... tabs) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withTabsBefore");
        }

        default CreativeModeTab.Builder withTabsAfter(ResourceLocation... tabs) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "withTabsAfter");
        }

        default CreativeModeTab.Builder displayItems(Collection<? extends Holder<? extends ItemLike>> collection) {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "displayItems");
        }

        default boolean kilt$hasSearchBar() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$hasSearchBar");
        }

        default int kilt$searchBarWidth() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$searchBarWidth");
        }

        default ResourceLocation kilt$getTabsImage() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$getTabsImage");
        }

        default int kilt$labelColor() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$labelColor");
        }

        default int kilt$slotColor() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$slotColor");
        }

        default Function<CreativeModeTab.Builder, CreativeModeTab> kilt$getTabFactory() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$getTabFactory");
        }

        default List<ResourceLocation> kilt$getTabsBefore() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$getTabsBefore");
        }

        default List<ResourceLocation> kilt$getTabsAfter() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$getTabsAfter");
        }

        default ResourceLocation kilt$scrollerSpriteLocation() {
            throw KiltHelper.createMixinException(CreativeModeTabInjection.BuilderInjection.class, "kilt$scrollerSpriteLocation");
        }
    }
}
