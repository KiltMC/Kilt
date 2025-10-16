// TRACKED HASH: c7da1c1b82a8d0a88d3f2a674ea41109be948d6c
package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
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
import xyz.bluspring.kilt.injections.client.gui.GuiGraphicsInjection;
import xyz.bluspring.kilt.injections.client.gui.screens.inventory.AbstractContainerScreenInjection;
import xyz.bluspring.kilt.injections.world.inventory.SlotInjection;
import xyz.bluspring.kilt.mixin.client.gui.screens.inventory.AbstractContainerScreenAccessor;

import java.util.List;
import java.util.Optional;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenInject extends Screen implements AbstractContainerScreenInjection {
    protected AbstractContainerScreenInject(Component component) {
        super(component);
    }

    @Shadow @Nullable protected Slot hoveredSlot;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V", shift = At.Shift.AFTER))
    private void kilt$callRenderBackgroundEvent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background((AbstractContainerScreen<?>) (Object) this, guiGraphics, mouseX, mouseY));
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V", shift = At.Shift.AFTER))
    private void kilt$callRenderForegroundEvent(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Foreground((AbstractContainerScreen<?>) (Object) this, guiGraphics, mouseX, mouseY));
    }

    @CreateStatic
    private static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y, int blitOffset, int slotColor) {
        AbstractContainerScreenInjection.kilt$slotColor.set(slotColor);
        // this is called: workarounds.
        AbstractContainerScreenAccessor.invokeRenderSlotHighlight(guiGraphics, x, y, blitOffset);
        AbstractContainerScreenInjection.kilt$slotColor.set(defaultSlotColor);
    }

    @ModifyExpressionValue(method = "renderSlotHighlight", at = @At(value = "CONSTANT", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(Lnet/minecraft/client/renderer/RenderType;IIIIIII)V", args = "intValue=-2130706433"))
    private static int kilt$useCustomSlotColor(int original) {
        if (original != AbstractContainerScreenInjection.defaultSlotColor)
            return original;

        return AbstractContainerScreenInjection.kilt$slotColor.get();
    }

    @WrapOperation(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void kilt$wrapTooltipRenderWithStack(GuiGraphics instance, Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY, Operation<Void> original, @Local ItemStack stack) {
        ((GuiGraphicsInjection) instance).kilt$setTooltipStack(stack);
        original.call(instance, font, tooltipLines, visualTooltipComponent, mouseX, mouseY);
        ((GuiGraphicsInjection) instance).kilt$setTooltipStack(ItemStack.EMPTY);
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