package xyz.bluspring.kilt.injections.world.level.storage;

import java.nio.file.Path;

public interface LevelStorageAccessInjection {
    void readAdditionalLevelSaveData();
    Path getWorldDir();
}
