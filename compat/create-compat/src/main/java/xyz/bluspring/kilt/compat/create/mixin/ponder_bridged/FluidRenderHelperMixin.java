package xyz.bluspring.kilt.compat.create.mixin.ponder_bridged;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.createmod.catnip.platform.services.ModFluidHelper;
import net.createmod.catnip.render.FluidRenderHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(FluidRenderHelper.class)
public abstract class FluidRenderHelperMixin {
    @ModifyExpressionValue(method = "renderFluidBox(Ljava/lang/Object;FFFFFFLcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack;IZZ)V", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/render/FluidRenderHelper;helper()Lnet/createmod/catnip/platform/services/ModFluidHelper;"))
    public ModFluidHelper<?> kilt$allowFluidStack(ModFluidHelper<?> original, @Local(argsOnly = true, name = "fluid") Object fluid) {
        if (fluid instanceof FluidStack)
            return NeoForgeCatnipServices.FLUID_HELPER;

        return original;
    }
}
