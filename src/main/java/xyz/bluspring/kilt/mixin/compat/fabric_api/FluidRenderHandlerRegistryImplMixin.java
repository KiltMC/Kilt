package xyz.bluspring.kilt.mixin.compat.fabric_api;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;
import net.minecraft.world.level.material.Fluid;

import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.workarounds.FluidHandlerWorkaround;

@Mixin(value = FluidRenderHandlerRegistryImpl.class, remap = false)
public class FluidRenderHandlerRegistryImplMixin {
    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    public FluidRenderHandler kilt$useForgeHandler(FluidRenderHandler original, @Local(argsOnly = true) Fluid fluid) {
        if (original != null)
            return original;

        var fluidType = fluid.forge$getFluidType();
        if (fluidType == ForgeMod.EMPTY_TYPE.get())
            return null;

        return FluidHandlerWorkaround.INSTANCE.getFluidRenderHandler(fluidType);
    }

    @ModifyReturnValue(method = "getOverride", at = @At("RETURN"))
    public FluidRenderHandler kilt$useForgeOverrides(FluidRenderHandler original, @Local(argsOnly = true) Fluid fluid) {
        if (original != null)
            return original;

        var fluidType = fluid.forge$getFluidType();
        if (fluidType == ForgeMod.EMPTY_TYPE.get())
            return null;

        return FluidHandlerWorkaround.INSTANCE.getFluidRenderHandler(fluidType);
    }
}
