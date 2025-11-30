package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Stack;

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
}
