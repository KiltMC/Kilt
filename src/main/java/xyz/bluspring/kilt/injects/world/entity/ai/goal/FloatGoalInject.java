package xyz.bluspring.kilt.injects.world.entity.ai.goal;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FloatGoal.class)
public abstract class FloatGoalInject extends Goal {
    @Shadow @Final private Mob mob;

    @ModifyReturnValue(method = "canUse", at = @At("RETURN"))
    private boolean kilt$tryCheckFluidType(boolean original) {
        return original || this.mob.isInFluidType((fluidType, height) -> this.mob.canSwimInFluidType(fluidType) && height > this.mob.getFluidJumpThreshold());
    }
}
