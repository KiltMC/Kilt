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
        ((CreativeModeTabInjection) tab).kilt$assignValues(builder);

        return tab;
    }

    // Helper method for both create() here and <init> in the mixin
    void kilt$assignValues(CreativeModeTab.Builder builder);
    void kilt$setBackgroundLocation(ResourceLocation location);

    ResourceLocation getBackgroundLocation();
    boolean hasSearchBar();
    int getSearchBarWidth();
    ResourceLocation getTabsImage();
    int getLabelColor();
    int getSlotColor();

    List<ResourceLocation> kilt$getTabsBefore();
    List<ResourceLocation> kilt$getTabsAfter();

    interface BuilderInjection {
        CreativeModeTab.Builder withBackgroundLocation(ResourceLocation background);
        CreativeModeTab.Builder withSearchBar();
        CreativeModeTab.Builder withSearchBar(int searchBarWidth);
        CreativeModeTab.Builder withTabsImage(ResourceLocation tabsImage);
        CreativeModeTab.Builder withLabelColor(int labelColor);
        CreativeModeTab.Builder withSlotColor(int slotColor);
        CreativeModeTab.Builder withTabFactory(Function<CreativeModeTab.Builder, CreativeModeTab> factory);
        CreativeModeTab.Builder withTabsBefore(ResourceKey<CreativeModeTab>... tabs);
        CreativeModeTab.Builder withTabsAfter(ResourceKey<CreativeModeTab>... tabs);
        CreativeModeTab.Builder withTabsBefore(ResourceLocation... tabs);
        CreativeModeTab.Builder withTabsAfter(ResourceLocation... tabs);

        ResourceLocation kilt$getBackgroundLocation();
        boolean kilt$hasSearchBar();
        int kilt$searchBarWidth();
        ResourceLocation kilt$getTabsImage();
        int kilt$labelColor();
        int kilt$slotColor();
        Function<CreativeModeTab.Builder, CreativeModeTab> kilt$getTabFactory();
        List<ResourceLocation> kilt$getTabsBefore();
        List<ResourceLocation> kilt$getTabsAfter();
    }
}
