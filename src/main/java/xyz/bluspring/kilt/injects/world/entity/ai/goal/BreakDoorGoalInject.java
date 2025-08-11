package xyz.bluspring.kilt.injects.world.entity.ai.goal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BreakDoorGoal.class)
public abstract class BreakDoorGoalInject extends DoorInteractGoal {
    public BreakDoorGoalInject(Mob mob) {
        super(mob);
    }

    @ModifyExpressionValue(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkCanEntityDestroy(boolean original) {
        return original || CommonHooks.canEntityDestroy(this.mob.level(), this.doorPos, this.mob);
    }
}
