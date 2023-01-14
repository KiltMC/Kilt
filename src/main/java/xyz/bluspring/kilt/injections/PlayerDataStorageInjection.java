package xyz.bluspring.kilt.injections;

import java.io.File;

public interface PlayerDataStorageInjection {
    default File getPlayerDataFolder() {
        throw new RuntimeException("mixin, why didn't you add this");
    }
}