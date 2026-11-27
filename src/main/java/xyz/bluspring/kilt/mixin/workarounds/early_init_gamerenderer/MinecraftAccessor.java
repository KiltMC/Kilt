package xyz.bluspring.kilt.mixin.workarounds.early_init_gamerenderer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Final @Mutable
    @Accessor("gameRenderer")
    void kilt$setGameRenderer(GameRenderer renderer);
}
