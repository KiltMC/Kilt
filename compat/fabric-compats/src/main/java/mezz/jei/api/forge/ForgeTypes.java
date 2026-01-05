package mezz.jei.api.forge;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public final class ForgeTypes {

    @SuppressWarnings("unchecked")
    public static final IIngredientTypeWithSubtypes<Fluid, FluidStack> FLUID_STACK = (IIngredientTypeWithSubtypes<Fluid, FluidStack>) (Object) FabricTypes.FLUID_STACK;

    private ForgeTypes() {}
}