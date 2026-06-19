package xyz.bluspring.kilt.compat.forge.mixin.quark;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
    @Definition(id = "shooter", local = @Local(type = Entity.class, ordinal = 0, argsOnly = true))
    @Definition(id = "entity2", local = @Local(type = Entity.class, ordinal = 2))
    @Definition(id = "getRootVehicle", method = "Lnet/minecraft/world/entity/Entity;getRootVehicle()Lnet/minecraft/world/entity/Entity;")
    @Expression("entity2.getRootVehicle() == shooter.getRootVehicle()")
    @ModifyExpressionValue(method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static boolean too(boolean original, Entity shooter, Vec3 startVec, Vec3 endVec, AABB boundingBox, Predicate<Entity> filter, double distance, @Local(ordinal = 2) Entity entity1) {
        if (shooter instanceof Player && entity1 instanceof Chicken)
            return false;
        return original;
    }
}
