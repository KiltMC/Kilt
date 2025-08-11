package xyz.bluspring.kilt.injects.world.level.storage.loot.predicates;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootItemRandomChanceWithLootingCondition.class)
public abstract class LootItemRandomChanceWithLootingConditionInject {
    @Inject(method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/level/storage/loot/LootContext;getRandom()Lnet/minecraft/util/RandomSource;"))
    private void kilt$modifyLootingModifier(LootContext lootContext, CallbackInfoReturnable<Boolean> cir, @Local LocalIntRef lootingModifier) {
        lootingModifier.set(lootContext.getLootingModifier());
    }
}
