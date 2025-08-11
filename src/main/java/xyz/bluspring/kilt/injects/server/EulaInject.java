package xyz.bluspring.kilt.injects.server;

import net.minecraft.server.Eula;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Eula.class)
public abstract class EulaInject {
    // Kilt: don't need to implement this
}
