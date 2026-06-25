package xyz.bluspring.kilt.injections.world.flag;

import java.util.Map;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlag;

public interface FeatureFlagRegistryInjection {
    default FeatureFlag getFlag(Identifier id) {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "getFlag");
    }

    default Map<Identifier, FeatureFlag> getAllFlags() {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "getAllFlags");
    }

    default boolean hasAnyModdedFlags() {
        throw KiltHelper.createMixinException(FeatureFlagInjection.class, "hasAnyModdedFlags");
    }

    interface BuilderInjection {
        default FeatureFlag create(Identifier id, boolean modded) {
            throw KiltHelper.createMixinException(FeatureFlagRegistryInjection.BuilderInjection.class, "create");
        }
    }
}
