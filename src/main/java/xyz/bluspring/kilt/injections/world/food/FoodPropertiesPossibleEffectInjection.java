package xyz.bluspring.kilt.injections.world.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Supplier;

@FabricInjectedInterface(FoodProperties.PossibleEffect.class)
public interface FoodPropertiesPossibleEffectInjection {

    static FoodProperties.PossibleEffect create(Supplier<MobEffectInstance> effectSupplier, float probability) {
        var possibleEffect = new FoodProperties.PossibleEffect(null, probability);
        possibleEffect.kilt$setEffectSupplier(effectSupplier);
        return possibleEffect;
    }

    default void kilt$setEffectSupplier(Supplier<MobEffectInstance> effectSupplier) {
        throw KiltHelper.createMixinException(FoodPropertiesPossibleEffectInjection.class, "kilt$setEffectSupplier");
    }

    default Supplier<MobEffectInstance> effectSupplier() {
        throw KiltHelper.createMixinException(FoodPropertiesPossibleEffectInjection.class, "effectSupplier");
    }
}
