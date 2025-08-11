package xyz.bluspring.kilt.injects.client.gui.screens.multiplayer;

import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListInject {
    // Kilt: we don't really,, have a reason to draw ping info for Forge,,,,
}
