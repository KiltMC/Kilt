package xyz.bluspring.kilt.injections.world.item.enchantment;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public interface EnchantmentHelperInjection {
    ThreadLocal<Boolean> kilt$shouldUseTagEnchantment = ThreadLocal.withInitial(() -> false);
    ThreadLocal<EquipmentSlot> kilt$currentSlot = new ThreadLocal<>();

    static int getTagEnchantmentLevel(Holder<Enchantment> enchantment, ItemStack stack) {
        kilt$shouldUseTagEnchantment.set(true);
        var value = EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
        kilt$shouldUseTagEnchantment.remove();
        return value;
    }

    static float processEquipmentDropChance(ServerLevel level, LivingEntity entity, DamageSource source, float equipmentDropChance, @Nullable EquipmentSlot slot) {
        try {
            kilt$currentSlot.set(slot);
            return EnchantmentHelper.processEquipmentDropChance(level, entity, source, equipmentDropChance);
        } finally {
            kilt$currentSlot.set(null);
        }
    }
}
