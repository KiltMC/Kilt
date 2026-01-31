package xyz.bluspring.kilt.injections.world.flag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Map;

public interface FeatureFlagRegistryInjection {
    default FeatureFlag getFlag(ResourceLocation id) {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "getFlag");
    }

    default Map<ResourceLocation, FeatureFlag> getAllFlags() {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "getAllFlags");
    }

    default boolean hasAnyModdedFlags() {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "hasAnyModdedFlags");
    }

    interface BuilderInjection {
        default FeatureFlag create(ResourceLocation id, boolean modded) {
            throw KiltHelper.createMixinException(FeatureFlagRegistryInjection.BuilderInjection.class, "create");
        }
    }
}
