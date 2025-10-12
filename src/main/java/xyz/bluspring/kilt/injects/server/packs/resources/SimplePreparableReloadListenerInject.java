package xyz.bluspring.kilt.injects.server.packs.resources;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.Extends;

@Extends(ContextAwareReloadListener.class)
@Mixin(SimplePreparableReloadListener.class)
public abstract class SimplePreparableReloadListenerInject {
}
