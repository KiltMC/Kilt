package xyz.bluspring.kilt.injections.client.renderer.state.level;

import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.BaseRenderStateWorkaround;

public interface LevelRenderStateInjection extends BaseRenderStateWorkaround {
    default @Nullable CustomSkyboxRenderer kilt$customSkyboxRenderer() {
        throw KiltHelper.createMixinException(LevelRenderStateInjection.class, "kilt$customSkyboxRenderer");
    }

    default void kilt$customSkyboxRenderer(@Nullable CustomSkyboxRenderer renderer) {
        throw KiltHelper.createMixinException(LevelRenderStateInjection.class, "kilt$customSkyboxRenderer");
    }

    default @Nullable CustomCloudsRenderer kilt$customCloudsRenderer() {
        throw KiltHelper.createMixinException(LevelRenderStateInjection.class, "kilt$customCloudsRenderer");
    }

    default void kilt$customCloudsRenderer(@Nullable CustomCloudsRenderer renderer) {
        throw KiltHelper.createMixinException(LevelRenderStateInjection.class, "kilt$customCloudsRenderer");
    }

    default @Nullable CustomWeatherEffectRenderer kilt$customWeatherEffectRenderer() {
        throw KiltHelper.createMixinException(LevelRenderStateInjection.class, "kilt$customWeatherEffectRenderer");
    }

    default void kilt$customWeatherEffectRenderer(@Nullable CustomWeatherEffectRenderer renderer) {
        throw KiltHelper.createMixinException(LevelRenderStateInjection.class, "kilt$customWeatherEffectRenderer");
    }
}
