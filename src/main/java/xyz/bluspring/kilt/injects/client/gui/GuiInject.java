package xyz.bluspring.kilt.injects.client.gui;

import net.neoforged.neoforge.client.gui.GuiLayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.client.gui.GuiInjection;

import net.minecraft.client.gui.Gui;

@Mixin(Gui.class)
public abstract class GuiInject implements GuiInjection {
    @Unique private final GuiLayerManager layerManager = new GuiLayerManager();

    @Override
    public void initModdedOverlays() {
        this.layerManager.initModdedLayers();
    }
}
