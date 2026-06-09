package xyz.bluspring.kilt.mixin.workarounds.fluid_type;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.fabricators_of_create.porting_lib.fluids.PortingLibFluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.StupidWorkarounds;
import xyz.bluspring.kilt.workarounds.FabricFluidTypeExtensions;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(IClientFluidTypeExtensions.class)
public interface IClientFluidTypeExtensionsMixin {
    @WrapOperation(method = {"of(Lnet/minecraft/world/level/material/Fluid;)Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;"}, at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;of(Lnet/neoforged/neoforge/fluids/FluidType;)Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;"))
    private static IClientFluidTypeExtensions kilt$tryHandleFabricFluidTypes(FluidType type, Operation<IClientFluidTypeExtensions> original, @Local(argsOnly = true) Fluid fluid) {
        if (type.kilt$isWrapped) {
            return StupidWorkarounds.kilt$fabricFluidExtensions.computeIfAbsent(type, $ -> new FabricFluidTypeExtensions(fluid));
        }

        return original.call(type);
    }

    @WrapOperation(method = {"of(Lnet/minecraft/world/level/material/FluidState;)Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;"}, at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;of(Lnet/neoforged/neoforge/fluids/FluidType;)Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;"))
    private static IClientFluidTypeExtensions kilt$tryHandleFabricFluidTypes(FluidType type, Operation<IClientFluidTypeExtensions> original, @Local(argsOnly = true) FluidState state) {
        if (type.kilt$isWrapped) {
            return StupidWorkarounds.kilt$fabricFluidExtensions.computeIfAbsent(type, $ -> new FabricFluidTypeExtensions(state.getType()));
        }

        return original.call(type);
    }

    @Inject(method = "of(Lnet/neoforged/neoforge/fluids/FluidType;)Lnet/neoforged/neoforge/client/extensions/common/IClientFluidTypeExtensions;", at = @At("HEAD"), cancellable = true)
    private static void kilt$tryHandleFabricFluidTypes(FluidType type, CallbackInfoReturnable<IClientFluidTypeExtensions> cir) {
        if (type.kilt$isWrapped) {
            if (StupidWorkarounds.kilt$fabricFluidExtensions.containsKey(type)) {
                cir.setReturnValue(StupidWorkarounds.kilt$fabricFluidExtensions.get(type));
                return;
            }

            // Kilt: Try to derive the fluid type
            if (type.kilt$wrapped != null) {
                var key = PortingLibFluids.FLUID_TYPES.getKey(type.kilt$wrapped);
                var fluid = BuiltInRegistries.FLUID.getOptional(key).orElse(null);

                if (fluid != null) {
                    var fabricExt = new FabricFluidTypeExtensions(fluid);
                    StupidWorkarounds.kilt$fabricFluidExtensions.put(type, fabricExt);

                    cir.setReturnValue(fabricExt);
                    StupidWorkarounds.kilt$fabricFluidExtensions.put(type, fabricExt);
                    return;
                }
            }
        }
    }
}
