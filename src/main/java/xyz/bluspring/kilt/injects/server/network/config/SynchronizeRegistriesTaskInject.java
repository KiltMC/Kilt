package xyz.bluspring.kilt.injects.server.network.config;

import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SynchronizeRegistriesTask.class)
public abstract class SynchronizeRegistriesTaskInject {
    // Kilt: don't think we need this?
}
