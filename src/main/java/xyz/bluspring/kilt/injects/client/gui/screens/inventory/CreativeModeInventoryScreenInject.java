// TRACKED HASH: 9169c1a016aa79fe41a22e24efb28b87a97545d4
package xyz.bluspring.kilt.injects.client.gui.screens.inventory;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.client.CreativeModeTabSearchRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.gui.screens.inventory.CreativeModeInventoryScreenInjection;
import xyz.bluspring.kilt.injections.world.inventory.SlotInjection;
import xyz.bluspring.kilt.injections.world.item.CreativeModeTabInjection;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

@Mixin(value = CreativeModeInventoryScreen.class, priority = 1009)
public abstract class CreativeModeInventoryScreenInject extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements CreativeModeInventoryScreenInjection {
    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private EditBox searchBox;

    public CreativeModeInventoryScreenInject(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "tryRebuildTabContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/SessionSearchTrees;updateCreativeTags(Ljava/util/List;)V", shift = At.Shift.AFTER))
    private void kilt$loadCreativeTabs(SessionSearchTrees searchTrees, FeatureFlagSet enabledFeatures, boolean hasPermissions, HolderLookup.Provider registries, CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTabs.allTabs().stream().filter(CreativeModeTabInjection::hasSearchBar).forEach(tab -> {
            List<ItemStack> list = List.copyOf(tab.getDisplayItems());
            searchTrees.updateCreativeTooltips(registries, list, CreativeModeTabSearchRegistry.getNameSearchKey(tab));
            searchTrees.updateCreativeTags(list, CreativeModeTabSearchRegistry.getTagSearchKey(tab));
        });
    }

    @WrapOperation(method = {"refreshCurrentTabContents", "charTyped", "keyPressed", }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;"))
    private CreativeModeTab.Type kilt$useSearchBarCheck(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (instance.hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;", ordinal = 4))
    private CreativeModeTab.Type kilt$useSearchBarCheck4(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (instance.hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "getTooltipFromContainerItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;", ordinal = 1))
    private CreativeModeTab.Type kilt$useSearchBarCheck5(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (instance.hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "getTooltipFromContainerItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;", ordinal = 2))
    private CreativeModeTab.Type kilt$useSearchBarCheck6(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (instance.hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @Inject(method = "refreshSearchResults", at = @At("HEAD"), cancellable = true)
    private void kilt$disableSearchRefreshIfNoBar(CallbackInfo ci) {
        if (!selectedTab.hasSearchBar()) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "refreshSearchResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/SessionSearchTrees;creativeTagSearch()Lnet/minecraft/client/searchtree/SearchTree;"))
    private SearchTree<ItemStack> kilt$useForgeTagSearch(SessionSearchTrees instance, Operation<SearchTree<ItemStack>> original) {
        var key = CreativeModeTabSearchRegistry.getTagSearchKey(selectedTab);

        if (key == null || key == SessionSearchTrees.CREATIVE_TAGS)
            return original.call(instance);

        return instance.creativeTagSearch(key);
    }

    @WrapOperation(method = "refreshSearchResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/SessionSearchTrees;creativeNameSearch()Lnet/minecraft/client/searchtree/SearchTree;"))
    private SearchTree<ItemStack> kilt$useForgeNameSearch(SessionSearchTrees instance, Operation<SearchTree<ItemStack>> original) {
        var key = CreativeModeTabSearchRegistry.getNameSearchKey(selectedTab);

        if (key == null || key == SessionSearchTrees.CREATIVE_NAMES)
            return original.call(instance);

        return instance.creativeNameSearch(key);
    }

    @Inject(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", shift = At.Shift.BEFORE))
    private void kilt$disableBlendOnLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        RenderSystem.disableBlend();
    }

    @ModifyExpressionValue(method = "renderLabels", at = @At(value = "CONSTANT", args = "intValue=4210752"))
    private int kilt$useSelectedTabLabelColor(int constant) {
        if (selectedTab.getLabelColor() == Integer.MIN_VALUE)
            return constant;

        return selectedTab.getLabelColor();
    }

    @Inject(method = "selectTab", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V", ordinal = 0))
    private void kilt$selectTabSlotColor(CreativeModeTab tab, CallbackInfo ci) {
        if (tab.getSlotColor() != Integer.MIN_VALUE)
            this.kilt$setSlotColor(tab.getSlotColor());
    }

    @Inject(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;refreshSearchResults()V", shift = At.Shift.BEFORE))
    private void kilt$setSearchBoxInfo(CreativeModeTab tab, CallbackInfo ci) {
        this.searchBox.setWidth(selectedTab.getSearchBarWidth());
        this.searchBox.setX(this.leftPos + (82 + 89) - this.searchBox.getWidth());
    }

//    Creative Tab sorting
//    @TargetHandler(
//            mixin = "net.fabricmc.fabric.mixin.itemgroup.client.CreativeInventoryScreenMixin",
//            name = "fabric_updateSelection"
//    )
//    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTabs;allTabs()Ljava/util/List;"))
//    private List<CreativeModeTab> kilt$allTabs() {
//        return CreativeModeTabRegistry.getSortedCreativeModeTabs();
//    }
//
//    @TargetHandler(
//            mixin = "net.fabricmc.fabric.mixin.itemgroup.client.CreativeInventoryScreenMixin",
//            name = "fabric_hasGroupForPage"
//    )
//    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTabs;tabs()Ljava/util/List;"))
//    private static List<CreativeModeTab> kilt$tabs() {
//        return CreativeModeTabRegistry.getSortedCreativeModeTabs();
//    }

    @Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
    public static class SlotWrapperInject implements SlotInjection {
        @Shadow @Final
        Slot target;

        @Override
        public int getSlotIndex() {
            return this.target.getSlotIndex();
        }

        @Override
        public boolean isSameInventory(Slot other) {
            return ((SlotInjection) this.target).isSameInventory(other);
        }

        @Override
        public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
            return this.target.setBackground(atlas, sprite);
        }
    }
}