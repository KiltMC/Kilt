package xyz.bluspring.kilt.forgeinjects.server.packs.resources;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.server.packs.resources.SimpleJsonResourceReloadListenerInjection;

@Mixin(SimpleJsonResourceReloadListener.class)
public abstract class SimpleJsonResourceReloadListenerInject implements SimpleJsonResourceReloadListenerInjection {
    @Shadow @Final private String directory;

    @Override
    public ResourceLocation getPreparedPath(ResourceLocation rl) {
        return new ResourceLocation(rl.getNamespace(), this.directory + "/" + rl.getPath() + ".json");
    }
}
