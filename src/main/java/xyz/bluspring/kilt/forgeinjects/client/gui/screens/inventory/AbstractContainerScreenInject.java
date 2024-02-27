// TRACKED HASH: c7da1c1b82a8d0a88d3f2a674ea41109be948d6c
package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.gui.screens.inventory.AbstractContainerScreenInjection;
import xyz.bluspring.kilt.injections.world.inventory.SlotInjection;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenInject extends Screen implements AbstractContainerScreenInjection {
    protected AbstractContainerScreenInject(Component component) {
        super(component);
    }

    // implemented ContainerScreen.Render.Background, ContainerScreen.Render.Foreground

    @Shadow @Nullable protected Slot hoveredSlot;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    @Shadow
    public static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset) {
    }

    private static final int defaultSlotColor = -2130706433;
    private static final AtomicInteger kilt$slotColor = new AtomicInteger(-2130706433);

    @CreateStatic
    private static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset, int slotColor) {
        kilt$slotColor.set(slotColor);
        renderSlotHighlight(guiGraphics, x, y, blitOffset);
        kilt$slotColor.set(defaultSlotColor);
    }

    @ModifyExpressionValue(method = "renderSlotHighlight", at = @At(value = "CONSTANT", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(Lnet/minecraft/client/renderer/RenderType;IIIIIII)V", args = "intValue=-2130706433"))
    private static int kilt$useCustomSlotColor(int original) {
        if (original != defaultSlotColor)
            return original;

        return kilt$slotColor.get();
    }

    @Inject(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    public void kilt$getItemFont(GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text, CallbackInfo ci, @Share("font") LocalRef<Font> font) {
        font.set(IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT));

        if (font.get() == null)
            font.set(this.font);
    }

    @ModifyArg(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
    public Font kilt$useForgeItemFont(Font oldFont, @Share("font") LocalRef<Font> newFont) {
        return newFont.get();
    }

    @ModifyVariable(method = "mouseClicked", at = @At("STORE"), ordinal = 1)
    public boolean kilt$ensureNoSlotInEmptySpaceWhenClicked(boolean hasClicked, @Local Slot slot) {
        if (slot != null)
            return false;
        else
            return hasClicked;
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"))
    public void kilt$callParentRelease(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        super.mouseReleased(mouseX, mouseY, button);
    }

    @ModifyVariable(method = "mouseReleased", at = @At("STORE"))
    public boolean kilt$ensureNoSlotInEmptySpaceWhenReleased(boolean hasClicked, @Local Slot slot) {
        if (slot != null)
            return false;
        else
            return hasClicked;
    }

    @ModifyExpressionValue(method = "mouseReleased", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;container:Lnet/minecraft/world/Container;", ordinal = 0))
    private Container kilt$forceContainerMatch(Container original, @Local(ordinal = 0) Slot slot) {
        return slot.container;
    }

    @WrapWithCondition(method = "mouseReleased", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ClickType;)V", ordinal = 0))
    private boolean kilt$useForgeInventoryCheck(AbstractContainerScreen<?> instance, Slot slot, int slotId, int mouseButton, ClickType type, @Local(ordinal = 0, index = 0) Slot slot2) {
        return ((SlotInjection) slot).isSameInventory(slot2);
    }

    @Nullable
    public Slot getSlotUnderMouse() {
        return this.hoveredSlot;
    }

    public int getGuiLeft() {
        return this.leftPos;
    }

    public int getGuiTop() {
        return this.topPos;
    }

    public int getXSize() {
        return this.imageWidth;
    }

    public int getYSize() {
        return this.imageHeight;
    }

    protected int slotColor = -2130706433;
    public int getSlotColor(int index) {
        return slotColor;
    }

    @Override
    public void kilt$setSlotColor(int slotColor) {
        this.slotColor = slotColor;
    }
}