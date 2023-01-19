package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraftforge.common.extensions.IForgeHolderSet;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(HolderSet.Named.class)
public class HolderSetNamedInject<T> implements IForgeHolderSet<T> {
    private final List<Runnable> invalidationCallbacks = new ArrayList<>();

    @Override
    public void addInvalidationListener(@NotNull Runnable runnable) {
        invalidationCallbacks.add(runnable);
    }

    @Inject(at = @At("TAIL"), method = "bind")
    public void kilt$invalidateCallbacks(List<Holder<T>> list, CallbackInfo ci) {
        for (Runnable invalidationCallback : this.invalidationCallbacks) {
            invalidationCallback.run();
        }
    }
}
