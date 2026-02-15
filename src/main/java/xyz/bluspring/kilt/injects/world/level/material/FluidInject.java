// TRACKED HASH: 8052166e9529780ad90ee8b00eda7d0ee8ffc2ca
package xyz.bluspring.kilt.injects.world.level.material;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.extensions.IFluidExtension;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.level.material.FluidInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Implements(@Interface(iface = FluidInjection.class, prefix = "kilt$i$"))
@Mixin(Fluid.class)
public abstract class FluidInject implements IFluidExtension, FluidInjection {
    private FluidType forgeFluidType;

    @NotNull
    @Override
    public FluidType neo$getFluidType() {
        // Kilt: We pray that this works
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(this.getClass(), Fluid.class, "getFluidType", FluidType.class)) {
            return this.getFluidType();
        }

        if (forgeFluidType == null)
            forgeFluidType = CommonHooks.getVanillaFluidType((Fluid) (Object) this);

        return forgeFluidType;
    }

    public FluidType kilt$i$getFluidType() {
        return this.neo$getFluidType();
    }
}