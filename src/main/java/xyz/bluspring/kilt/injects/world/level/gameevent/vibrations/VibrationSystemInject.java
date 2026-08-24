package xyz.bluspring.kilt.injects.world.level.gameevent.vibrations;

import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

@Mixin(VibrationSystem.class)
public interface VibrationSystemInject {
    @Inject(method = "getGameEventFrequency(Lnet/minecraft/core/Holder;)I", at = @At("HEAD"), cancellable = true)
    private static void kilt$tryUseDataMapIfPossible(Holder<GameEvent> gameEvent, CallbackInfoReturnable<Integer> cir) {
        var data = gameEvent.getData(NeoForgeDataMaps.VIBRATION_FREQUENCIES);

        if (data != null) {
            cir.setReturnValue(data.frequency());
        }
    }

    @Inject(method = "getGameEventFrequency(Lnet/minecraft/resources/ResourceKey;)I", at = @At("HEAD"), cancellable = true)
    private static void kilt$tryUseDataMapIfPossible(ResourceKey<GameEvent> gameEvent, CallbackInfoReturnable<Integer> cir) {
        var holder = BuiltInRegistries.GAME_EVENT.get(gameEvent);
        
        if (holder.isPresent()) {
            var data = holder.orElseThrow().getData(NeoForgeDataMaps.VIBRATION_FREQUENCIES);

            if (data != null) {
                cir.setReturnValue(data.frequency());
            }
        }
    }
}
