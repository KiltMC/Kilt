package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.fabric.RegistryObject;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;

import java.util.Optional;

@IfModLoaded("registrate-fabric")
@SuppressWarnings("rawtypes")
@Mixin(FluidEntry.class)
public abstract class FluidEntryMixin extends RegistryEntry {

    @Unique
    private boolean kilt$isForge = false;

    public FluidEntryMixin(AbstractRegistrate<?> owner, RegistryObject delegate) {
        super(owner, delegate);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void kilt$init(AbstractRegistrate owner, RegistryObject delegate, CallbackInfo ci) {
        if (Kilt.Companion.getLoader().hasMod(owner.getModid())) {
            kilt$isForge = true;
        }
    }

    public FluidType getType() {
        return ((Fluid) get()).forge$getFluidType();
    }

    @Intrinsic
    public ForgeFlowingFluid getSource() {
        return (ForgeFlowingFluid) ((ForgeFlowingFluid) get()).getSource();
    }

    @WrapMethod(method = "getBucket")
    public <I extends Item> Optional<I> kilt$getBucket(Operation<Optional<I>> original) {
        if (kilt$isForge) { // Needed to avoid a ClassCastException.
            return Optional.ofNullable((I) ((ForgeFlowingFluid) get()).getBucket());
        }
        return original.call();
    }
}
