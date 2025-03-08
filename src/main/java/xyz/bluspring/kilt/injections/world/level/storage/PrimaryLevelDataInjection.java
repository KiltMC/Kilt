package xyz.bluspring.kilt.injections.world.level.storage;

import net.minecraft.world.level.storage.PrimaryLevelData;

public interface PrimaryLevelDataInjection {
    boolean hasConfirmedExperimentalWarning();
    PrimaryLevelData withConfirmedWarning(boolean confirmedWarning);
}
