package xyz.bluspring.kilt.injections.world.level.saveddata;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.helpers.RecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public interface SavedDataTypeInjection<T extends SavedData> {
    static <T extends SavedData> SavedDataType<T> create(Identifier id, Factory<T> factory, Factory<Codec<T>> codecFactory) {
        return create(id, factory, codecFactory, null);
    }

    static <T extends SavedData> SavedDataType<T> create(Identifier id, Supplier<T> factory, Codec<T> codec) {
        return new SavedDataType<>(id, factory, codec, null);
    }

    static <T extends SavedData> SavedDataType<T> create(Identifier id, Factory<T> factory, Factory<Codec<T>> codecFactory, @Nullable DataFixTypes dataFixType) {
        var type = new SavedDataType<>(id, () -> factory.create(null), null, dataFixType);

        // kill me
        RecordMixinRefMaps.EXTENDED_SAVED_DATA_TYPE.put(type, new RecordMixinRefMaps.ExtendedSavedDataType<>(factory, codecFactory));

        return type;
    }

    default Factory<T> factory() {
        throw KiltHelper.createMixinException(SavedDataTypeInjection.class, "factory");
    }

    default Factory<Codec<T>> codecFactory() {
        throw KiltHelper.createMixinException(SavedDataTypeInjection.class, "codecFactory");
    }

    @FunctionalInterface
    interface Factory<T> {
        T create(@Nullable ServerLevel level);
    }
}
