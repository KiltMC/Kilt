package xyz.bluspring.kilt.injections.world.level.storage.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public interface LootDataTypeInjection<T> {
    void kilt$setTriTopDeserializerGetter(BiFunction<Gson, String, TriFunction<ResourceLocation, JsonElement, ResourceManager, Optional<T>>> deserializer);
    Optional<T> deserialize(ResourceLocation id, JsonElement json, ResourceManager resourceManager);

    Optional<T> kilt$tryDeserialize(ResourceLocation id, JsonElement json, ResourceManager resourceManager, Supplier<Optional<T>> original);
}
