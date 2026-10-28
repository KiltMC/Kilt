package xyz.bluspring.kilt.injects.world.level.storage.loot.predicates;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;

@Mixin(BonusLevelTableCondition.class)
public abstract class BonusLevelTableConditionInject {
    @Shadow @Final private Holder<Enchantment> enchantment;

    @Definition(id = "values", field = "Lnet/minecraft/world/level/storage/loot/predicates/BonusLevelTableCondition;values:Ljava/util/List;")
    @Definition(id = "get", method = "Ljava/util/List;get(I)Ljava/lang/Object;")
    @Expression("this.values.get(?)")
    @ModifyVariable(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int kilt$tryGetBlockLootEnchantmentLevel(int level, @Local ItemStack tool, @Local(argsOnly = true) LootContext context) {
        return EventHooks.getBlockLootEnchantmentLevel(tool, this.enchantment, level, context);
    }
}
