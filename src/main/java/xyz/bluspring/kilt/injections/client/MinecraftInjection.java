package xyz.bluspring.kilt.injections.client;

import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelResolver;

@FabricInjectedInterface(Minecraft.class)
public interface MinecraftInjection {
    default BlockModelResolver getBlockModelResolver() {
        throw KiltHelper.createMixinException(MinecraftInjection.class, "getBlockModelResolver");
    }
}
