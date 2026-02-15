package xyz.bluspring.kilt.injections.world.item.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;
import java.util.function.Function;

@FabricInjectedInterface(Ingredient.class)
public interface IngredientInjection {
    MapCodec<Ingredient> MAP_CODEC_NONEMPTY = CraftingHelper.makeIngredientMapCodec();
    Codec<List<Ingredient>> LIST_CODEC = MAP_CODEC_NONEMPTY.codec().listOf();
    Codec<List<Ingredient>> LIST_CODEC_NONEMPTY = LIST_CODEC.validate(list -> list.isEmpty() ? DataResult.error(() -> "Item array cannot be empty, at least one item must be defined") : DataResult.success(list));

    static Ingredient create(ICustomIngredient customIngredient) {
        Ingredient ingredient = new Ingredient(new Ingredient.Value[0]);
        ingredient.kilt$setCustomIngredient(customIngredient);

        return ingredient;
    }

    default boolean hasNoItems() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "hasNoItems");
    }

    default Ingredient.Value[] getValues() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "getValues");
    }

    default boolean isSimple() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "isSimple");
    }

    default ICustomIngredient getCustomIngredient() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "getCustomIngredient");
    }

    default ICustomIngredient neoforge$getCustomIngredient() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "neoforge$getCustomIngredient");
    }

    default boolean isCustom() {
        throw KiltHelper.createMixinException(IngredientInjection.class, "isCustom");
    }

    default void kilt$setCustomIngredient(ICustomIngredient customIngredient) {
        throw KiltHelper.createMixinException(IngredientInjection.class, "kilt$setCustomIngredient");
    }

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
