package xyz.bluspring.kilt.injections.client;

import net.minecraft.client.color.item.ItemColors;

public interface MinecraftInjection {
    default ItemColors getItemColors() {
        throw new IllegalStateException();
    }

    default float getPartialTick() {
        throw new IllegalStateException();
    }
}
