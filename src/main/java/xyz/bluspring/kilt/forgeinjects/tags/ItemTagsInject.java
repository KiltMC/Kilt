package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.tags.ItemTagsInjection;

@Mixin(ItemTags.class)
public class ItemTagsInject implements ItemTagsInjection {
    @CreateStatic
    private static TagKey<Item> create(ResourceLocation name) {
        return ItemTagsInjection.create(name);
    }
}
