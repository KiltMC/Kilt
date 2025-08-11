package xyz.bluspring.kilt.injects.world.level.storage.loot.functions;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootingEnchantFunction.class)
public abstract class LootingEnchantFunctionInject {
    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getMobLooting(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int kilt$useContextLootingModifier(LivingEntity entity, @Local(argsOnly = true) LootContext context) {
        return context.getLootingModifier();
    }
}
