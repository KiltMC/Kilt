package xyz.bluspring.kilt.compat.fabric.mixin.jei;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.library.ingredients.TypedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.util.neoforge.fluid.FluidTransferUtils;

@IfModLoaded("jei")
@Mixin(TypedIngredient.class)
public class TypedIngredientMixin<T> {

    @WrapMethod(method = "getIngredient", remap = false)
    public T getIngredient(Operation<T> original) {
        var ingredient = original.call();
        if (ingredient instanceof IJeiFluidIngredient fluid) {
            var stack = new FluidStack(
                    fluid.getFluidVariant().getRegistryEntry(),
                    FluidTransferUtils.toMillibuckets(fluid.getAmount()),
                    fluid.getFluidVariant().getComponents()
            );
            //noinspection unchecked
            return (T) stack;
        }
        return ingredient;
    }

}
