package xyz.bluspring.kilt.injects.world.item.crafting;

import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.item.crafting.IngredientInjection;
import xyz.bluspring.kilt.workarounds.FabricCustomIngredientWrapper;

import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;

@Mixin(Ingredient.class)
public abstract class IngredientInject implements FabricIngredient, IngredientInjection {
    // Kilt: Fabric API already handles most of the ingredient stuff, actually. We defer to Fabric API anyway for custom ingredients,
    //       so let's just redirect whatever else.

    @Shadow @Final private HolderSet<Item> values;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;ifRight(Ljava/util/function/Consumer;)Lcom/mojang/datafixers/util/Either;"))
    private <L, R> Either<L, R> kilt$checkIsImmediatelyResolvable(Either<L, R> instance, Consumer<? super R> consumer, Operation<Either<L, R>> original, @Local(argsOnly = true, name = "values") HolderSet<Item> values) {
        if (values.isImmediatelyResolvable())
            return original.call(instance, consumer);

        return instance;
    }

    // Kilt: The Ingredient constructor here should be remapped to KiltCustomIngredient,
    //       so we can directly use Fabric API's stuff.
    @CreateInitializer
    public IngredientInject(ICustomIngredient customIngredient) {
        throw new IllegalStateException("This should have been remapped out!");
    }

    @Override
    public HolderSet<Item> getValues() {
        return this.values;
    }

    @Override
    public boolean isSimple() {
        return this.requiresTesting();
    }

    public @Nullable ICustomIngredient getCustomIngredient() {
        return this.kilt$getCustomIngredient();
    }

    @Override
    public @Nullable ICustomIngredient kilt$getCustomIngredient() {
        var ingredient = ((FabricIngredient) this).getCustomIngredient();
        if (ingredient instanceof ICustomIngredient customIngredient) {
            return customIngredient;
        } else if (ingredient != null) {
            return new FabricCustomIngredientWrapper(ingredient);
        }

        return null;
    }

    @Override
    public boolean isCustom() {
        return this.getCustomIngredient() != null;
    }
}
