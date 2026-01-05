package xyz.bluspring.kilt.compat.create.mixin.create_fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.AllFluids;
import io.github.fabricators_of_create.porting_lib.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AllFluids.class)
public class AllFluidsMixin {

    @ModifyExpressionValue(
            method = "<clinit>",
            at = @At(
                    value = "NEW",
                    target = "io/github/fabricators_of_create/porting_lib/fluids/FluidType"
            ),
            require = 0 // Older versions of create-fabric won't have FluidType
    )
    private static FluidType kilt$wrapFluid(FluidType fluidType) {
        return net.minecraftforge.fluids.FluidType.kilt$tryGetWrappingFluidType(fluidType);
    }

}
