// TRACKED HASH: 7db9f60a09f2e5b156013ce9fa93086ae63920c1
package xyz.bluspring.kilt.forgeinjects.world.food;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.food.FoodPropertiesInjection;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

@Mixin(FoodProperties.class)
public class FoodPropertiesInject implements FoodPropertiesInjection {
    @Shadow @Final @Mutable
    private List<Pair<MobEffectInstance, Float>> effects;
    private List<Pair<Supplier<MobEffectInstance>, Float>> kilt$deferredEffects;

    @Override
    public void kilt$setDeferredEffects(List<Pair<Supplier<MobEffectInstance>, Float>> deferredEffects) {
        this.kilt$deferredEffects = deferredEffects;
    }

    @Inject(at = @At("HEAD"), method = "getEffects")
    public void kilt$appendDeferredEffects(CallbackInfoReturnable<List<Pair<MobEffectInstance, Float>>> cir) {
        if (!this.kilt$deferredEffects.isEmpty()) {
            var list = new LinkedList<>(this.effects);

            for (Pair<Supplier<MobEffectInstance>, Float> deferredEffect : kilt$deferredEffects) {
                var newPair = Pair.of(deferredEffect.getFirst().get(), deferredEffect.getSecond());
                list.add(newPair);
            }

            this.effects = list;
        }
    }
}