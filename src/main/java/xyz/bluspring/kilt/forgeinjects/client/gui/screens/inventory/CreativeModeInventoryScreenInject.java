package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.CreativeModeTabSearchRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.gui.screens.inventory.AbstractContainerScreenInjection;
import xyz.bluspring.kilt.injections.client.gui.screens.inventory.CreativeModeInventoryScreenInjection;
import xyz.bluspring.kilt.injections.world.inventory.SlotInjection;
import xyz.bluspring.kilt.injections.world.item.CreativeModeTabInjection;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenInject extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements CreativeModeInventoryScreenInjection {
    @Shadow private static CreativeModeTab selectedTab;
    @Shadow private EditBox searchBox;

    public CreativeModeInventoryScreenInject(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @WrapOperation(method = "refreshCurrentTabContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;"))
    private CreativeModeTab.Type kilt$useSearchBarCheck(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (((CreativeModeTabInjection) instance).hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;"))
    private CreativeModeTab.Type kilt$useSearchBarCheck2(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (((CreativeModeTabInjection) instance).hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;"))
    private CreativeModeTab.Type kilt$useSearchBarCheck3(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (((CreativeModeTabInjection) instance).hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;", ordinal = 4))
    private CreativeModeTab.Type kilt$useSearchBarCheck4(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (((CreativeModeTabInjection) instance).hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "getTooltipFromContainerItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;", ordinal = 1))
    private CreativeModeTab.Type kilt$useSearchBarCheck5(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (((CreativeModeTabInjection) instance).hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "getTooltipFromContainerItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getType()Lnet/minecraft/world/item/CreativeModeTab$Type;", ordinal = 2))
    private CreativeModeTab.Type kilt$useSearchBarCheck6(CreativeModeTab instance, Operation<CreativeModeTab.Type> original) {
        if (((CreativeModeTabInjection) instance).hasSearchBar())
            return CreativeModeTab.Type.SEARCH;
        else
            return original.call(instance);
    }

    @WrapOperation(method = "renderBg", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$useSelectedTabBackground(String location, Operation<ResourceLocation> original) {
        return ((CreativeModeTabInjection) selectedTab).getBackgroundLocation();
    }

    @WrapOperation(method = "renderBg", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;CREATIVE_TABS_LOCATION:Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$useSelectedTabImage(Operation<ResourceLocation> original) {
        return ((CreativeModeTabInjection) selectedTab).getTabsImage();
    }

    @Inject(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V", shift = At.Shift.BEFORE))
    private void kilt$enableBlendOnTabButton(GuiGraphics guiGraphics, CreativeModeTab creativeModeTab, CallbackInfo ci) {
        RenderSystem.enableBlend();
    }

    @WrapOperation(method = "renderTabButton", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;CREATIVE_TABS_LOCATION:Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$useSelectedTabImageOnTabButton(Operation<ResourceLocation> original) {
        return ((CreativeModeTabInjection) selectedTab).getTabsImage();
    }

    @Inject(method = "refreshSearchResults", at = @At("HEAD"), cancellable = true)
    private void kilt$disableSearchRefreshIfNoBar(CallbackInfo ci) {
        if (!((CreativeModeTabInjection) selectedTab).hasSearchBar()) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "refreshSearchResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getSearchTree(Lnet/minecraft/client/searchtree/SearchRegistry$Key;)Lnet/minecraft/client/searchtree/SearchTree;", ordinal = 0))
    private SearchRegistry.Key<ItemStack> kilt$useForgeTagSearch(SearchRegistry.Key<ItemStack> key) {
        return CreativeModeTabSearchRegistry.getTagSearchKey(selectedTab);
    }

    @ModifyArg(method = "refreshSearchResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getSearchTree(Lnet/minecraft/client/searchtree/SearchRegistry$Key;)Lnet/minecraft/client/searchtree/SearchTree;", ordinal = 1))
    private SearchRegistry.Key<ItemStack> kilt$useForgeNameSearch(SearchRegistry.Key<ItemStack> key) {
        return CreativeModeTabSearchRegistry.getNameSearchKey(selectedTab);
    }

    @Inject(method = "renderLabels", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", shift = At.Shift.BEFORE))
    private void kilt$disableBlendOnLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        RenderSystem.disableBlend();
    }

    @ModifyConstant(method = "renderLabels", constant = @Constant(intValue = 4210752))
    private int kilt$useSelectedTabLabelColor(int constant) {
        return ((CreativeModeTabInjection) selectedTab).getLabelColor();
    }

    @Inject(method = "selectTab", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V", ordinal = 0))
    private void kilt$selectTabSlotColor(CreativeModeTab tab, CallbackInfo ci) {
        ((AbstractContainerScreenInjection) this).kilt$setSlotColor(((CreativeModeTabInjection) tab).getSlotColor());
    }

    @Inject(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;refreshSearchResults()V", shift = At.Shift.BEFORE))
    private void kilt$setSearchBoxInfo(CreativeModeTab tab, CallbackInfo ci) {
        this.searchBox.setWidth(((CreativeModeTabInjection) selectedTab).getSearchBarWidth());
        this.searchBox.setX(this.leftPos + (82 + 89) - this.searchBox.getWidth());
    }

    @Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper")
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
