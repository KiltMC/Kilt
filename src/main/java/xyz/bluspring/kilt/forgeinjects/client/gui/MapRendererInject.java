package xyz.bluspring.kilt.forgeinjects.client.gui;

import net.minecraft.client.gui.MapRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MapRenderer.class)
public abstract class MapRendererInject {
    @Mixin(MapRenderer.MapInstance.class)
    public static abstract class MapInstanceInject {
        // Handed by Porting Lib
    }
}
