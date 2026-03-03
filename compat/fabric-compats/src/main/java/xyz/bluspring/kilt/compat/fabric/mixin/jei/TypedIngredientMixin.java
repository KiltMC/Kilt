package xyz.bluspring.kilt.compat.fabric.mixin.jei;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.library.ingredients.TypedIngredient;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.util.forge.fluid.FluidTransferUtils;

@IfModLoaded("jei")
@Mixin(TypedIngredient.class)
public class TypedIngredientMixin<T> {

    @WrapMethod(method = "getIngredient", remap = false)
    public T getIngredient(Operation<T> original) {
        var ingredient = original.call();
        if (ingredient instanceof IJeiFluidIngredient fluid) {
            var stack = new FluidStack(
                    fluid.getFluid(),
                    FluidTransferUtils.toMillibuckets(fluid.getAmount()),
                    fluid.getTag().orElse(null)
            );
            //noinspection unchecked
            return (T) stack;
        }
        return ingredient;
    }

}
