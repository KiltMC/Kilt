package xyz.bluspring.kilt.injects.world.entity.monster.illager;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.illager.Evoker;

@Mixin(Evoker.class)
public abstract class EvokerInject {
    @Mixin(Evoker.EvokerWololoSpellGoal.class)
    public abstract static class EvokerWololoSpellGoalInject {
        @Shadow @Final Evoker this$0;

        @Definition(id = "Boolean", type = Boolean.class)
        @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
        @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
        @Expression("(Boolean) ?.get(MOB_GRIEFING)")
        @ModifyExpressionValue(method = "canUse", at = @At("MIXINEXTRAS:EXPRESSION"))
        private Boolean kilt$checkMobGriefing(Boolean original) {
            return original || EventHooks.canEntityGrief((ServerLevel) this$0.level(), this$0);
        }
    }
}
