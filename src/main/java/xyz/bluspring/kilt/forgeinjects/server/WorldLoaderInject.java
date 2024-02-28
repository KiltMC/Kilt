package xyz.bluspring.kilt.forgeinjects.server;

import net.minecraft.server.WorldLoader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldLoader.class)
public class WorldLoaderInject {
    /**
     * The inject for this is here,
     * See: {@link xyz.bluspring.kilt.mixin.fabric_api.DynamicRegistriesMixin}
     */
}
