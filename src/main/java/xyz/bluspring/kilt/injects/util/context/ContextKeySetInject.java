package xyz.bluspring.kilt.injects.util.context;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.util.context.ContextKeySetInjection;

import net.minecraft.util.context.ContextKeySet;

@Mixin(ContextKeySet.class)
public abstract class ContextKeySetInject {
    @CreateStatic
    private static final ContextKeySet EMPTY = ContextKeySetInjection.EMPTY;
}
