package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.minecraft.world.inventory.BeaconMenu;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeaconMenu.class)
public abstract class BeaconMenuInject {
    // Kilt: this seems like a bugfix that is out of scope for Kilt.
}
