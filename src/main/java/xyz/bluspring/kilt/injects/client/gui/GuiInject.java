// TRACKED HASH: fd1859323d2b7b647915a5c458b0159a1f4e13b1
package xyz.bluspring.kilt.injects.client.gui;

import net.minecraft.client.gui.Gui;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.client.gui.GuiInjection;

@Mixin(Gui.class)
public abstract class GuiInject implements GuiInjection {
    // Kilt TODO: *[SCREAMS]*

    @Unique private final GuiLayerManager layerManager = new GuiLayerManager();

    @Unique public int leftHeight;
    @Unique public int rightHeight;
}