package xyz.bluspring.kilt.injects.server.packs.resources;

import io.github.fabricators_of_create.porting_lib.resources.conditions.ICondition;
import io.github.fabricators_of_create.porting_lib.resources.extensions.ContextAwareReloadListenerExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.workarounds.WrappedFabricConditionContext;
import xyz.bluspring.kilt.workarounds.WrappedNeoConditionContext;
import xyz.bluspring.kilt.workarounds.ContextAwareReloadListenerWorkaround;

@Extends(ContextAwareReloadListener.class)
@Mixin(SimplePreparableReloadListener.class)
public abstract class SimplePreparableReloadListenerInject implements ContextAwareReloadListenerWorkaround, ContextAwareReloadListenerExtension {
    // Kilt: Handle Porting Lib's contexts ourselves
    public ICondition.IContext port_lib$getContext() {
        return new WrappedNeoConditionContext(this.kilt$asContextAware().getContext());
    }

    @Override
    public void injectContext(ICondition.IContext context, HolderLookup.Provider registryLookup) {
        this.kilt$asContextAware().injectContext(new WrappedFabricConditionContext(context), registryLookup);
    }
}
