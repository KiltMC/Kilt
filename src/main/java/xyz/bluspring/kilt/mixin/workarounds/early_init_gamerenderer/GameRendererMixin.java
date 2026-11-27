package xyz.bluspring.kilt.mixin.workarounds.early_init_gamerenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(value = GameRenderer.class, priority = 850)
public abstract class GameRendererMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void kilt$earlyInitGameRenderer(CallbackInfo ci) { // Kilt: This is mostly to work around the fact that some NeoForge mods rely on GameRenderer having already been initialized for PiP stuff.
        if (Minecraft.getInstance().gameRenderer == null) {
            ((MinecraftAccessor) Minecraft.getInstance()).kilt$setGameRenderer((GameRenderer) (Object) this);
        }
    }
}
