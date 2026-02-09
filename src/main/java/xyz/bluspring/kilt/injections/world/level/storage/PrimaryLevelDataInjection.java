package xyz.bluspring.kilt.injections.world.level.storage;

import net.minecraft.world.level.storage.PrimaryLevelData;
import xyz.bluspring.kilt.util.KiltHelper;

public interface PrimaryLevelDataInjection {
    default boolean hasConfirmedExperimentalWarning() {
        throw KiltHelper.createMixinException(PrimaryLevelDataInjection.class, "hasConfirmedExperimentalWarning");
    }

    default PrimaryLevelData withConfirmedWarning(boolean confirmedWarning) {
        throw KiltHelper.createMixinException(PrimaryLevelDataInjection.class, "withConfirmedWarning");
    }
}
