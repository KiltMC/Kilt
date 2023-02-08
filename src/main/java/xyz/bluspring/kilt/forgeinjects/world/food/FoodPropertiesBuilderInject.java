package xyz.bluspring.kilt.forgeinjects.world.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.food.FoodPropertiesBuilderInjection;
import xyz.bluspring.kilt.injections.world.food.FoodPropertiesInjection;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(FoodProperties.Builder.class)
public class FoodPropertiesBuilderInject implements FoodPropertiesBuilderInjection {
    private final List<Pair<Supplier<MobEffectInstance>, Float>> kilt$deferredEffects = new LinkedList<>();

    @Override
    public FoodProperties.Builder effect(Supplier<MobEffectInstance> effect, float probability) {
        this.kilt$deferredEffects.add(Pair.of(effect, probability));
        return (FoodProperties.Builder) (Object) this;
    }

    @Override
    public List<Pair<Supplier<MobEffectInstance>, Float>> kilt$getDeferredEffects() {
        return this.kilt$deferredEffects;
    }

    @Inject(at = @At("RETURN"), method = "build")
    public void kilt$setDeferredEffects(CallbackInfoReturnable<FoodProperties> cir) {
        ((FoodPropertiesInjection) cir.getReturnValue()).kilt$setDeferredEffects(kilt$deferredEffects);
    }
}
