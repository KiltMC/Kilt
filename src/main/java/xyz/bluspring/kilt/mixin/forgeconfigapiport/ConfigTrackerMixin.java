package xyz.bluspring.kilt.mixin.forgeconfigapiport;

import net.minecraftforge.fml.config.ConfigTracker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ConfigTracker.class, remap = false)
public class ConfigTrackerMixin {
}
