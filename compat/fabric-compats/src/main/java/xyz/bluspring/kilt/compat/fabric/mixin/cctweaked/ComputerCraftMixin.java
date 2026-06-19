package xyz.bluspring.kilt.compat.fabric.mixin.cctweaked;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.shared.ComputerCraft;
import dan200.computercraft.shared.integration.CreateIntegration;
import dan200.computercraft.shared.peripheral.generic.methods.EnergyMethods;
import dan200.computercraft.shared.peripheral.generic.methods.FluidMethods;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.loader.KiltLoader;

@Mixin(ComputerCraft.class)
public abstract class ComputerCraftMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private static void kilt$addCreateCompat(CallbackInfo ci) {
        ComputerCraftAPI.registerGenericSource(new FluidMethods());
        ComputerCraftAPI.registerGenericSource(new EnergyMethods());

        ForgeComputerCraftAPI.registerGenericCapability(Capabilities.FluidHandler.BLOCK);
        ForgeComputerCraftAPI.registerGenericCapability(Capabilities.EnergyStorage.BLOCK);

        if (KiltLoader.Companion.getInstance().hasMod("create")) {
            CreateIntegration.setup();
        }
    }
}
