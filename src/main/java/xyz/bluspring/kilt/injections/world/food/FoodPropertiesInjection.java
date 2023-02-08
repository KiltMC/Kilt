package xyz.bluspring.kilt.injections.world.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;
import java.util.function.Supplier;

public interface FoodPropertiesInjection {
    default void kilt$setDeferredEffects(List<Pair<Supplier<MobEffectInstance>, Float>> deferredEffects) {
        throw new IllegalStateException();
    }
}
