// TRACKED HASH: d5622944d672b2af11ae93f5d7d4e097c075ddc4
package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Explosion.class)
public abstract class ExplosionInject {
    @Shadow @Final private Level level;

    @Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;<init>(DDD)V", ordinal = 1))
    private void kilt$callNeoExplosionDetonate(CallbackInfo ci, @Local List<Entity> entities, @Local(ordinal = 0) float diameter) {
        EventHooks.onExplosionDetonate(this.level, (Explosion) (Object) this, entities, diameter);
    }

    @ModifyVariable(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"), ordinal = 1)
    private Vec3 kilt$modifyExplosionKnockback(Vec3 original, @Local Entity entity) {
        return EventHooks.getExplosionKnockback(this.level, (Explosion) (Object) this, entity, original);
    }
}