// TRACKED HASH: 73e5f001c405920db0c51467eaae6d0f5e817e2b
package xyz.bluspring.kilt.forgeinjects.server;

import net.minecraft.server.WorldLoader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldLoader.class)
public class WorldLoaderInject {
    /**
     * This is already handled by Fabric API.
     */
}