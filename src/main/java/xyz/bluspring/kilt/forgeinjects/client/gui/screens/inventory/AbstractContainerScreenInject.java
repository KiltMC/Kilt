package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenInject extends Screen {
    protected AbstractContainerScreenInject(Component component) {
        super(component);
    }

    // implemented ContainerScreen.Render.Background, ContainerScreen.Render.Foreground

    @Shadow public static void renderSlotHighlight(PoseStack poseStack, int x, int y, int blitOffset) {
        throw new IllegalStateException();
    }

    @Shadow @Nullable protected Slot hoveredSlot;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;
    @Shadow protected int imageHeight;
    private static final int defaultSlotColor = -2130706433;
    private static final AtomicInteger kilt$slotColor = new AtomicInteger(-2130706433);

    @CreateStatic
    private static void renderSlotHighlight(PoseStack poseStack, int x, int y, int blitOffset, int slotColor) {
        kilt$slotColor.set(slotColor);
        renderSlotHighlight(poseStack, x, y, blitOffset);
        kilt$slotColor.set(defaultSlotColor);
    }

    // I know it's better to use @ModifyConstant, but IntelliJ was yelling at me for it,
    // saying it couldn't find any method references with it, so...
    @Redirect(method = "renderSlotHighlight(Lcom/mojang/blaze3d/vertex/PoseStack;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;fillGradient(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIIII)V"))
    private static void kilt$useCustomSlotColor(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o) {
        fillGradient(poseStack, i, j, k, l, kilt$slotColor.get(), kilt$slotColor.get(), o);
    }

    @Inject(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderAndDecorateItem(Lnet/minecraft/world/item/ItemStack;II)V"))
    public void kilt$getItemFont(ItemStack stack, int x, int y, String altText, CallbackInfo ci, @Share("font") LocalRef<Font> font) {
        font.set(IClientItemExtensions.of(stack).getFont(stack, IClientItemExtensions.FontContext.ITEM_COUNT));

        if (font.get() == null)
            font.set(this.font);
    }

    @ModifyArg(method = "renderFloatingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"))
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
}
