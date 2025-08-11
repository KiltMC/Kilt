package xyz.bluspring.kilt.injections.item.crafting;

import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IIngredientSerializer;
import net.neoforged.neoforge.common.crafting.VanillaIngredientSerializer;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public interface IngredientInjection {
    AtomicInteger INVALIDATION_COUNTER = new AtomicInteger();
    static void invalidateAll() {
        INVALIDATION_COUNTER.incrementAndGet();
    }

    default boolean isVanilla() {
        throw new IllegalStateException();
    }

    default boolean checkInvalidation() {
        throw new IllegalStateException();
    }

    default void markValid() {
        throw new IllegalStateException();
    }

    default void invalidate() {
        throw new IllegalStateException();
    }

    default IIngredientSerializer<? extends Ingredient> getSerializer() {
        throw new IllegalStateException();
    }

    default boolean isSimple() {
        return true;
    }

    static Ingredient merge(Collection<Ingredient> parts) {
        return Ingredient.fromValues(parts.stream().flatMap((it) -> Arrays.stream(it.values)));
    }
}
