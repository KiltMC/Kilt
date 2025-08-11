package xyz.bluspring.kilt.injects.world.effect;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net/minecraft/world/effect/PoisonMobEffect")
public abstract class PoisonMobEffectInject {
    @ModifyArg(method = "applyEffectTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private DamageSource kilt$useNeoPoisonDamage(DamageSource damageSource, @Local(argsOnly = true) LivingEntity entity) {
        var dTypeReg = entity.damageSources().damageTypes;
        var dType = dTypeReg.getHolder(NeoForgeMod.POISON_DAMAGE);

        if (!damageSource.is(DamageTypes.MAGIC) || dType.isEmpty()) {
            return damageSource;
        }

        return new DamageSource(dType.orElseThrow());
    }
}
