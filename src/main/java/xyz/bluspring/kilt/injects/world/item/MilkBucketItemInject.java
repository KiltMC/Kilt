package xyz.bluspring.kilt.injects.world.item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MilkBucketItem;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemInject {
    @WrapOperation(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean kilt$tryCureViaMilk(LivingEntity instance, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(instance.getClass(), LivingEntity.class, "removeEffectsCuredBy", boolean.class, EffectCure.class)) {
            return instance.removeEffectsCuredBy(EffectCures.MILK);
        } else {
            Iterator<MobEffectInstance> itr = instance.getActiveEffectsMap().values().iterator();
            List<MobEffectInstance> effectsToReAdd = new ArrayList<>();

            while (itr.hasNext()) {
                MobEffectInstance effect = itr.next();

                if (!effect.neoforge$getCures().contains(EffectCures.MILK)) {
                    effectsToReAdd.add(effect);
                }
            }

            for (MobEffectInstance effect : effectsToReAdd) {
                instance.getActiveEffectsMap().remove(effect.getEffect());
            }

            var ret = original.call(instance);

            for (MobEffectInstance effect : effectsToReAdd) {
                instance.getActiveEffectsMap().put(effect.getEffect(), effect);
            }

            return ret;
        }
    }
}
