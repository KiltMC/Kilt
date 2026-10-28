package xyz.bluspring.kilt.injections.world.item.crafting;

import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;

@FabricInjectedInterface(Ingredient.class)
public interface IngredientInjection extends FabricIngredient {
    MapCodec<Ingredient> MAP_CODEC_NONEMPTY = CraftingHelper.makeIngredientMapCodec();
    Codec<List<Ingredient>> LIST_CODEC = MAP_CODEC_NONEMPTY.codec().listOf();
    Codec<List<Ingredient>> LIST_CODEC_NONEMPTY = LIST_CODEC.validate(list -> list.isEmpty() ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined") : DataResult.success(list));

    static Ingredient create(ICustomIngredient customIngredient) {
        /*Ingredient ingredient = new Ingredient(new Ingredient.Value[0]);
        ingredient.kilt$setCustomIngredient(customIngredient);

        return ingredient;*/

        return customIngredient.toVanilla();
    }

    default boolean hasNoItems() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "hasNoItems");
    }

    default Ingredient.Value[] getValues() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "getValues");
    }

    default boolean isSimple() {
        return this.requiresTesting();
    }

    @Override
    default ICustomIngredient getCustomIngredient() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "getCustomIngredient");
    }

    /*default ICustomIngredient neoforge$getCustomIngredient() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "neoforge$getCustomIngredient");
    }*/

    default boolean isCustom() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "isCustom");
    }

    /*default void kilt$setCustomIngredient(ICustomIngredient customIngredient) {
        throw KiltHelper.createMixinException(IngredientInjection.class, "kilt$setCustomIngredient");
    }*/

    interface ValueInjection {
        // Kilt: Assuming and hoping mods aren't changing this out of being a map for some weird reason.
        MapCodec<Ingredient.Value> MAP_CODEC = NeoForgeExtraCodecs.xor(MapCodec.assumeMapUnsafe(Ingredient.ItemValue.CODEC), MapCodec.assumeMapUnsafe(Ingredient.TagValue.CODEC))
            .xmap((either) -> either.map(Function.identity(), Function.identity()), value -> {
                if (value instanceof Ingredient.TagValue tagValue) {
                    return Either.right(tagValue);
                } else if (value instanceof Ingredient.ItemValue itemValue) {
                    return Either.left(itemValue);
                } else {
                    throw new UnsupportedOperationException("This is neither an item value nor a tag value.");
                }
            });
    }
}
