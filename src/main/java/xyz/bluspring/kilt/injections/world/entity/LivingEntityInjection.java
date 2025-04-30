package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.item.ItemStack;

public interface LivingEntityInjection {
    boolean curePotionEffects(ItemStack curativeStack);
}
