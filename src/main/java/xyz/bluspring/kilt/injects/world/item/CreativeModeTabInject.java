package xyz.bluspring.kilt.injects.world.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.CreativeModeTabInjection;
import xyz.bluspring.kilt.mixin.CreativeModeTabAccessor;
import xyz.bluspring.kilt.mixin.world.item.CreativeModeTabBuilderAccessor;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabInject implements CreativeModeTabInjection {
    @Shadow
    public abstract boolean canScroll();

    @Unique private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    @Unique private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");

    @Unique private Identifier scrollerSpriteLocation;
    @Unique private boolean hasSearchBar;
    @Unique private int searchBarWidth;
    @Unique private Identifier tabImage;
    @Unique private int labelColor = Integer.MIN_VALUE;
    @Unique private int slotColor = Integer.MIN_VALUE;
    @Unique private List<Identifier> tabsBefore = new ArrayList<>();
    @Unique private List<Identifier> tabsAfter = new ArrayList<>();

    CreativeModeTabInject(CreativeModeTab.Row row, int column, CreativeModeTab.Type type, Component displayName, Supplier<ItemStack> iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemGenerator) {
    }

    @CreateInitializer
    protected CreativeModeTabInject(CreativeModeTab.Builder builder) {
        this(((CreativeModeTabBuilderAccessor) builder).getRow(), ((CreativeModeTabBuilderAccessor) builder).getColumn(), ((CreativeModeTabBuilderAccessor) builder).getType(), ((CreativeModeTabBuilderAccessor) builder).getDisplayName(), ((CreativeModeTabBuilderAccessor) builder).getIconGenerator(), ((CreativeModeTabBuilderAccessor) builder).getDisplayItemsGenerator());

        kilt$assignValues(builder);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initCustomFields(CreativeModeTab.Row row, int column, CreativeModeTab.Type type, Component displayName, Supplier iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemGenerator, CallbackInfo ci) {
        this.tabsBefore = new ArrayList<>();
        this.tabsAfter = new ArrayList<>();
    }

    @Override
    public void kilt$assignValues(CreativeModeTab.Builder builder) {
        var b = (CreativeModeTabInjection.BuilderInjection) builder;
        this.scrollerSpriteLocation = b.kilt$scrollerSpriteLocation();
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

    @WrapOperation(method = "buildContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;accept(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;Lnet/minecraft/world/item/CreativeModeTab$Output;)V"))
    private void kilt$buildContentsWithNeoForge(CreativeModeTab.DisplayItemsGenerator instance, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output, Operation<Void> original) {
        EventHooks.onCreativeModeTabBuildContents((CreativeModeTab) (Object) this, (params, out) -> original.call(instance, params, out), parameters, output);
    }

    @Override
    public void kilt$setScrollerSprite(Identifier location) {
        this.scrollerSpriteLocation = location;
    }

    @Override
    public Identifier getScrollerSprite() {
        if (this.scrollerSpriteLocation == null)
            return this.canScroll() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;

        return scrollerSpriteLocation;
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
    public Identifier getTabsImage() {
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
    public List<Identifier> kilt$getTabsBefore() {
        return this.tabsBefore;
    }

    @Override
    public List<Identifier> kilt$getTabsAfter() {
        return this.tabsAfter;
    }

    @Mixin(CreativeModeTab.Builder.class)
    public static abstract class BuilderInject implements CreativeModeTabInjection.BuilderInjection {
        @Shadow
        private Identifier backgroundTexture;

        @Shadow
        public abstract CreativeModeTab.Builder backgroundTexture(Identifier resourceLocation);

        @Shadow
        public abstract CreativeModeTab.Builder displayItems(CreativeModeTab.DisplayItemsGenerator displayItemsGenerator);

        @Unique private static final Identifier CREATIVE_INVENTORY_TABS_IMAGE = Identifier.withDefaultNamespace("textures/gui/container/creative_inventory/tabs.png");
        @Unique private static final Identifier CREATIVE_ITEM_SEARCH_BACKGROUND = CreativeModeTab.createTextureLocation("item_search");

        @Unique @Nullable private Identifier spriteScrollerLocation;
        @Unique private boolean hasSearchBar = false;
        @Unique private int searchBarWidth = 89;
        @Unique private Identifier tabsImage = CREATIVE_INVENTORY_TABS_IMAGE;
        @Unique private int labelColor = 4210752;
        @Unique private int slotColor = -2130706433;
        @Unique private Function<CreativeModeTab.Builder, CreativeModeTab> tabFactory = CreativeModeTabInjection::create;
        @Unique private final List<Identifier> tabsBefore = new ArrayList<>();
        @Unique private final List<Identifier> tabsAfter = new ArrayList<>();

        @Unique
        private CreativeModeTab.Builder self() {
            return (CreativeModeTab.Builder) (Object) this;
        }

        @Override
        public CreativeModeTab.Builder withScrollBarSpriteLocation(Identifier background) {
            this.spriteScrollerLocation = background;
            return self();
        }

        @Override
        public CreativeModeTab.Builder withSearchBar() {
            this.hasSearchBar = true;
            if (this.backgroundTexture == CreativeModeTabAccessor.getDefaultBackground())
                return this.backgroundTexture(CREATIVE_ITEM_SEARCH_BACKGROUND);

            return self();
        }

        @Override
        public CreativeModeTab.Builder withSearchBar(int searchBarWidth) {
            this.searchBarWidth = searchBarWidth;
            return withSearchBar();
        }

        @Override
        public CreativeModeTab.Builder withTabsImage(Identifier tabsImage) {
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
            Stream.of(tabs).map(ResourceKey::identifier).forEach(this.tabsBefore::add);
            return self();
        }

        @Override
        public CreativeModeTab.Builder withTabsAfter(ResourceKey<CreativeModeTab>... tabs) {
            Stream.of(tabs).map(ResourceKey::identifier).forEach(this.tabsAfter::add);
            return self();
        }

        @Override
        public CreativeModeTab.Builder displayItems(Collection<? extends Holder<? extends ItemLike>> collection) {
            return this.displayItems((p, o) -> collection.stream()
                .map(Holder::value)
                .map(ItemLike::asItem)
                .filter(i -> i != Items.AIR)
                .filter(i -> i.isEnabled(p.enabledFeatures()))
                .forEach(o::accept)
            );
        }

        @Override
        public CreativeModeTab.Builder withTabsBefore(Identifier... tabs) {
            this.tabsBefore.addAll(List.of(tabs));
            return self();
        }

        @Override
        public CreativeModeTab.Builder withTabsAfter(Identifier... tabs) {
            this.tabsAfter.addAll(List.of(tabs));
            return self();
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
        public Identifier kilt$getTabsImage() {
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
        public List<Identifier> kilt$getTabsBefore() {
            return tabsBefore;
        }

        @Override
        public List<Identifier> kilt$getTabsAfter() {
            return tabsAfter;
        }

        @ModifyReturnValue(method = "type", at = @At("RETURN"))
        private CreativeModeTab.Builder kilt$addSearchBarForType(CreativeModeTab.Builder original, @Local(argsOnly = true) CreativeModeTab.Type type) {
            if (type == CreativeModeTab.Type.SEARCH)
                return original.withSearchBar();

            return original;
        }

        @Redirect(method = "build", at = @At(value = "NEW", target = "(Lnet/minecraft/world/item/CreativeModeTab$Row;ILnet/minecraft/world/item/CreativeModeTab$Type;Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;)Lnet/minecraft/world/item/CreativeModeTab;"))
        private CreativeModeTab kilt$useTabFactory(CreativeModeTab.Row row, int column, CreativeModeTab.Type type, Component displayName, Supplier iconGenerator, CreativeModeTab.DisplayItemsGenerator displayItemGenerator) {
            return tabFactory.apply((CreativeModeTab.Builder) (Object) this);
        }

        @Override
        public Identifier kilt$scrollerSpriteLocation() {
            return this.spriteScrollerLocation;
        }
    }
}
