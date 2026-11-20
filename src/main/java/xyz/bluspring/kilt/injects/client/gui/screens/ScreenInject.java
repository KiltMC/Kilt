package xyz.bluspring.kilt.injects.client.gui.screens;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.gui.screens.ScreenInjection;
import xyz.bluspring.kilt.mixin.ScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(Screen.class)
public abstract class ScreenInject implements ScreenInjection {
    @Shadow @Final private List<GuiEventListener> children;
    @Shadow public List<Renderable> renderables;
    @Shadow @Final private List<NarratableEntry> narratables;

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;nextStratum()V"))
    private void kilt$renderBackgroundEvent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Background((Screen) (Object) this, graphics, mouseX, mouseY, a));
    }

    @Inject(method = "extractRenderStateWithTooltipAndSubtitles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", shift = At.Shift.AFTER))
    private void kilt$renderForegroundEvent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        if (!((Object) this instanceof AbstractContainerScreen<?>)) {
            NeoForge.EVENT_BUS.post(new ScreenEvent.Render.Foreground((Screen) (Object) this, graphics, mouseX, mouseY, a));
        }
    }

    @WrapOperation(method = "onClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void kilt$useForgeGuiLayerSystem(Minecraft instance, Screen screen, Operation<Void> original) {
        instance.kilt$popGuiLayer(() -> original.call(instance, screen));
    }

    @WrapOperation(method = {"init(II)V", "rebuildWidgets"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V"))
    private void kilt$tryCallForgeScreenInit(Screen instance, Operation<Void> original) {
        if (!NeoForge.EVENT_BUS.post(new ScreenEvent.Init.Pre((Screen) (Object) this, this.children, ((ScreenInjection) instance)::kilt$addEventWidget, ((ScreenAccessor) this)::callRemoveWidget)).isCanceled()) {
            original.call(instance);

            // Kilt TODO: setInitialFocus is unhandled
        }

        NeoForge.EVENT_BUS.post(new ScreenEvent.Init.Post((Screen) (Object) this, this.children, ((ScreenInjection) instance)::kilt$addEventWidget, ((ScreenAccessor) this)::callRemoveWidget));
    }

    @Accessor
    @Override
    public abstract Minecraft getMinecraft();

    @Unique
    public void kilt$addEventWidget(GuiEventListener b) {
        if (b instanceof Renderable r)
            this.renderables.add(r);
        if (b instanceof NarratableEntry ne)
            this.narratables.add(ne);

        this.children.add(b);
    }
}
