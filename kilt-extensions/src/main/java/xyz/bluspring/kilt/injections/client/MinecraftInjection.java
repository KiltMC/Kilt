package xyz.bluspring.kilt.injections.client;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.searchtree.SearchRegistry;

public interface MinecraftInjection {
    default ItemColors getItemColors() {
        throw new IllegalStateException();
    }
    default float getPartialTick() {
        throw new IllegalStateException();
    }
    default SearchRegistry getSearchTreeManager() {
        throw new IllegalStateException();
    }
}
