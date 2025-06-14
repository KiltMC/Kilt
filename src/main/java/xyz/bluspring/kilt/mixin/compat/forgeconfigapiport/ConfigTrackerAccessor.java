package xyz.bluspring.kilt.mixin.compat.forgeconfigapiport;

import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ConfigTracker.class)
public interface ConfigTrackerAccessor {
    @Invoker
    void invokeTrackConfig(final ModConfig config);
}
