// TRACKED HASH: b77abfa3d010ae58c80d911cff64c39f34dadbd6
package xyz.bluspring.kilt.injects.server.packs.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.server.packs.resources.SimpleJsonResourceReloadListenerInjection;

@Mixin(SimpleJsonResourceReloadListener.class)
public class SimpleJsonResourceReloadListenerInject implements SimpleJsonResourceReloadListenerInjection {
    @Shadow @Final private String directory;

    public ResourceLocation getPreparedPath(ResourceLocation rl) {
        return rl.withPath(this.directory + "/" + rl.getPath() + ".json");
    }
}
