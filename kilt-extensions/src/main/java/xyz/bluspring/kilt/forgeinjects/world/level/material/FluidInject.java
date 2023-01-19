package xyz.bluspring.kilt.forgeinjects.world.level.material;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeFluid;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Fluid.class)
public abstract class FluidInject implements IForgeFluid {
    private FluidType forgeFluidType;

    @NotNull
    @Override
    public FluidType getFluidType() {
        if (forgeFluidType == null)
            forgeFluidType = ForgeHooks.getVanillaFluidType((Fluid) (Object) this);

        return forgeFluidType;
    }
}
