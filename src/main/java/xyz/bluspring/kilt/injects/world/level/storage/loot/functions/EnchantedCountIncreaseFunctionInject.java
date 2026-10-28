package xyz.bluspring.kilt.injects.world.level.storage.loot.functions;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;

@Mixin(EnchantedCountIncreaseFunction.class)
public abstract class EnchantedCountIncreaseFunctionInject {
    @Shadow @Final private Holder<Enchantment> enchantment;

    @Definition(id = "i", local = @Local(type = int.class))
    @Expression("i == 0")
    @ModifyVariable(method = "run", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$tryGetBlockLootEnchantmentLevel(int level, @Local(argsOnly = true) LootContext context) {
        return EventHooks.getEntityLootEnchantmentLevel(this.enchantment, level, context);
    }
}
