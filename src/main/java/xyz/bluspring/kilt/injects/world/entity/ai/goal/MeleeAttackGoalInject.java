package xyz.bluspring.kilt.injects.world.entity.ai.goal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalInject extends Goal {
    @Shadow private int ticksUntilNextPathRecalculation;
    @Shadow private Path path;
    @Shadow @Final protected PathfinderMob mob;
    @Unique private int failedPathFindingPenalty = 0;
    @Unique private boolean canPenalize = false;

    // Kilt: Not entirely sure why this is here, but just to be safe, y'know

    @Inject(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;createPath(Lnet/minecraft/world/entity/Entity;I)Lnet/minecraft/world/level/pathfinder/Path;"), cancellable = true)
    private void kilt$checkCanUsePenalized(CallbackInfoReturnable<Boolean> cir, @Local LivingEntity entity) {
        if (canPenalize) {
            if (--this.ticksUntilNextPathRecalculation <= 0) {
                this.path = this.mob.getNavigation().createPath(entity, 0);
                this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                cir.setReturnValue(this.path != null);
            } else {
                cir.setReturnValue(true);
            }
        }
    }

    @Definition(id = "ticksUntilNextPathRecalculation", field = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;ticksUntilNextPathRecalculation:I")
    @Definition(id = "mob", field = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;mob:Lnet/minecraft/world/entity/PathfinderMob;")
    @Definition(id = "getRandom", method = "Lnet/minecraft/world/entity/PathfinderMob;getRandom()Lnet/minecraft/util/RandomSource;")
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression("this.ticksUntilNextPathRecalculation = ? + this.mob.getRandom().nextInt(?)")
    @Inject(method = "tick", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$checkPenalty(CallbackInfo ci, @Local LivingEntity entity) {
        if (this.canPenalize) {
            this.ticksUntilNextPathRecalculation += this.failedPathFindingPenalty;

            if (this.mob.getNavigation().getPath() != null) {
                var finalPathPoint = this.mob.getNavigation().getPath().getEndNode();

                if (finalPathPoint != null && entity.distanceToSqr(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
                    this.failedPathFindingPenalty = 0;
                else this.failedPathFindingPenalty += 10;
            } else {
                this.failedPathFindingPenalty += 10;
            }
        }
    }
}
