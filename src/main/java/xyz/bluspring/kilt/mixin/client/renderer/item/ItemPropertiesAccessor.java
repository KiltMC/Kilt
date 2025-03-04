package xyz.bluspring.kilt.mixin.client.renderer.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ItemProperties.class)
public interface ItemPropertiesAccessor {
    @Accessor("GENERIC_PROPERTIES")
    static Map<ResourceLocation, ItemPropertyFunction> getGenericProperties() {
        throw new UnsupportedOperationException();
    }

    @Accessor("PROPERTIES")
    static Map<Item, Map<ResourceLocation, ItemPropertyFunction>> getProperties() {
        throw new UnsupportedOperationException();
    }
}
