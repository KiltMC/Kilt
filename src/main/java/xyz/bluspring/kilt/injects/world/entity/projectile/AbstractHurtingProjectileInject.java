package xyz.bluspring.kilt.injects.world.entity.projectile;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileInject extends Projectile {
    public AbstractHurtingProjectileInject(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "hitResult", local = @Local(type = HitResult.class))
    @Definition(id = "getType", method = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;")
    @Definition(id = "MISS", field = "Lnet/minecraft/world/phys/HitResult$Type;MISS:Lnet/minecraft/world/phys/HitResult$Type;")
    @Expression("hitResult.getType() != MISS")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkProjectileImpactEvent(boolean original, @Local HitResult hitResult) {
        return original && !EventHooks.onProjectileImpact(this, hitResult);
    }
}
