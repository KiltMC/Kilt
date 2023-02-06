package xyz.bluspring.kilt.injections.world.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

import java.util.function.Supplier;

public interface FoodPropertiesBuilderInjection {
    default FoodProperties.Builder effect(Supplier<MobEffectInstance> effect, float probability) {
        throw new IllegalStateException();
    }
}
