package xyz.bluspring.kilt.injects.client.renderer.state.level;

import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;
import net.neoforged.neoforge.client.renderstate.BaseRenderState;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.client.renderer.state.level.LevelRenderStateInjection;

import net.minecraft.client.renderer.state.level.LevelRenderState;

@Mixin(LevelRenderState.class)
@Extends(BaseRenderState.class)
public abstract class LevelRenderStateInject implements LevelRenderStateInjection {
    @Unique public @Nullable CustomSkyboxRenderer customSkyboxRenderer;
    @Unique public @Nullable CustomCloudsRenderer customCloudsRenderer;
    @Unique public @Nullable CustomWeatherEffectRenderer customWeatherEffectRenderer;

    @Inject(method = "reset", at = @At("TAIL"))
    private void kilt$resetCustomRenderers(CallbackInfo ci) {
        this.customSkyboxRenderer = null;
        this.customCloudsRenderer = null;
        this.customWeatherEffectRenderer = null;
        ((BaseRenderState) (Object) this).resetRenderData();
    }

    @Override
    public @Nullable CustomCloudsRenderer kilt$customCloudsRenderer() {
        return this.customCloudsRenderer;
    }

    @Override
    public void kilt$customCloudsRenderer(@Nullable CustomCloudsRenderer renderer) {
        this.customCloudsRenderer = renderer;
    }

    @Override
    public @Nullable CustomSkyboxRenderer kilt$customSkyboxRenderer() {
        return this.customSkyboxRenderer;
    }

    @Override
    public void kilt$customSkyboxRenderer(@Nullable CustomSkyboxRenderer renderer) {
        this.customSkyboxRenderer = renderer;
    }

    @Override
    public @Nullable CustomWeatherEffectRenderer kilt$customWeatherEffectRenderer() {
        return this.customWeatherEffectRenderer;
    }

    @Override
    public void kilt$customWeatherEffectRenderer(@Nullable CustomWeatherEffectRenderer renderer) {
        this.customWeatherEffectRenderer = renderer;
    }
}
