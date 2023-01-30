package xyz.bluspring.kilt.injections.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface ItemPredicateInjection {
    Map<ResourceLocation, Function<JsonObject, ItemPredicate>> customPredicates = new HashMap<>();

    static void register(ResourceLocation name, Function<JsonObject, ItemPredicate> deserializer) {
        customPredicates.put(name, deserializer);
    }

    static Map<ResourceLocation, Function<JsonObject, ItemPredicate>> getPredicates() {
        return customPredicates;
    }
}
