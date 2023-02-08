package xyz.bluspring.kilt.injections.world.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;

import java.util.List;
import java.util.function.Supplier;

public interface FoodPropertiesBuilderInjection {
    default FoodProperties.Builder effect(Supplier<MobEffectInstance> effect, float probability) {
        throw new IllegalStateException();
    }

    default List<Pair<Supplier<MobEffectInstance>, Float>> kilt$getDeferredEffects() {
        throw new IllegalStateException();
    }
}
