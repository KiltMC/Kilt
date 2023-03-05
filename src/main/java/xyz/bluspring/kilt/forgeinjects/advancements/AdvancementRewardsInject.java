package xyz.bluspring.kilt.forgeinjects.advancements;

import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdvancementRewards.class)
public class AdvancementRewardsInject {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootContext$Builder;create(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParamSet;)Lnet/minecraft/world/level/storage/loot/LootContext;"), method = "grant")
    public LootContext kilt$addLuckToLootContext(LootContext.Builder instance, LootContextParamSet lootContextParamSet, ServerPlayer player) {
        return instance.withLuck(player.getLuck()).create(lootContextParamSet);
    }
}
