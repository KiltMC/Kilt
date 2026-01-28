// TRACKED HASH: 8052166e9529780ad90ee8b00eda7d0ee8ffc2ca
package xyz.bluspring.kilt.injects.world.level.material;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.extensions.IFluidExtension;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Fluid.class)
public abstract class FluidInject implements IFluidExtension {
    private FluidType forgeFluidType;

    @NotNull
    @Override
    public FluidType neo$getFluidType() {
        if (forgeFluidType == null)
            forgeFluidType = CommonHooks.getVanillaFluidType((Fluid) (Object) this);

        return forgeFluidType;
    }
}