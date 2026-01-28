package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

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

        @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
        private boolean kilt$checkMobGriefingEvent(boolean original) {
            return original && EventHooks.canEntityGrief(this.mob.level(), this.mob);
        }
    }

    @Mixin(targets = "net.minecraft.world.entity.monster.Silverfish$SilverfishWakeUpFriendsGoal")
    public abstract static class SilverfishWakeUpFriendsGoalInject extends Goal {
        @Shadow @Final private Silverfish silverfish;

        @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
        private boolean kilt$checkMobGriefingEvent(boolean original) {
            return original && EventHooks.canEntityGrief(this.silverfish.level(), this.silverfish);
        }
    }
}
