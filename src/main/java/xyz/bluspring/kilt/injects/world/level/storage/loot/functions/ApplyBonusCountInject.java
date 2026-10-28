package xyz.bluspring.kilt.injects.world.level.storage.loot.functions;

import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;

@Mixin(ApplyBonusCount.class)
public abstract class ApplyBonusCountInject {
    @Shadow @Final private Holder<Enchantment> enchantment;

    @ModifyVariable(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/functions/ApplyBonusCount$Formula;calculateNewCount(Lnet/minecraft/util/RandomSource;II)I"))
    private int kilt$tryGetBlockLootEnchantmentLevel(int level, @Local(ordinal = 1) ItemStack tool, @Local(argsOnly = true) LootContext context) {
        return EventHooks.getBlockLootEnchantmentLevel(tool, this.enchantment, level, context);
    }
}
