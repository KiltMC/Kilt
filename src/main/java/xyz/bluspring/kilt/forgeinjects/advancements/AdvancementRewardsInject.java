// TRACKED HASH: b711f8bd363f86bd2bb82e79b379c95f0f649632
package xyz.bluspring.kilt.forgeinjects.advancements;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AdvancementRewards.class)
public class AdvancementRewardsInject {
    @ModifyReceiver(method = "grant", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;create(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParamSet;)Lnet/minecraft/world/level/storage/loot/LootParams;"))
    public LootParams.Builder kilt$addLuckToLootContext(LootParams.Builder instance, LootContextParamSet params, @Local(argsOnly = true) ServerPlayer player) {
        return instance.withLuck(player.getLuck());
    }
}