package xyz.bluspring.kilt.injections.world.entity;

import java.util.Stack;

import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.entity.player.Player;

public interface LivingEntityInjection {
    default Stack<DamageContainer> kilt$getDamageContainers() {
        throw KiltHelper.createMixinException(LivingEntityInjection.class, "kilt$getDamageContainers");
    }

    default boolean removeEffectsCuredBy(EffectCure cure) {
        throw KiltHelper.createMixinException(LivingEntityInjection.class, "removeEffectsCuredBy");
    }

    default boolean shouldRiderFaceForward(Player player) {
        throw KiltHelper.createMixinException(LivingEntityInjection.class, "shouldRiderFaceForward");
    }

    default boolean kilt$wasDeathHandledByUs() {
        throw KiltHelper.createMixinException(LivingEntityInjection.class, "kilt$wasDeathHandledByUs");
    }
}
