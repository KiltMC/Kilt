// TRACKED HASH: 66858683859d22896e8fbc2be116d575c4c260c3
package xyz.bluspring.kilt.forgeinjects.client.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.ItemDecoratorHandler;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.extensions.IForgeGuiGraphics;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import xyz.bluspring.kilt.injections.client.gui.GuiGraphicsInjection;
import xyz.bluspring.kilt.injections.client.gui.screens.inventory.tooltip.TooltipRenderUtilInjection;
import xyz.bluspring.kilt.mixin.ClientTextTooltipAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsInject implements GuiGraphicsInjection, IForgeGuiGraphics {
    @Shadow @Deprecated protected abstract void flushIfUnmanaged();

    @Shadow @Final private PoseStack pose;

    @Shadow @Final private MultiBufferSource.BufferSource bufferSource;

    @Shadow
    private static IntIterator slices(int target, int total) {
        return null;
    }


    @Shadow public abstract void blit(ResourceLocation atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight);

    @Shadow public abstract void renderTooltip(Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY);

    @Shadow public abstract int guiWidth();

    @Shadow public abstract int guiHeight();

    @Shadow protected abstract void renderTooltipInternal(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner);

    @Override
    public int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        int i = font.drawInBatch(text, x, y, color, dropShadow, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        this.flushIfUnmanaged();
        return i;
    }

    @Override
    public int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        } else {
            int i = font.drawInBatch(text, x, y, color, dropShadow, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, font.isBidirectional());
            this.flushIfUnmanaged();
            return i;
        }
    }

    @Override
    public void blitRepeating(ResourceLocation atlasLocation, int x, int y, int width, int height, int uOffset, int vOffset, int sourceWidth, int sourceHeight, int textureWidth, int textureHeight) {
        int i = x;

        int j;
        for (IntIterator intIterator = slices(width, sourceWidth); intIterator.hasNext(); i += j) {
            j = intIterator.nextInt();
            int k = (sourceWidth - j) / 2;
            int l = y;

            int m;
            for(IntIterator intIterator2 = slices(height, sourceHeight); intIterator2.hasNext(); l += m) {
                m = intIterator2.nextInt();
                int n = (sourceHeight - m) / 2;
                this.blit(atlasLocation, i, l, uOffset + k, vOffset + n, j, m, textureWidth, textureHeight);
            }
        }
    }

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.AFTER))
    private void kilt$renderForgeDecorators(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        ItemDecoratorHandler.of(stack).render((GuiGraphics) (Object) this, font, stack, x, y);
    }

    @Unique private ItemStack tooltipStack = ItemStack.EMPTY;

    @Override
    public ItemStack kilt$getTooltipStack() {
        return tooltipStack;
    }

    @WrapOperation(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"))
    private void kilt$wrapTooltipRenderWithStack(GuiGraphics instance, Font font, List<Component> tooltipLines, Optional<TooltipComponent> visualTooltipComponent, int mouseX, int mouseY, Operation<Void> original, @Local(argsOnly = true) ItemStack stack) {
        this.tooltipStack = stack;
        original.call(instance, font, tooltipLines, visualTooltipComponent, mouseX, mouseY);
        this.tooltipStack = ItemStack.EMPTY;
    }

    @Override
    public void renderTooltip(Font font, List<Component> textComponents, Optional<TooltipComponent> tooltipComponent, ItemStack stack, int mouseX, int mouseY) {
        this.tooltipStack = stack;
        this.renderTooltip(font, textComponents, tooltipComponent, mouseX, mouseY);
        this.tooltipStack = ItemStack.EMPTY;
    }

    @ModifyArg(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltipInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)V"))
    private List<ClientTooltipComponent> kilt$gatherForgeTooltips(List<ClientTooltipComponent> original, @Local(argsOnly = true) List<Component> lines, @Local(argsOnly = true) Optional<TooltipComponent> visualTooltipComponent, @Local(argsOnly = true, ordinal = 0) int mouseX, @Local(argsOnly = true) Font font) {
        var forgeTooltips = new ArrayList<>(ForgeHooksClient.gatherTooltipComponents(this.tooltipStack, lines, visualTooltipComponent, mouseX, guiWidth(), guiHeight(), font));

        // Make a copy of the original list for modification, as we also want to be able to support other mods that may be injecting into here.
        var originalCopy = new ArrayList<>(original);

        // Remove the visual tooltip component located in the original, as it's already in the Forge tooltip.
        visualTooltipComponent.ifPresent(c -> originalCopy.remove(1));

        // Remove anything that is already in the Forge tooltip.
        var filtered = originalCopy.stream().filter(component -> {
            if (component instanceof ClientTextTooltip clientTextTooltip) {
                var text = ((ClientTextTooltipAccessor) clientTextTooltip).getText();

                return forgeTooltips.stream().noneMatch(e -> e instanceof ClientTextTooltip tooltip && ((ClientTextTooltipAccessor) tooltip).getText().equals(text));
            }

            return true;
        }).toList();

        forgeTooltips.addAll(filtered);

        return forgeTooltips;
    }

    @Override
    public void renderComponentTooltip(Font font, List<? extends FormattedText> tooltips, int mouseX, int mouseY, ItemStack stack) {
        this.tooltipStack = stack;
        List<ClientTooltipComponent> components = ForgeHooksClient.gatherTooltipComponents(stack, tooltips, mouseX, guiWidth(), guiHeight(), font);
        this.renderTooltipInternal(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
        this.tooltipStack = ItemStack.EMPTY;
    }

    // Kilt: We're using ThreadLocal rather than @Share here, because we enter a separate method (lambda) within renderTooltipInternal.
    @Unique private RenderTooltipEvent.Pre preEvent;
    @Unique private List<ClientTooltipComponent> kilt$components;

    @Inject(method = "renderTooltipInternal", at = @At("HEAD"))
    private void kilt$storeComponents(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, CallbackInfo ci) {
        this.kilt$components = components;
    }

    @Inject(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void kilt$callPreTooltipRenderEvent(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner tooltipPositioner, CallbackInfo ci) {
        preEvent = ForgeHooksClient.onRenderTooltipPre(this.tooltipStack, (GuiGraphics) (Object) this, mouseX, mouseY, guiWidth(), guiHeight(), components, font, tooltipPositioner);

        if (preEvent.isCanceled())
            ci.cancel();
    }

    @ModifyArg(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;getWidth(Lnet/minecraft/client/gui/Font;)I", ordinal = 0))
    private Font kilt$useForgeEventFont(Font font, @Local(argsOnly = true) Font argFont) {
        // prioritize font replaced in mixin
        if (argFont != font)
            return font;

        return preEvent.getFont();
    }

    @ModifyArgs(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"))
    private void kilt$useForgeEventPositions(Args args, @Local(argsOnly = true, ordinal = 0) int argMouseX, @Local(argsOnly = true, ordinal = 1) int argMouseY) {
        int mouseX = args.get(2);
        int mouseY = args.get(3);

        // prioritize positions replaced in mixin
        if (mouseX == argMouseX)
            args.set(2, preEvent.getX());

        if (mouseY == argMouseY)
            args.set(3, preEvent.getY());
    }

    @WrapOperation(method = "method_51743", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;renderTooltipBackground(Lnet/minecraft/client/gui/GuiGraphics;IIIII)V"))
    private void kilt$useForgeColorBackgroundModifyIfApplicable(GuiGraphics guiGraphics, int x, int y, int width, int height, int z, Operation<Void> original) {
        var colorEvent = ForgeHooksClient.onRenderTooltipColor(this.tooltipStack, guiGraphics, x, y, preEvent.getFont(), this.kilt$components);

        // prioritize rendering the original instead
        if (colorEvent.getBackgroundStart() == colorEvent.getOriginalBackgroundStart() &&
            colorEvent.getBackgroundEnd() == colorEvent.getOriginalBackgroundEnd() &&
            colorEvent.getBorderStart() == colorEvent.getOriginalBorderStart() &&
            colorEvent.getBorderEnd() == colorEvent.getOriginalBorderEnd()
        ) {
            original.call(guiGraphics, x, y, width, height, z);
            return;
        }

        TooltipRenderUtilInjection.renderTooltipBackground(guiGraphics, x, y, width, height, z, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd(), colorEvent.getBorderStart(), colorEvent.getBorderEnd());
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/CrashReportCategory;setDetail(Ljava/lang/String;Lnet/minecraft/CrashReportDetail;)Lnet/minecraft/CrashReportCategory;", ordinal = 1))
    private void kilt$addRegistryNameForItemToCrashReport(LivingEntity entity, Level level, ItemStack stack, int x, int y, int seed, int guiOffset, CallbackInfo ci, @Local CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Registry Name", () -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }

    @ModifyArg(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;renderText(Lnet/minecraft/client/gui/Font;IILorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V"))
    private Font kilt$useForgeEventFontInTextRender(Font font, @Local(argsOnly = true) Font argFont) {
        // prioritize font replaced in mixin
        if (argFont != font)
            return font;

        return preEvent.getFont();
    }

    @ModifyArg(method = "renderTooltipInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;renderImage(Lnet/minecraft/client/gui/Font;IILnet/minecraft/client/gui/GuiGraphics;)V"))
    private Font kilt$useForgeEventFontInImageRender(Font font, @Local(argsOnly = true) Font argFont) {
        // prioritize font replaced in mixin
        if (argFont != font)
            return font;

        return preEvent.getFont();
    }
}