package xyz.bluspring.kilt.injects.world.level.storage.loot.parameters;

import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootContextParamSets.class)
public abstract class LootContextParamSetsInject {
    @Inject(method = "method_15970", at = @At("TAIL"))
    private static void kilt$addChestMinecartOptionalParams(LootContextParamSet.Builder builder, CallbackInfo ci) {
        builder.optional(LootContextParams.KILLER_ENTITY);
    }

    @Inject(method = "method_764", at = @At("TAIL"))
    private static void kilt$addFishingOptionalParams(LootContextParamSet.Builder builder, CallbackInfo ci) {
        builder.optional(LootContextParams.KILLER_ENTITY).optional(LootContextParams.THIS_ENTITY);
    }
}
