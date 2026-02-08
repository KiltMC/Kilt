package xyz.bluspring.kilt.injects.world.entity.animal;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Kilt: This doesn't extend Cow for one reason and one reason only: https://github.com/FabricMC/Mixin/issues/196
@Mixin(MushroomCow.class)
public abstract class MushroomCowInject implements IShearable {
    // Kilt: Shearing handled by Porting Lib / Vanilla

    @Inject(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"), cancellable = true)
    private void kilt$checkCanLivingConvert(SoundSource source, CallbackInfo ci) {
        if (!EventHooks.canLivingConvert((LivingEntity) (Object) this, EntityType.COW, timer -> {})) {
            ci.cancel();
        }
    }

    @Inject(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private void kilt$callLivingConvert(SoundSource source, CallbackInfo ci, @Local Cow cow) {
        EventHooks.onLivingConvert((LivingEntity) (Object) this, cow);
    }

    // TODO: captureDrops stuff
}
