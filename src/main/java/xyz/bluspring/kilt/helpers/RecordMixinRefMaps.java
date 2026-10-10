package xyz.bluspring.kilt.helpers;


import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.injections.world.level.saveddata.SavedDataTypeInjection;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.loot.LootDataType;

// hey, you! have you ever wanted to mixin to records and add a field into them?
// don't! use this instead! some JVMs actually don't support that behavior and instead return invalid/default values!
public class RecordMixinRefMaps {
    private RecordMixinRefMaps() {}

    public static final Map<LootDataType<?>, ExtendedLootDataType<?>> EXTENDED_LOOT_DATA_TYPE = create();
    public static final Map<MobEffect.AttributeTemplate, ExtendedAttributeTemplate> EXTENDED_ATTRIBUTE_TEMPLATE = create();
    public static final Map<SavedDataType<?>, ExtendedSavedDataType<?>> EXTENDED_SAVED_DATA_TYPE = create();

    public record ExtendedLootDataType<T>(@Nullable T defaultValue, Codec<Optional<T>> conditionalCodec, BiConsumer<T, Identifier> idSetter) {}
    public record ExtendedAttributeTemplate(@Nullable Int2DoubleFunction curve) {}
    public record ExtendedSavedDataType<T extends SavedData>(SavedDataTypeInjection.Factory<T> factory, SavedDataTypeInjection.Factory<Codec<T>> codecFactory) {}

    private static <K, V> Map<K, V> create() {
        return new WeakHashMap<>();
    }
}
