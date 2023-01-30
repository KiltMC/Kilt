package xyz.bluspring.kilt.injections.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface ItemTagsInjection {
    static TagKey<Item> create(ResourceLocation name) {
        return TagKey.create(Registry.ITEM_REGISTRY, name);
    }
}
