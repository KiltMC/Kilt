package xyz.bluspring.kilt.injections.item.crafting;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;

import java.util.Arrays;
import java.util.Collection;

public interface IngredientInjection {
    default boolean isVanilla() {
        throw new IllegalStateException();
    }

    default IIngredientSerializer<? extends Ingredient> getSerializer() {
        if (!isVanilla())
            throw new IllegalStateException();

        return VanillaIngredientSerializer.INSTANCE;
    }

    default boolean isSimple() {
        return true;
    }

    static Ingredient merge(Collection<Ingredient> parts) {
        return Ingredient.fromValues(parts.stream().flatMap((it) -> Arrays.stream(it.values)));
    }
}
