package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;

@Mixin(Silverfish.class)
public abstract class SilverfishInject extends Monster {
    protected SilverfishInject(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishMergeWithStoneGoal")
    public abstract static class SilverfishMergeWithStoneGoalInject extends RandomStrollGoal {
        public SilverfishMergeWithStoneGoalInject(PathfinderMob mob, double speedModifier) {
            super(mob, speedModifier);
        }

        @Definition(id = "Boolean", type = Boolean.class)
        @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
        @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
        @Expression("(Boolean) ?.get(MOB_GRIEFING)")
        @ModifyExpressionValue(method = "canUse", at = @At("MIXINEXTRAS:EXPRESSION"))
        private Boolean kilt$checkMobGriefingEvent(Boolean original) {
            return original && EventHooks.canEntityGrief(getServerLevel(this.mob.level()), this.mob);
        }
    }

    @Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishWakeUpFriendsGoal")
    public abstract static class SilverfishWakeUpFriendsGoalInject extends Goal {
        @Shadow @Final private Silverfish silverfish;

        @Definition(id = "Boolean", type = Boolean.class)
        @Definition(id = "get", method = "Lnet/minecraft/world/level/gamerules/GameRules;get(Lnet/minecraft/world/level/gamerules/GameRule;)Ljava/lang/Object;")
        @Definition(id = "MOB_GRIEFING", field = "Lnet/minecraft/world/level/gamerules/GameRules;MOB_GRIEFING:Lnet/minecraft/world/level/gamerules/GameRule;")
        @Expression("(Boolean) ?.get(MOB_GRIEFING)")
        @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
        private Boolean kilt$checkMobGriefingEvent(Boolean original) {
            return original && EventHooks.canEntityGrief(getServerLevel(this.silverfish.level()), this.silverfish);
        }
    }
}
