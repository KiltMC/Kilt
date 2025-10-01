package xyz.bluspring.kilt.injections.world.item.alchemy;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public interface PotionBrewingInjection {
    AtomicReference<RegistryAccess> kilt$registryAccess = new AtomicReference<>(RegistryAccess.EMPTY);

    static PotionBrewing bootstrap(FeatureFlagSet enabledFeatures, RegistryAccess registryAccess) {
        kilt$registryAccess.set(registryAccess);
        return PotionBrewing.bootstrap(enabledFeatures);
    }

    default boolean isInput(ItemStack stack) {
        throw KiltHelper.createMixinException(PotionBrewingInjection.class, "isInput");
    }

    default List<IBrewingRecipe> getRecipes() {
        throw KiltHelper.createMixinException(PotionBrewingInjection.class, "getRecipes");
    }

    default void kilt$setBrewingRegistry(BrewingRecipeRegistry registry) {
        throw KiltHelper.createMixinException(PotionBrewingInjection.class, "kilt$setBrewingRegistry");
    }
}
