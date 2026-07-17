package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

@Mixin(Ravager.class)
public abstract class RavagerInject extends Raider {
    protected RavagerInject(EntityType<? extends Raider> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "Boolean", type = Boolean.class)
    @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
    @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
    @Expression("(Boolean) ?.get(MOB_GRIEFING)")
    @ModifyExpressionValue(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Boolean kilt$checkMobGriefingEvent(Boolean original, @Local ServerLevel level) {
        return original && EventHooks.canEntityGrief(level, this);
    }
}
