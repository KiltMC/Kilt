package xyz.bluspring.kilt.injects.data;

import net.minecraft.data.Main;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Main.class)
public abstract class MainInject {
    // Kilt: we shouldn't have to mess with this honestly
}
