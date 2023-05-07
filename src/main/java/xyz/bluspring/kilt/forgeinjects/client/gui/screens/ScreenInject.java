package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.workarounds.ForgeHooksClientWorkaround;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@Mixin(Screen.class)
public abstract class ScreenInject {
    @Shadow protected abstract void renderTooltipInternal(PoseStack poseStack, List<ClientTooltipComponent> list, int i, int j);

    @Shadow public abstract void renderComponentTooltip(PoseStack poseStack, List<Component> list, int i, int j);

    @Shadow public abstract void renderTooltip(PoseStack poseStack, List<Component> components, Optional<TooltipComponent> tooltipComponent, int x, int y);

    @Shadow public int height;
    @Shadow public int width;
    @Shadow protected Font font;

    @Shadow public abstract void renderTooltip(PoseStack poseStack, List<? extends FormattedCharSequence> tooltips, int mouseX, int mouseY);

    @Shadow @org.jetbrains.annotations.Nullable protected Minecraft minecraft;
    private Font tooltipFont = null;
    private ItemStack tooltipStack = ItemStack.EMPTY;

    @Inject(at = @At("HEAD"), method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V")
    public void kilt$setTooltipStack(PoseStack poseStack, ItemStack itemStack, int i, int j, CallbackInfo ci) {
        tooltipStack = itemStack;
    }

    @Inject(at = @At("TAIL"), method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V")
    public void kilt$unsetTooltipStack(PoseStack poseStack, ItemStack itemStack, int i, int j, CallbackInfo ci) {
        tooltipStack = ItemStack.EMPTY;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderTooltipInternal(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V"), method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;Ljava/util/Optional;II)V")
    public void kilt$gatherTooltipComponents(Screen instance, PoseStack poseStack, List<ClientTooltipComponent> list, int i, int j, PoseStack poseStack2, List<Component> list2, Optional<TooltipComponent> optional, int i2, int j2) {
        var list3 = ForgeHooksClientWorkaround.gatherTooltipComponents(tooltipStack, list2, optional, i, this.width, this.height, tooltipFont, this.font);
        this.renderTooltipInternal(poseStack, list3, i, j);
    }

    public void renderTooltip(PoseStack poseStack, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y, ItemStack stack) {
        this.renderTooltip(poseStack, textComponents, tooltipComponent, x, y, null, stack);
    }

    public void renderTooltip(PoseStack poseStack, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y, @Nullable Font font) {
        this.renderTooltip(poseStack, textComponents, tooltipComponent, x, y, font, ItemStack.EMPTY);
    }

    public void renderTooltip(PoseStack poseStack, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, int x, int y, @Nullable Font font, ItemStack stack) {
        this.tooltipFont = font;
        this.tooltipStack = stack;
        this.renderTooltip(poseStack, textComponents, tooltipComponent, x, y);
        this.tooltipFont = null;
        this.tooltipStack = ItemStack.EMPTY;
    }

    @Inject(method = "renderComponentTooltip", at = @At("HEAD"), cancellable = true)
    public void kilt$gatherForgeTooltips(PoseStack poseStack, List<Component> tooltips, int mouseX, int mouseY, CallbackInfo ci) {
        var components = ForgeHooksClientWorkaround.gatherTooltipComponents(this.tooltipStack, tooltips, mouseX, width, height, this.tooltipFont, this.font);
        if (components != null && !components.isEmpty()) {
            ci.cancel();

            this.renderTooltipInternal(poseStack, components, mouseX, mouseY);
        }
    }

    public void renderComponentTooltip(PoseStack poseStack, List<? extends FormattedText> tooltips, int mouseX, int mouseY, ItemStack stack) {
        this.renderComponentTooltip(poseStack, tooltips, mouseX, mouseY, null, stack);
    }

    public void renderComponentTooltip(PoseStack poseStack, List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nullable Font font) {
        this.renderComponentTooltip(poseStack, tooltips, mouseX, mouseY, font, ItemStack.EMPTY);
    }

    public void renderComponentTooltip(PoseStack poseStack, List<? extends FormattedText> tooltips, int mouseX, int mouseY, @Nullable Font font, ItemStack stack) {
        this.tooltipFont = font;
        this.tooltipStack = stack;

        var components = ForgeHooksClientWorkaround.gatherTooltipComponents(this.tooltipStack, tooltips, mouseX, width, height, this.tooltipFont, this.font);
        this.renderTooltipInternal(poseStack, components, mouseX, mouseY);

        this.tooltipFont = null;
        this.tooltipStack = ItemStack.EMPTY;
    }

    public void renderTooltip(PoseStack poseStack, List<? extends FormattedCharSequence> lines, int x, int y, Font font) {
        this.tooltipFont = font;
        this.renderTooltip(poseStack, lines, x, y);
        this.tooltipFont = null;
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }
}
