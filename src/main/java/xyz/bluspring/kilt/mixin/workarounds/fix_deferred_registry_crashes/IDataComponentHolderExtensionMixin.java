package xyz.bluspring.kilt.mixin.workarounds.fix_deferred_registry_crashes;

import java.util.function.Supplier;

import net.neoforged.neoforge.common.extensions.IDataComponentHolderExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.component.DataComponentType;

@Mixin(IDataComponentHolderExtension.class)
public interface IDataComponentHolderExtensionMixin {
    @Inject(method = "has", at = @At("HEAD"), cancellable = true)
    private void kilt$avoidCrashWithUnboundHolders(Supplier<? extends DataComponentType<?>> type, CallbackInfoReturnable<Boolean> cir) {
        if (type instanceof DeferredHolder<?, ?> holder && !holder.isBound())
            cir.setReturnValue(false);
    }
}
