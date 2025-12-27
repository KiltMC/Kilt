// TRACKED HASH: 8052166e9529780ad90ee8b00eda7d0ee8ffc2ca
package xyz.bluspring.kilt.forgeinjects.world.level.material;

import io.github.fabricators_of_create.porting_lib.fluids.extensions.FluidExtension;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeFluid;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import xyz.bluspring.kilt.injections.world.level.material.FluidInjection;

@Mixin(value = Fluid.class)
@Implements(value = { @Interface(iface = FluidInjection.class, prefix = "forge$i$"), @Interface(iface = FluidExtension.class, prefix = "kilt$porting_lib$")})
public abstract class FluidInject implements IForgeFluid {
    private FluidType forgeFluidType;

    @NotNull
    public FluidType forge$i$getFluidType() {
        if (forgeFluidType == null)
            forgeFluidType = ForgeHooks.getVanillaFluidType((Fluid) (Object) this);

        return forgeFluidType;
    }

    @Override
    public FluidType forge$getFluidType() {
        return this.forge$i$getFluidType();
    }

    @Intrinsic
    public io.github.fabricators_of_create.porting_lib.fluids.FluidType getFluidType() {
        throw new IllegalStateException();
    }

    @Unique private boolean kilt$cachedUsePortingLib = false;

    @Intrinsic(displace = true)
    public io.github.fabricators_of_create.porting_lib.fluids.FluidType kilt$porting_lib$getFluidType() {
        if (this.kilt$cachedUsePortingLib) {
            return this.forge$getFluidType();
        }

        try {
            return this.forge$getFluidType();
        } catch (Exception e) {
            this.kilt$cachedUsePortingLib = true;
            return this.getFluidType();
        }
    }
}