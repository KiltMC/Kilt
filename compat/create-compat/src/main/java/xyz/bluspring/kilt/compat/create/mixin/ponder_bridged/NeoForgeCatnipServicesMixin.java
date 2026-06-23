package xyz.bluspring.kilt.compat.create.mixin.ponder_bridged;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.createmod.catnip.platform.services.ModFluidHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.compat.create.ponder_bridged.WrappedModFluidHelper;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

@Pseudo
@Mixin(NeoForgeCatnipServices.class)
public abstract class NeoForgeCatnipServicesMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/createmod/catnip/platform/CatnipServices;FLUID_HELPER:Lnet/createmod/catnip/platform/services/ModFluidHelper;", opcode = Opcodes.GETSTATIC))
    private static ModFluidHelper<FluidStack> kilt$ponder_bridged$wrapFluidHelper(ModFluidHelper<FluidVariant> original) {
        return new WrappedModFluidHelper(original);
    }
}
