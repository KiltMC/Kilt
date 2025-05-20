package xyz.bluspring.kilt.forgeinjects.world.entity.projectile;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletInject extends Projectile {
    public ShulkerBulletInject(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "hitResult", local = @Local(type = HitResult.class))
    @Definition(id = "getType", method = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;")
    @Definition(id = "MISS", field = "Lnet/minecraft/world/phys/HitResult$Type;MISS:Lnet/minecraft/world/phys/HitResult$Type;")
    @Expression("hitResult.getType() != MISS")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkProjectileImpactEvent(boolean original, @Local HitResult hitResult) {
        return original && !ForgeEventFactory.onProjectileImpact(this, hitResult);
    }
}
