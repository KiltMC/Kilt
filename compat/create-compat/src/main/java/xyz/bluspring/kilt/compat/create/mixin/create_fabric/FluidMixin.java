package xyz.bluspring.kilt.compat.create.mixin.create_fabric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(value = Fluid.class, priority = 10000)
public class FluidMixin {
//    @ModifyReturnValue(method = "getFluidType", at = @At("RETURN"), cancellable = true)
//    private void kilt$bridgeFluidTypes(CallbackInfoReturnable<FluidType> cir) {
//
//    }
    @Dynamic("getFluidType is injected by Porting Lib and inherited from FluidExtension")
    @ModifyReturnValue(method = "getFluidType", at = @At("RETURN"))
    private static FluidType kilt$wrapFabricFluidType(FluidType original) {
        return FluidType.kilt$tryGetWrappingFluidType((io.github.fabricators_of_create.porting_lib.fluids.FluidType) (Object) original);
    }
}
