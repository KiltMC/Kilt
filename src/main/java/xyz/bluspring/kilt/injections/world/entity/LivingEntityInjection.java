package xyz.bluspring.kilt.injections.world.entity;

import java.util.Stack;

import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public interface LivingEntityInjection {
    default @Nullable Stack<DamageContainer> kilt$getDamageContainers() {
        throw KiltHelper.createMixinException(LivingEntityInjection.class, "kilt$getDamageContainers");
    }

    default void setItemSlot(EquipmentSlot slot, ItemStack itemStack, boolean insideTransaction) {
        throw KiltHelper.createMixinException(LivingEntityInjection.class, "setItemSlot");
    }
}
