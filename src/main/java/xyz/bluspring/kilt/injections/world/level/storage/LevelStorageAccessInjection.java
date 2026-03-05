package xyz.bluspring.kilt.injections.world.level.storage;

import xyz.bluspring.kilt.util.KiltHelper;

import java.nio.file.Path;

public interface LevelStorageAccessInjection {
    default void readAdditionalLevelSaveData(boolean fallback) {
        throw KiltHelper.createMixinException(LevelStorageAccessInjection.class, "readAdditionalLevelSaveData");
    }

    default Path getWorldDir() {
        throw KiltHelper.createMixinException(LevelStorageAccessInjection.class, "getWorldDir");
    }
}
