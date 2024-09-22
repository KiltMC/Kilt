package xyz.bluspring.kilt.injections.client.renderer.item;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import xyz.bluspring.kilt.mixin.client.renderer.item.ItemPropertiesAccessor;

public interface ItemPropertiesInjection {
    static ItemPropertyFunction registerGeneric(ResourceLocation name, ItemPropertyFunction property) {
        ItemPropertiesAccessor.getGenericProperties().put(name, property);
        return property;
    }

    static void register(Item item, ResourceLocation name, ItemPropertyFunction property) {
        ItemPropertiesAccessor.getProperties().computeIfAbsent(item, $ -> Maps.newHashMap())
            .put(name, property);
    }
}
