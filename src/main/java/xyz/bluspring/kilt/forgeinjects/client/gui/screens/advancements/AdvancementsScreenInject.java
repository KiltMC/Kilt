package xyz.bluspring.kilt.forgeinjects.client.gui.screens.advancements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import xyz.bluspring.kilt.injections.client.gui.screens.advancements.AdvancementTabInjection;
import xyz.bluspring.kilt.injections.client.gui.screens.advancements.AdvancementTabTypeInjection;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenInject extends Screen {
    @Shadow @Final private Map<Advancement, AdvancementTab> tabs;
    private static int tabPage, maxPages;

    protected AdvancementsScreenInject(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void kilt$addExtraAdvancementsTabs(CallbackInfo ci) {
        if (this.tabs.size() > AdvancementTabTypeInjection.MAX_TABS) {
            int guiLeft = (this.width - 252) / 2;
            int guiTop = (this.height - 140) / 2;

            addRenderableWidget(new Button(guiLeft, guiTop - 50, 20, 20, Component.literal("<"), b -> tabPage = Math.max(tabPage - 1, 0)));
            addRenderableWidget(new Button(guiLeft + 252 - 20, guiTop - 50, 20, 20, Component.literal(">"), b -> tabPage = Math.min(tabPage + 1, maxPages)));
            maxPages = this.tabs.size() / AdvancementTabTypeInjection.MAX_TABS;
        }
    }

    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;isMouseOver(IIDD)Z"))
    public boolean kilt$isPageEqualToTabPage(boolean original, @Local AdvancementTab tab) {
        return original && ((AdvancementTabInjection) tab).getPage() == tabPage;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;renderInside(Lcom/mojang/blaze3d/vertex/PoseStack;IIII)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$drawPageCount(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo ci, int i, int j) {
        var page = Component.literal(String.format("%d / %d", tabPage + 1, maxPages + 1));
        var width = this.font.width(page);

        this.font.drawShadow(poseStack, page.getVisualOrderText(), i + (252 / 2) - (width / 2), j - 44, -1);
    }

    @WrapWithCondition(method = "renderWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;drawTab(Lcom/mojang/blaze3d/vertex/PoseStack;IIZ)V"))
    public boolean kilt$drawTabIfInPage(AdvancementTab instance, PoseStack poseStack, int x, int y, boolean isSelected) {
        return ((AdvancementTabInjection) instance).getPage() == tabPage;
    }

    @WrapWithCondition(method = "renderWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;drawIcon(IILnet/minecraft/client/renderer/entity/ItemRenderer;)V"))
    public boolean kilt$drawIconIfInPage(AdvancementTab instance, int x, int y, ItemRenderer itemRenderer) {
        return ((AdvancementTabInjection) instance).getPage() == tabPage;
    }

    @ModifyExpressionValue(method = "renderTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;isMouseOver(IIDD)Z"))
    public boolean kilt$isPageEqualToTabPageInTooltip(boolean original, @Local AdvancementTab tab) {
        return original && ((AdvancementTabInjection) tab).getPage() == tabPage;
    }
}
