// TRACKED HASH: 6c2af6a881a23ea04d8f49bf80f6e56938c38e8d
package xyz.bluspring.kilt.injects.world.item.alchemy;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.alchemy.PotionBrewingInjection;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingInject implements PotionBrewingInjection {
    @Shadow
    protected abstract boolean isContainer(ItemStack stack);

    @Shadow
    public static PotionBrewing bootstrap(FeatureFlagSet enabledFeatures) {
        throw new IllegalStateException();
    }

    private BrewingRecipeRegistry registry;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initRegistryWithEmptyRecipes(List<Ingredient> containers, List<PotionBrewing.Mix<Potion>> potionMixes, List<PotionBrewing.Mix<Item>> containerMixes, CallbackInfo ci) {
        this.registry = new BrewingRecipeRegistry(List.of());
    }

    public PotionBrewingInject(List<Ingredient> containers, List<PotionBrewing.Mix<Potion>> potionMixes, List<PotionBrewing.Mix<Item>> containerMixes) {}

    @CreateInitializer
    public PotionBrewingInject(List<Ingredient> containers, List<PotionBrewing.Mix<Potion>> potionMixes, List<PotionBrewing.Mix<Item>> containerMixes, List<IBrewingRecipe> recipes) {
        this(containers, potionMixes, containerMixes);
        this.registry = new BrewingRecipeRegistry(recipes);
    }

    @Override
    public void kilt$setBrewingRegistry(BrewingRecipeRegistry registry) {
        this.registry = registry;
    }

    @ModifyReturnValue(method = "isIngredient", at = @At("RETURN"))
    private boolean kilt$checkIsValidIngredientInRegistry(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return this.registry.isValidIngredient(stack) || original;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return this.registry.isValidInput(stack) || this.isContainer(stack);
    }

    @Override
    public List<IBrewingRecipe> getRecipes() {
        return this.registry.recipes();
    }

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void kilt$checkIfRegistryHasOutput(ItemStack reagent, ItemStack potionItem, CallbackInfoReturnable<Boolean> cir) {
        if (this.registry.hasOutput(reagent, potionItem))
            cir.setReturnValue(true);
    }

    @Inject(method = "mix", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"), cancellable = true)
    private void kilt$tryReturnCustomMix(ItemStack potion, ItemStack potionItem, CallbackInfoReturnable<ItemStack> cir) {
        var customMix = this.registry.getOutput(potionItem, potion); // apparently Neo says these are flipped, so...
        if (!customMix.isEmpty()) {
            cir.setReturnValue(customMix);
        }
    }

    @CreateStatic
    private static PotionBrewing bootstrap(FeatureFlagSet enabledFeatures, RegistryAccess registryAccess) {
        PotionBrewingInjection.kilt$registryAccess.set(registryAccess);
        return bootstrap(enabledFeatures);
    }

    @Inject(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionBrewing$Builder;build()Lnet/minecraft/world/item/alchemy/PotionBrewing;"))
    private static void kilt$callRegisterBrewingRecipesEvent(FeatureFlagSet enabledFeatures, CallbackInfoReturnable<PotionBrewing> cir, @Local PotionBrewing.Builder builder) {
        var registryAccess = PotionBrewingInjection.kilt$registryAccess.getAndSet(RegistryAccess.EMPTY);
        NeoForge.EVENT_BUS.post(new RegisterBrewingRecipesEvent(builder, registryAccess));
    }

    @Mixin(PotionBrewing.Builder.class)
    public static abstract class BuilderInject implements PotionBrewingInjection.BuilderInjection {
        private final List<IBrewingRecipe> recipes = new ArrayList<>();

        // Porting Lib already does this
        /*@Override
        public void addRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
            this.addRecipe(new BrewingRecipe(input, ingredient, output));
        }*/

        @Override
        public void addRecipe(IBrewingRecipe recipe) {
            this.recipes.add(recipe);
        }

        @ModifyReturnValue(method = "build", at = @At("RETURN"))
        private PotionBrewing kilt$appendNeoRecipes(PotionBrewing original) {
            original.kilt$setBrewingRegistry(new BrewingRecipeRegistry(this.recipes));
            return original;
        }
    }
}
