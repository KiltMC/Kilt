package xyz.bluspring.kilt.forgeinjects.world.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.food.FoodPropertiesBuilderInjection;

import java.util.List;
import java.util.function.Supplier;

@Mixin(FoodProperties.Builder.class)
public class FoodPropertiesBuilderInject implements FoodPropertiesBuilderInjection {
    @Shadow @Final private List<Pair<MobEffectInstance, Float>> effects;

    @Override
    public FoodProperties.Builder effect(Supplier<MobEffectInstance> effect, float probability) {
        this.effects.add(Pair.of(effect.get(), probability));
        return (FoodProperties.Builder) (Object) this;
    }
}
