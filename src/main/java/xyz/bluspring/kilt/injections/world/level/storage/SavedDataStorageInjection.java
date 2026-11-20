package xyz.bluspring.kilt.injections.world.level.storage;

import java.nio.file.Path;

import com.mojang.datafixers.DataFixer;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.SavedDataStorage;

public interface SavedDataStorageInjection {
    default void kilt$setLevel(ServerLevel level) {
        throw KiltHelper.createMixinException(SavedDataStorageInjection.class, "kilt$setLevel");
    }

    static SavedDataStorage create(@Nullable ServerLevel level, Path dataFolder, DataFixer fixerUpper, HolderLookup.Provider registries) {
        var storage = new SavedDataStorage(dataFolder, fixerUpper, registries);
        storage.kilt$setLevel(level);
        return storage;
    }
}
