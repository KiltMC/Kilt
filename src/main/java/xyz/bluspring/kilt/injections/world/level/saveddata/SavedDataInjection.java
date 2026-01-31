package xyz.bluspring.kilt.injections.world.level.saveddata;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface SavedDataInjection {
    interface FactoryInjection {
        static <T extends SavedData> SavedData.Factory<T> create(Supplier<T> constructor, BiFunction<CompoundTag, HolderLookup.Provider, T> deserializer) {
            return new SavedData.Factory<>(constructor, deserializer, null);
        }
    }
}
