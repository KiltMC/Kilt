package xyz.bluspring.kilt.forgeinjects.client.renderer.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.item.ItemPropertiesInjection;

@Mixin(ItemProperties.class)
public abstract class ItemPropertiesInject {
    @CreateStatic
    private static ItemPropertyFunction registerGeneric(ResourceLocation name, ItemPropertyFunction property) {
        return ItemPropertiesInjection.registerGeneric(name, property);
    }

    @CreateStatic
    private static void register(Item item, ResourceLocation name, ItemPropertyFunction property) {
        ItemPropertiesInjection.register(item, name, property);
    }
}
