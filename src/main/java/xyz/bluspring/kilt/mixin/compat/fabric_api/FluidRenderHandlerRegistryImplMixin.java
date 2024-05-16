package xyz.bluspring.kilt.mixin.compat.fabric_api;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.workarounds.FluidHandlerWorkaround;

@Mixin(value = FluidRenderHandlerRegistryImpl.class, remap = false)
public class FluidRenderHandlerRegistryImplMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void kilt$useForgeHandler(Fluid fluid, CallbackInfoReturnable<FluidRenderHandler> cir) {
        try {
            var fluidType = fluid.getFluidType();

            cir.setReturnValue(FluidHandlerWorkaround.INSTANCE.getFluidRenderHandler(fluidType));
        } catch (Exception ignored) {}
    }

    @Inject(method = "getOverride", at = @At("HEAD"), cancellable = true)
    public void kilt$useForgeOverrides(Fluid fluid, CallbackInfoReturnable<FluidRenderHandler> cir) {
        try {
            var fluidType = fluid.getFluidType();

            cir.setReturnValue(FluidHandlerWorkaround.INSTANCE.getFluidRenderHandler(fluidType));
        } catch (Exception ignored) {}
    }
}
