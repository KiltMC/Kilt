// TRACKED HASH: be042a2fb57e71e2ff4f71f41e1bbac6b9698ff7
package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.CreativeModeTabInjection;
import xyz.bluspring.kilt.mixin.world.item.CreativeModeTabBuilderAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabInject implements CreativeModeTabInjection {
    @Shadow @Final private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;
    @Unique private ResourceLocation backgroundLocation;
    @Unique private boolean hasSearchBar;
    @Unique private int searchBarWidth;
    @Unique private ResourceLocation tabImage;
    @Unique private int labelColor;
    @Unique private int slotColor;
    @Unique private List<ResourceLocation> tabsBefore;
    @Unique private List<ResourceLocation> tabsAfter;

    CreativeModeTabInject(CreativeModeTab.Row row, int column, CreativeModeTab.Type type, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemGenerator) {
    }

    @CreateInitializer
    protected CreativeModeTabInject(CreativeModeTab.Builder builder) {
        this(((CreativeModeTabBuilderAccessor) builder).getRow(), ((CreativeModeTabBuilderAccessor) builder).getColumn(), ((CreativeModeTabBuilderAccessor) builder).getType(), ((CreativeModeTabBuilderAccessor) builder).getDisplayName(), ((CreativeModeTabBuilderAccessor) builder).getIconGenerator(), ((CreativeModeTabBuilderAccessor) builder).getDisplayItemsGenerator());

        kilt$assignValues(builder);
    }

    @Override
    public void kilt$assignValues(CreativeModeTab.Builder builder) {
        var b = (CreativeModeTabInjection.BuilderInjection) builder;
        this.backgroundLocation = b.kilt$getBackgroundLocation();
        this.hasSearchBar = b.kilt$hasSearchBar();
        this.searchBarWidth = b.kilt$searchBarWidth();
        this.tabImage = b.kilt$getTabsImage();
        this.labelColor = b.kilt$labelColor();
        this.slotColor = b.kilt$slotColor();
        this.tabsBefore = b.kilt$getTabsBefore();
        this.tabsAfter = b.kilt$getTabsAfter();
    }

    @CreateStatic
    private static CreativeModeTab.Builder builder() {
        return new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0);
    }

    @Redirect(method = "buildContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;accept(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;Lnet/minecraft/world/item/CreativeModeTab$Output;)V"))
    private void kilt$buildContentsWithForge(CreativeModeTab.DisplayItemsGenerator instance, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output itemDisplayBuilder, @Local ResourceKey<CreativeModeTab> resourceKey) {
        ForgeHooksClient.onCreativeModeTabBuildContents((CreativeModeTab) (Object) this, resourceKey, instance, parameters, itemDisplayBuilder);
    }

    @Override
    public void kilt$setBackgroundLocation(ResourceLocation location) {
        this.backgroundLocation = location;
    }

    @Override
    public ResourceLocation getBackgroundLocation() {
        return backgroundLocation;
    }

    @Override
    public boolean hasSearchBar() {
        return hasSearchBar;
    }

    @Override
    public int getSearchBarWidth() {
        return searchBarWidth;
    }

    @Override
    public ResourceLocation getTabsImage() {
        return tabImage;
    }

    @Override
    public int getLabelColor() {
        return labelColor;
    }

    @Override
    public int getSlotColor() {
        return slotColor;
    }

    @Override
    public List<ResourceLocation> kilt$getTabsBefore() {
        return this.tabsBefore;
    }

    @Override
    public List<ResourceLocation> kilt$getTabsAfter() {
        return this.tabsAfter;
    }

    @Mixin(CreativeModeTab.Builder.class)
    public static abstract class BuilderInject implements CreativeModeTabInjection.BuilderInjection {
        @Shadow public abstract CreativeModeTab.Builder backgroundSuffix(String backgroundSuffix);

        @Shadow private String backgroundSuffix;
        @Unique private static final ResourceLocation CREATIVE_INVENTORY_TABS_IMAGE = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
        
        @Unique @Nullable private ResourceLocation backgroundLocation;
        @Unique private boolean hasSearchBar = false;
        @Unique private int searchBarWidth = 89;
        @Unique private ResourceLocation tabsImage = CREATIVE_INVENTORY_TABS_IMAGE;
        @Unique private int labelColor = 4210752;
        @Unique private int slotColor = -2130706433;
        @Unique private Function<CreativeModeTab.Builder, CreativeModeTab> tabFactory = CreativeModeTabInjection::create;
        @Unique private final List<ResourceLocation> tabsBefore = new ArrayList<>();
        @Unique private final List<ResourceLocation> tabsAfter = new ArrayList<>();
        
        @Unique
        private CreativeModeTab.Builder self() {
            return (CreativeModeTab.Builder) (Object) this;
        } 

        @Override
        public CreativeModeTab.Builder withBackgroundLocation(ResourceLocation background) {
            this.backgroundLocation = background;
            return self();
        }

        @Override
        public CreativeModeTab.Builder withSearchBar() {
            this.hasSearchBar = true;
            if (this.backgroundLocation == null)
                return this.backgroundSuffix("item_search.png");
            return self();
        }

        @Override
        public CreativeModeTab.Builder withSearchBar(int searchBarWidth) {
            this.searchBarWidth = searchBarWidth;
            return withSearchBar();
        }

        @Override
        public CreativeModeTab.Builder withTabsImage(ResourceLocation tabsImage) {
            this.tabsImage = tabsImage;
            return self();
        }

        @Override
        public CreativeModeTab.Builder withLabelColor(int labelColor) {
            this.labelColor = labelColor;
            return self();
        }

        @Override
        public CreativeModeTab.Builder withSlotColor(int slotColor) {
            this.slotColor = slotColor;
            return self();
        }

        @Override
        public CreativeModeTab.Builder withTabFactory(Function<CreativeModeTab.Builder, CreativeModeTab> factory) {
            this.tabFactory = factory;
            return self();
        }

        @Override
        public CreativeModeTab.Builder withTabsBefore(ResourceKey<CreativeModeTab>... tabs) {
            Stream.of(tabs).map(ResourceKey::location).forEach(this.tabsBefore::add);
            return self();
        }

        @Override
        public CreativeModeTab.Builder withTabsAfter(ResourceKey<CreativeModeTab>... tabs) {
            Stream.of(tabs).map(ResourceKey::location).forEach(this.tabsAfter::add);
            return self();
        }

        @Override
        public CreativeModeTab.Builder withTabsBefore(ResourceLocation... tabs) {
            this.tabsBefore.addAll(List.of(tabs));
            return self();
        }

        @Override
        public CreativeModeTab.Builder withTabsAfter(ResourceLocation... tabs) {
            this.tabsAfter.addAll(List.of(tabs));
            return self();
        }

        @Override
        public ResourceLocation kilt$getBackgroundLocation() {
            return backgroundLocation;
        }

        @Override
        public boolean kilt$hasSearchBar() {
            return hasSearchBar;
        }

        @Override
        public int kilt$searchBarWidth() {
            return searchBarWidth;
        }

        @Override
        public ResourceLocation kilt$getTabsImage() {
            return tabsImage;
        }

        @Override
        public int kilt$labelColor() {
            return labelColor;
        }

        @Override
        public int kilt$slotColor() {
            return slotColor;
        }

        @Override
        public Function<CreativeModeTab.Builder, CreativeModeTab> kilt$getTabFactory() {
            return tabFactory;
        }

        @Override
        public List<ResourceLocation> kilt$getTabsBefore() {
            return tabsBefore;
        }

        @Override
        public List<ResourceLocation> kilt$getTabsAfter() {
            return tabsAfter;
        }

        @Inject(method = "type", at = @At("TAIL"), cancellable = true)
        private void kilt$addSearchBarForType(CreativeModeTab.Type type, CallbackInfoReturnable<CreativeModeTab.Builder> cir) {
            if (type == CreativeModeTab.Type.SEARCH)
                cir.setReturnValue(this.withSearchBar());
        }

        @Redirect(method = "build", at = @At(value = "NEW", target = "(Lnet/minecraft/world/item/CreativeModeTab$Row;ILnet/minecraft/world/item/CreativeModeTab$Type;Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;)Lnet/minecraft/world/item/CreativeModeTab;"))
        private CreativeModeTab kilt$useTabFactory(CreativeModeTab.Row row, int column, CreativeModeTab.Type type, Component displayName, Supplier iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemGenerator) {
            var tab = tabFactory.apply((CreativeModeTab.Builder) (Object) this);
            ((CreativeModeTabInjection) tab).kilt$setBackgroundLocation(this.backgroundLocation != null ? this.backgroundLocation : new ResourceLocation("textures/gui/container/creative_inventory/tab_" + this.backgroundSuffix));

            return tab;
        }
    }
}