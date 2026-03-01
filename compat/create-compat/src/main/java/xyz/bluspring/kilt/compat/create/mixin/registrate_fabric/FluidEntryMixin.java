package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.fabric.RegistryObject;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("rawtypes")
@Mixin(FluidEntry.class)
public abstract class FluidEntryMixin extends RegistryEntry {
    public FluidEntryMixin(AbstractRegistrate<?> owner, RegistryObject delegate) {
        super(owner, delegate);
    }

    public FluidType getType() {
        return ((Fluid) get()).forge$getFluidType();
    }
}
