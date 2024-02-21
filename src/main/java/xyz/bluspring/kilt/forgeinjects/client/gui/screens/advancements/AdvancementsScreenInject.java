package xyz.bluspring.kilt.forgeinjects.client.gui.screens.advancements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
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
    @Shadow @Final public static int WINDOW_WIDTH;
    private static int tabPage, maxPages;

    protected AdvancementsScreenInject(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void kilt$addExtraAdvancementsTabs(CallbackInfo ci) {
        if (this.tabs.size() > AdvancementTabTypeInjection.MAX_TABS) {
            int guiLeft = (this.width - 252) / 2;
            int guiTop = (this.height - 140) / 2;

            addRenderableWidget(Button.builder(Component.literal("<"), b -> tabPage = Math.max(tabPage - 1, 0))
                    .pos(guiLeft, guiTop - 50)
                    .size(20, 20)
                    .build()
            );
            addRenderableWidget(Button.builder(Component.literal(">"), b -> tabPage = Math.max(tabPage + 1, maxPages))
                    .pos(guiLeft + WINDOW_WIDTH - 20, guiTop - 50)
                    .size(20, 20)
                    .build()
            );

            maxPages = this.tabs.size() / AdvancementTabTypeInjection.MAX_TABS;
        }
    }

    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;isMouseOver(IIDD)Z"))
    public boolean kilt$isPageEqualToTabPage(boolean original, @Local AdvancementTab tab) {
        return original && ((AdvancementTabInjection) tab).getPage() == tabPage;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementsScreen;renderInside(Lnet/minecraft/client/gui/GuiGraphics;IIII)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void kilt$drawPageCount(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci, int i, int j) {
        var page = Component.literal(String.format("%d / %d", tabPage + 1, maxPages + 1));
        var width = this.font.width(page);

        guiGraphics.drawString(this.font, page.getVisualOrderText(), i + (252 / 2) - (width / 2), j - 44, -1);
    }

    @WrapWithCondition(method = "renderWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;drawTab(Lnet/minecraft/client/gui/GuiGraphics;IIZ)V"))
    public boolean kilt$drawTabIfInPage(AdvancementTab instance, GuiGraphics guiGraphics, int offsetX, int offsetY, boolean isSelected) {
        return ((AdvancementTabInjection) instance).getPage() == tabPage;
    }

    @WrapWithCondition(method = "renderWindow", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;drawIcon(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    public boolean kilt$drawIconIfInPage(AdvancementTab instance, GuiGraphics guiGraphics, int offsetX, int offsetY) {
        return ((AdvancementTabInjection) instance).getPage() == tabPage;
    }

    @ModifyExpressionValue(method = "renderTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/advancements/AdvancementTab;isMouseOver(IIDD)Z"))
    public boolean kilt$isPageEqualToTabPageInTooltip(boolean original, @Local AdvancementTab tab) {
        return original && ((AdvancementTabInjection) tab).getPage() == tabPage;
    }
}
