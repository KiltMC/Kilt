package xyz.bluspring.kilt.forgeinjects.client.gui.screens.worldselection;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PresetEditor.class)
public interface PresetEditorInject {
    // Kilt: there's only a Deprecated tag in here.
}
