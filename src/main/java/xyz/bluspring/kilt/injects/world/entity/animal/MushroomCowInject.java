package xyz.bluspring.kilt.injects.world.entity.animal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MushroomCow.class)
public abstract class MushroomCowInject extends Cow implements IShearable {
    public MushroomCowInject(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }

    // Kilt: Shearing handled by Porting Lib / Vanilla
    @Inject(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"), cancellable = true)
    private void kilt$checkCanLivingConvert(SoundSource source, CallbackInfo ci) {
        if (!EventHooks.canLivingConvert(this, EntityType.COW, timer -> {})) {
            ci.cancel();
        }
    }

    @Inject(method = "shear", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private void kilt$callLivingConvert(SoundSource source, CallbackInfo ci, @Local Cow cow) {
        EventHooks.onLivingConvert(this, cow);
    }

    // TODO: captureDrops stuff
}
