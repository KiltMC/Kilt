package xyz.bluspring.kilt.mixin.compat.fabric_api.resource.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.item.crafting.Ingredient;

@Mixin(value = Ingredient.class, priority = 1100) // higher priority than Fabric API
public abstract class IngredientMixin {
    @Dynamic // Kilt: idk why this doesn't just work but oh well ig
    @TargetHandler(mixin = "net.fabricmc.fabric.mixin.recipe.ingredient.IngredientMixin", name = "injectCodec", prefix = "handler")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;dispatch(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
    private static <A, E> Codec<E> kilt$fabric_api$tryHandleBothIngredientTypes(Codec<A> instance, String typeKey, Function<? super E, ? extends A> type, Function<? super A, ? extends MapCodec<? extends E>> codec, Operation<Codec<E>> original) {
        return Codec.withAlternative(
            original.call(instance, typeKey, type, codec),
            original.call(instance, "type", type, codec)
        );
    }

    @WrapMethod(method = "codec") // otherwise, @Inject cancel takes priority.
    private static Codec<Ingredient> kilt$fabric_api$supportListCodecs(boolean allowEmpty, Operation<Codec<Ingredient>> original) {
        Codec<Ingredient> codec = original.call(allowEmpty);
        return Codec.withAlternative(
            codec,
            codec.listOf()
                .xmap(
                    list -> CompoundIngredient.of(list.toArray(new Ingredient[0])),
                    ingredient -> {
                        if (ingredient.getCustomIngredient() instanceof CompoundIngredient compoundIngredient) {
                            return compoundIngredient.children();
                        } else if (ingredient.getValues().length != 1) {
                            return Arrays.stream(ingredient.getValues())
                                .map(v -> Ingredient.fromValues(Stream.of(v)))
                                .toList();
                        } else {
                            return List.of(ingredient);
                        }
                    })
        );
    }
}
