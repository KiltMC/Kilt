// TRACKED HASH: 0abe6b86598c641126d968f40fad8310d1fff167
package xyz.bluspring.kilt.forgeinjects.client.gui.screens;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.gui.screens.ScreenInjection;
import xyz.bluspring.kilt.mixin.ScreenAccessor;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenInject implements ScreenInjection {
    @Shadow public int height;
    @Shadow public int width;
    @Shadow protected Font font;
    @Shadow @Nullable protected Minecraft minecraft;
    @Shadow @Final private List<GuiEventListener> children;
    @Shadow public List<Renderable> renderables;
    @Shadow @Final private List<NarratableEntry> narratables;

    @WrapOperation(method = "onClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void kilt$useForgeGuiLayerSystem(Minecraft instance, Screen guiScreen, Operation<Void> original) {
        ForgeHooksClient.kilt$popGuiLayer(instance, () -> original.call(instance, guiScreen));
    }

    @WrapOperation(method = {"init(Lnet/minecraft/client/Minecraft;II)V", "rebuildWidgets"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V"))
    private void kilt$tryCallForgeScreenInit(Screen instance, Operation<Void> original) {
        if (!MinecraftForge.EVENT_BUS.post(new ScreenEvent.Init.Pre((Screen) (Object) this, this.children, ((ScreenInjection) instance)::kilt$addEventWidget, ((ScreenAccessor) this)::callRemoveWidget))) {
            original.call(instance);
        }

        MinecraftForge.EVENT_BUS.post(new ScreenEvent.Init.Post((Screen) (Object) this, this.children, ((ScreenInjection) instance)::kilt$addEventWidget, ((ScreenAccessor) this)::callRemoveWidget));
    }

    @Inject(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fillGradient(IIIIII)V", shift = At.Shift.AFTER))
    private void kilt$callRenderBackgroundEvent(GuiGraphics guiGraphics, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered((Screen) (Object) this, guiGraphics));
    }

    @Inject(method = "renderDirtBackground", at = @At("TAIL"))
    private void kilt$callRenderDirtBackgroundEvent(GuiGraphics guiGraphics, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered((Screen) (Object) this, guiGraphics));
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    private void addEventWidget(GuiEventListener b) {
        if (b instanceof Renderable r)
            this.renderables.add(r);

        if (b instanceof NarratableEntry ne)
            this.narratables.add(ne);

        this.children.add(b);
    }

    // We don't want to make the above method public, so.
    @Override
    public void kilt$addEventWidget(GuiEventListener b) {
        this.addEventWidget(b);
    }
}