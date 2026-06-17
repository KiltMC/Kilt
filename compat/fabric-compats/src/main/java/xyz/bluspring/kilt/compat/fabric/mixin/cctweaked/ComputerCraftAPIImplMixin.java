package xyz.bluspring.kilt.compat.fabric.mixin.cctweaked;

import dan200.computercraft.api.detail.DetailRegistry;
import dan200.computercraft.impl.ComputerCraftAPIForgeService;
import dan200.computercraft.impl.ComputerCraftAPIImpl;
import dan200.computercraft.impl.Peripherals;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.compat.fabric.cctweaked.CapabilityLookup;
import xyz.bluspring.kilt.compat.fabric.cctweaked.WrappedDetailRegistry;

@Mixin(ComputerCraftAPIImpl.class)
public abstract class ComputerCraftAPIImplMixin implements ComputerCraftAPIForgeService {
    @Shadow
    @Final
    private DetailRegistry<StorageView<FluidVariant>> fluidDetails;
    @Unique
    private DetailRegistry<FluidStack> kilt$fluidDetailsNeoForge;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void kilt$wrapFluidDetails(CallbackInfo ci) {
        kilt$fluidDetailsNeoForge = new WrappedDetailRegistry(fluidDetails);
    }

    @Override
    public void registerGenericCapability(BlockCapability<?, Direction> capability) {
        Peripherals.addGenericLookup(new CapabilityLookup<>(capability));
    }

    @Override
    public DetailRegistry<FluidStack> getFluidStackDetailRegistry() {
        return kilt$fluidDetailsNeoForge;
    }
}
