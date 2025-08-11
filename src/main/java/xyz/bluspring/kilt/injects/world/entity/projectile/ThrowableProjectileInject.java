package xyz.bluspring.kilt.injects.world.entity.projectile;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrowableProjectile.class)
public abstract class ThrowableProjectileInject extends Projectile {
    public ThrowableProjectileInject(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    private boolean kilt$checkProjectileImpactEvent(ThrowableProjectile instance, HitResult hitResult) {
        return !EventHooks.onProjectileImpact(this, hitResult);
    }
}
