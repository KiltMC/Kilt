package xyz.bluspring.kilt.injections.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(Minecraft.class)
public interface MinecraftInjection {
    default ItemColors getItemColors() {
        throw new IllegalStateException();
    }
}
