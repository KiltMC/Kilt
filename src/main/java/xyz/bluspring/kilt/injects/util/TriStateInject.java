package xyz.bluspring.kilt.injects.util;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.util.TriStateInjection;

import net.minecraft.util.TriState;

@Mixin(TriState.class)
public abstract class TriStateInject implements TriStateInjection {
    @Shadow @Final public static TriState TRUE;
    @Shadow @Final public static TriState DEFAULT;
    @Shadow @Final public static TriState FALSE;

    @Override
    public boolean isTrue() {
        return (Object) this == TRUE;
    }

    @Override
    public boolean isDefault() {
        return (Object) this == DEFAULT;
    }

    @Override
    public boolean isFalse() {
        return (Object) this == FALSE;
    }
}
