package xyz.bluspring.kilt.injections.client;

import xyz.bluspring.kilt.processor.FabricInjectedInterface;

import net.minecraft.client.Minecraft;

@FabricInjectedInterface(Minecraft.class)
public interface MinecraftInjection {
}
