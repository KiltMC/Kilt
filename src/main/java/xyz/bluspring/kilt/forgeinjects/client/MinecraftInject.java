package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.client.*;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.gui.ClientTooltipComponentManager;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.textures.TextureAtlasSpriteLoaderManager;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.MinecraftInjection;

@Mixin(Minecraft.class)
public class MinecraftInject implements MinecraftInjection {
    @Shadow @Final private ItemColors itemColors;

    @Shadow @Final private SearchRegistry searchRegistry;
    @Shadow @Final private ReloadableResourceManager resourceManager;
    @Shadow @Final public Options options;
    @Shadow private volatile boolean pause;
    @Shadow private float pausePartialTick;
    @Shadow @Final private Timer timer;
    @Unique
    private float realPartialTick;

    @Override
    public float getPartialTick() {
        return realPartialTick;
    }

    @Override
    public ItemColors getItemColors() {
        return this.itemColors;
    }

    @Override
    public SearchRegistry getSearchTreeManager() {
        return this.searchRegistry;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateVsync(Z)V", shift = At.Shift.BEFORE), method = "<init>")
    public void kilt$initializeForge(GameConfig gameConfig, CallbackInfo ci) {
        ForgeGameTestHooks.registerGametests();
        ModLoader.get().postEvent(new RegisterClientReloadListenersEvent(this.resourceManager));
        ModLoader.get().postEvent(new EntityRenderersEvent.RegisterLayerDefinitions());
        ModLoader.get().postEvent(new EntityRenderersEvent.RegisterRenderers());
        TextureAtlasSpriteLoaderManager.init();
        ClientTooltipComponentManager.init();
        EntitySpectatorShaderManager.init();
        ForgeHooksClient.onRegisterKeyMappings(this.options);
        RecipeBookManager.init();
        GuiOverlayManager.init();
        DimensionSpecialEffectsManager.init();
        NamedRenderTypeManager.init();
        ColorResolverManager.init();
        ItemDecoratorHandler.init();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.BEFORE), method = "runTick")
    public void kilt$setPartialTicks(boolean bl, CallbackInfo ci) {
        realPartialTick = this.pause ? this.pausePartialTick : this.timer.partialTick;
        ForgeEventFactory.onRenderTickStart(realPartialTick);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.BY, by = 3), method = "runTick")
    public void kilt$callRenderTickEnd(boolean bl, CallbackInfo ci) {
        ForgeEventFactory.onRenderTickEnd(realPartialTick);
    }
}
