package xyz.bluspring.kilt.helpers;


import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;

// hey, you! have you ever wanted to mixin to records and add a field into them?
// don't! use this instead! some JVMs actually don't support that behavior and instead return invalid/default values!
public class RecordMixinRefMaps {
    private RecordMixinRefMaps() {}

    public static final Map<LootDataType<?>, ExtendedLootDataType<?>> EXTENDED_LOOT_DATA_TYPE = create();

    public record ExtendedLootDataType<T>(@Nullable T defaultValue, Codec<Optional<T>> conditionalCodec, BiConsumer<T, ResourceLocation> idSetter) {}

    private static <K, V> Map<K, V> create() {
        return new WeakHashMap<>();
    }
}
