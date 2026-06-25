package xyz.bluspring.kilt.injects.world.entity.projectile.hurtingprojectile;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;

@Mixin(LargeFireball.class)
public abstract class LargeFireballInject extends Fireball {
    public LargeFireballInject(EntityType<? extends Fireball> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "Boolean", type = Boolean.class)
    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Expression("(Boolean) ?.get(MOB_GRIEFING)")
    @ModifyExpressionValue(method = "onHit", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Boolean kilt$checkCanInvokeMobGriefing(Boolean original) {
        return original || EventHooks.canEntityGrief((ServerLevel) this.level(), this.getOwner());
    }
}
