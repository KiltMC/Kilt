package xyz.bluspring.kilt.forgeinjects.advancements;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AdvancementRewards.class)
public class AdvancementRewardsInject {
    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootContext$Builder;withRandom(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/world/level/storage/loot/LootContext$Builder;"), method = "grant")
    public LootContext.Builder kilt$addLuckToLootContext(LootContext.Builder instance, RandomSource random, Operation<LootContext.Builder> original, @Local(argsOnly = true) ServerPlayer player) {
        return original.call(instance, random).withLuck(player.getLuck());
    }
}
