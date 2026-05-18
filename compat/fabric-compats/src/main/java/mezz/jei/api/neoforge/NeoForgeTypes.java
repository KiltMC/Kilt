package mezz.jei.api.neoforge;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public final class NeoForgeTypes {

    @SuppressWarnings("unchecked")
    public static final IIngredientTypeWithSubtypes<Fluid, FluidStack> FLUID_STACK = (IIngredientTypeWithSubtypes<Fluid, FluidStack>) (Object) FabricTypes.FLUID_STACK;

    private NeoForgeTypes() {}
}