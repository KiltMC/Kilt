package xyz.bluspring.kilt.helpers;


import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.injections.world.level.saveddata.SavedDataTypeInjection;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.loot.LootDataType;

// hey, you! have you ever wanted to mixin to records and add a field into them?
// don't! use this instead! some JVMs actually don't support that behavior and instead return invalid/default values!
public interface RecordMixinRefMaps {
    Map<LootDataType<?>, ExtendedLootDataType<?>> EXTENDED_LOOT_DATA_TYPE = create();
    Map<MobEffect.AttributeTemplate, ExtendedAttributeTemplate> EXTENDED_ATTRIBUTE_TEMPLATE = create();
    Map<SavedDataType<?>, ExtendedSavedDataType<?>> EXTENDED_SAVED_DATA_TYPE = create();
    Map<RegistryDataLoader.RegistryData<?>, ExtendedRegistryData<?>> EXTENDED_REGISTRY_DATA = create();
    Map<TicketType, ExtendedTicketType> EXTENDED_TICKET_TYPE = create();

    record ExtendedLootDataType<T>(@Nullable T defaultValue, Codec<Optional<T>> conditionalCodec, BiConsumer<T, Identifier> idSetter) {}
    record ExtendedAttributeTemplate(@Nullable Int2DoubleFunction curve) {}
    record ExtendedSavedDataType<T extends SavedData>(SavedDataTypeInjection.Factory<T> factory, SavedDataTypeInjection.Factory<Codec<T>> codecFactory) {}
    record ExtendedRegistryData<T>(Consumer<RegistryBuilder<T>> registryBuilderConsumer) {}
    record ExtendedTicketType(boolean forceNaturalSpawning) {}

    static <K, V> Map<K, V> create() {
        return new WeakHashMap<>();
    }
}
